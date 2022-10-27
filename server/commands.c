
#include "commands.h"
#include "main.h"
#include "shipsGame.h"

#define SPLIT_SYMBOL ';'
const int COMMANDS_COUNT = 8;
const cmd_handler COMMANDS[] =
    {
        {"login_req", cmd_login},
        {"room_create_req", cmd_room_create},
        {"room_list_req", cmd_room_list},
        {"room_join_req", cmd_room_join},
        {"room_leave_req", cmd_room_leave},
        {"game_prepared", cmd_game_prepared},
        {"game_fire_req", cmd_game_fire},
       // {"game_conn", cmd_game_conn},

        {"logout_req", cmd_logout}
    };

fcmd get_handler(char command[])
{
    int i;
    unsigned long command_length = strlen(command);

    for(i = 0; i < command_length; i++)
    {
        command[i] = (char) tolower(command[i]);
    }

    for(i = 0; i < COMMANDS_COUNT; ++i)
    {
        if(!strcmp(COMMANDS[i].cmd, command)) {
            return COMMANDS[i].handler;
        }
    }

    return NULL;
}

int cmd_login(server *server, struct client *client, int argc, char **argv)
{
    char buff[64];
    if(!client || !argv)
    {
        sprintf(buff,"login_err%c%d\n", SPLIT_SYMBOL, 1);
        send_message(client, buff);
        trace("Socket %d - Login request filed due internal error", client->fd);
        return EXIT_FAILURE;
    }

    if(argc < 1) {
        sprintf(buff,"login_err%c%d\n", SPLIT_SYMBOL, 2);
        send_message(client, buff);
        trace("Socket %d - Login request failed due message format error", client->fd);
        return EXIT_FAILURE;
    }

    if(strlen(argv[0]) == 0 || strlen(argv[0]) > NAME_MAX_LENGTH)
    {
        sprintf(buff,"login_err%c%d\n", SPLIT_SYMBOL, 3);
        send_message(client, buff);
        trace("Socket %d - User is trying to login as %s, but this name does not meet requirements", client->fd, argv[0]);
        return EXIT_FAILURE;
    }
    else
    {
        int i;
        for(i = 0; i < strlen(argv[0]); i++) {
            if(argv[0][i] >= '0' && argv[0][i] <= '9') continue;
            if(argv[0][i] >= 'A' && argv[0][i] <= 'Z') continue;
            if(argv[0][i] >= 'a' && argv[0][i] <= 'z') continue;

            sprintf(buff,"login_err%c%d\n", SPLIT_SYMBOL, 3);
            send_message(client, buff);
            trace("Socket %d - User is trying to login as %s, but this name does not meet requirements", client->fd, argv[0]);
            return EXIT_FAILURE;
        }
    }

    struct client *logged_client = ht_get(server->players, argv[0]);
    if(logged_client)
    {
        if(logged_client->connected)
        {
            send_message(client, "login_err%c%d\n", SPLIT_SYMBOL, 4);
            trace("Socket %d - User is trying to login as %s, but this name is using", client->fd, logged_client->name);
            return EXIT_SUCCESS;
        }
        else
        {
            logged_client->fd = client->fd;
            server->clients[client->fd] = logged_client;
            free(client);
            client = logged_client;
            client->connected = CONNECTED;
            trace("Socket %d - User %s is logged bud disconnected, reconnecting", client->fd, logged_client->name);
        }
    }

    if(client_set_name(client, argv[0]))
    {
        char buff[64];
        struct client *opp = NULL;

        sprintf(buff, "login_ok%c%s%c%d\n", SPLIT_SYMBOL, client->name, SPLIT_SYMBOL, client->state);
        ht_put(server->players, client->name, client);
        send_message(client, buff);
        if(client->game && (client->state == STATE_IN_GAME || client->state == STATE_IN_GAME_PLAYING
        || client->state == STATE_IN_GAME_PREPARING))
        {
            if(client->game->player1 == client) {
                opp = client->game->player2;
            } else if(client->game->player2 == client) {
                opp = client->game->player1;
            }

            if(opp && opp->connected)
            {
                sprintf(buff, "room_join_opp%c%s\n", SPLIT_SYMBOL, client->name);
                send_message(opp, buff);
            }
        }
        trace("Socket %d - User %s logged in.", client->fd, client->name);
        return EXIT_SUCCESS;
    }

    send_message(client, "login_err%c%d\n", SPLIT_SYMBOL, 1);
    trace("Socket %d - Login internal error", client->fd, client->name);
    return EXIT_FAILURE;
}

int cmd_room_create(server *server, struct client *client, int argc, char **argv)
{
    if(client->state != STATE_IN_LOBBY) {
        send_message(client, "room_create_err%c%d\n", SPLIT_SYMBOL, 5);
        trace("Socket %d - room creation failed due wrong client state (%d)", client->fd, client->state);
        return EXIT_FAILURE;
    }

    room_create(server, client);
    client->state = STATE_IN_ROOM;

    //CREATE ROOM
    send_message(client, "room_create_ok\n");
    trace("Socket %d - room creation success (%d)", client->fd, client->game->id);
    return EXIT_SUCCESS;
}

int cmd_room_list(server *server, struct client *client, int argc, char **argv) {

    size_t buffer_size = 1024;
    size_t copied = 0;
    char *buffer = malloc(sizeof(char) * buffer_size);
    char *temp = malloc(sizeof(char) * 32);

    strcpy(buffer, "room_list_data");
    copied = strlen(buffer);

    int i = server->rooms->first;

    if(i == -1) {
        send_message(client, "room_list_data\n");
        trace("Socket %d - List of joinable rooms has been sent", client->fd);
        return EXIT_SUCCESS;
    }

    while(i != -1) {
        struct game *game = server->rooms->data[i];

        if(game->player2 != NULL && game->player1 != NULL) {
            i = server->rooms->next[i];
            continue;
        }

        struct client *player;
        if(game->player1 != NULL) {
            player = game->player1;
        } else if(game->player2 != NULL) {
            player = game->player2;
        } else {
            send_message(client, "room_list_err%c%d\n", SPLIT_SYMBOL, 1);
            trace("Socket %d - Failure due preparing list of free roams.", client->fd);
            return EXIT_FAILURE;
        }

        int size;
        size = sprintf(temp, "%d%c%s", i, SPLIT_SYMBOL, player->name);

         if(size > buffer_size - copied) {
            char *temp2 = malloc(buffer_size * 2 * sizeof(char));
            memcpy(temp2, buffer, buffer_size);
            buffer_size = 2 * buffer_size;
            free(buffer);
            buffer = temp2;

        }

        sprintf(buffer, "%s%c%s", buffer, SPLIT_SYMBOL, temp);

        copied += size;
        i = server->rooms->next[i];

    }

    sprintf(buffer, "%s\n", buffer);
    send_message(client, buffer);
    trace("Socket %d - List of joinable rooms has been sent", client->fd);

    free(temp);
    free(buffer);

    return EXIT_SUCCESS;
}

int cmd_room_join(server *server, struct client *client, int argc, char **argv) {

    int i;
    int length;

    if(client->state != STATE_IN_LOBBY)
    {
        send_message(client, "room_join_err%c%d\n", SPLIT_SYMBOL, 5);
        trace("Sock1et %d - Room join was rejected due to wrong client state (%d)", client->fd, client->state);
        return EXIT_FAILURE;
    }

    if(argc < 1) {
        send_message(client, "room_join_err%c%d\n", SPLIT_SYMBOL, 2);
        trace("Socket %d - Room joining failed due to wrong message format", client->fd);
        return EXIT_FAILURE;
    }

    length = strlen(argv[0]);
    if(length == 0) {
        send_message(client, "room_join_err%c%d\n", SPLIT_SYMBOL, 2);
        trace("Socket %d - Room joining failed due to wrong message format", client->fd);
        return EXIT_FAILURE;
    }


    for(i = 0; i < length; i++) {
        if(isdigit(argv[0][i]) == 0) {
            send_message(client, "room_join_err%c%d\n", SPLIT_SYMBOL, 2);
            trace("Socket %d - Room joining failed due to wrong message format", client->fd);
            return EXIT_FAILURE;
        }
    }

    int room_id = atoi(argv[0]);

    struct game *room = arraylist_get(server->rooms, room_id);

    if(!room) {
        send_message(client, "room_join_err%c%d\n", SPLIT_SYMBOL, 9);
        trace("Socket %d - Room joining failed due room is unavailable", client->fd);
        return EXIT_FAILURE;
    }

    if(room->state != GAME_STATE_LOBBY) {
        send_message(client, "room_join_err%c%d\n", SPLIT_SYMBOL, 6);
        trace("Socket %d - Room (%d) joining failed due room is full", client->fd, room->id);
        return EXIT_FAILURE;
    }

    struct client *opponent = NULL;

    if(room->player1 == NULL) {
        room->player1 = client;
        opponent = room->player2;
    } else if(room->player2 == NULL) {
        room->player2 = client;
        opponent = room->player1;
    } else {
        send_message(client, "room_join_err%c%d\n", SPLIT_SYMBOL, 6);
        trace("Socket %d - Room (%d) joining failed due room is full", client->fd, room->id);
        return EXIT_FAILURE;
    }

    client->game = room;
    client->state = STATE_IN_ROOM;
    char buf[64] = "";
    sprintf(buf, "room_join_ok%c%s\n", SPLIT_SYMBOL, opponent->name);
    send_message(client, buf);

    sprintf(buf, "room_join_opp%c%s\n", SPLIT_SYMBOL, client->name);
    send_message(opponent, buf);
    trace("Socket %d - Room (%d) join success.", client->fd, room->id);

    printf("Cleint %s state %d\n", client->name, client->state);
    if (room->player1->state == STATE_IN_ROOM && room->player2->state == STATE_IN_ROOM)
    {
        room->player1->state = STATE_IN_GAME_PREPARING;
        room->player2->state = STATE_IN_GAME_PREPARING;

        room->player1->playerNum = 1;
        room->player2->playerNum = 2;

        send_message(client, "game_conn\n");
        send_message(opponent, "game_conn\n");
        trace("Game %d - Room is full, game is starting", room->id);
        game_init(room);
    }

    return EXIT_SUCCESS;
}

int cmd_room_leave(server *server, struct client *client, int argc, char **argv) {

    char buf[64];
    if(client->state == STATE_IN_GAME || client->state == STATE_IN_GAME_PLAYING || client->state == STATE_IN_GAME_PREPARING)
    {

        struct client *opp = NULL;
        char * winner;

        if(client->game->player1 == client) {
            opp = client->game->player2;
            winner = client->name;
        } else if(client->game->player2 == client) {
            opp = client->game->player1;
            winner = client->name;
        }

        sprintf(buf, "room_leave_opp%c%s\n", SPLIT_SYMBOL, client->name);
        send_message(opp, buf);
        trace("Socket %d - Game %d was terminated due client leave", client->fd, client->game->id);
        game_end(server, client->game, winner);
    } else if(client->state == STATE_IN_ROOM) {
        struct client *opp = NULL;

        if (client->game->player1 == client) {
            client->game->player1 = NULL;
            opp = client->game->player2;
        }

        if (client->game->player2 == client) {
            client->game->player2 = NULL;
            opp = client->game->player1;
        }

        if (client->game->player1 == NULL && client->game->player2 == NULL) {
            arraylist_delete_item(server->rooms, client->game->id);
            free(client->game);
        }

        client->game = NULL;
        client->state = STATE_IN_LOBBY;
        send_message(client, "room_leave_ok\n");
        if (opp != NULL) {
            sprintf(buf, "room_leave_opp%c%s\n", SPLIT_SYMBOL, client->name);
            send_message(opp, buf);
        }
    } else {
        send_message(client, "room_leave_err%cnespravny stav uzivatele\n", SPLIT_SYMBOL);
        trace("Socket %d - Room leaving failed due wrong client state (%d)", client->fd, client->state);
        return EXIT_FAILURE;
    }

    trace("Socket %d - Client left the game/room", client->fd);
    return EXIT_SUCCESS;
}

int cmd_logout(server *server, struct client *client, int argc, char **argv) {

    char buf[64];

    if(client->connected == DISCONNECTED) {
        send_message(client, "logout_err%c%d\n", SPLIT_SYMBOL, 5);
        trace("Socket %d - Tried to disconnect when his state was DISCONNECTED", client->fd);
        return EXIT_FAILURE;
    }

    if(client->state == STATE_UNLOGGED) {
        server_close_connection(server, client->fd);
        send_message(client, "logout_ok\n");
        trace("Socket %d - Socket was disconnected at its request", client->fd);
        return EXIT_SUCCESS;
    }

    if(client->state == STATE_IN_ROOM) {
        if(client->game->player1 && client->game->player2) {

            struct client *opp = NULL;
            if(client->game->player1 == client) {
                opp = client->game->player2;
                client->game->player1 = NULL;
            } else if(client->game->player2 == client) {
                opp = client->game->player1;
                client->game->player2 = NULL;
            }

            sprintf(buf, "room_leave_opp%c%s", SPLIT_SYMBOL, client->name);
            send_message(opp, buf);
        } else {
            if(client->game->player1 == client || client->game->player2 == client) {
                arraylist_delete_item(server->rooms, client->game->id);
                free(client->game);
                client->game = NULL;
            }
        }
    } else if(client->state == STATE_IN_GAME || client->state == STATE_IN_GAME_PLAYING
    || client->state == STATE_IN_GAME_PREPARING) {

        struct client *opp = NULL;
        char* winner = NULL;

        if(client->game->player1 == client) {
            opp = client->game->player2;
            winner = client->name;
        } else if(client->game->player2 == client) {
            opp = client->game->player1;
            winner = client->name;
        }

        sprintf(buf, "room_leave_opp%c%s\n", SPLIT_SYMBOL, client->name);
        send_message(opp, buf);

        trace("Game %d - Game was ended due user (%d - %s) logout.", client->game->id, client->fd, client->name);

        game_end(server, client->game, winner);
    }

    ht_remove(server->players, client->name);
    send_message(client, "logout_ok\n");
    trace("Socket %d - Socket was disconnected at its request", client->fd);

    server_close_connection(server, client->fd);
    return EXIT_SUCCESS;
}


int cmd_game_prepared(server *server, struct client *client, int argc, char **argv) {
    int i, j;
    int x, y;
    char buff[64];

    printf("Klient %s state: %d\n", client->name, client->state);
    if (client->state != STATE_IN_GAME_PREPARING)
    {
        sprintf(buff,  "game_prepare_err%c%d\n", SPLIT_SYMBOL, 10);
        send_message(client, buff);
        trace("Socket %d - Tried to prepared game when not in right state", client->fd);
        return EXIT_FAILURE;
    }

    if(argc < 1) {
        sprintf(buff,  "game_prepare_err%c%d\n", SPLIT_SYMBOL, 2);
        send_message(client,buff);
        trace("Socket %d Game prepared failed due message format error", client->fd);
        return EXIT_FAILURE;
    }

    if (strlen(argv[0]) != GAME_BOARD_STRING_SIZE)
    {
        sprintf(buff,  "game_prepare_err%c%d\n", SPLIT_SYMBOL, 2);
        send_message(client, buff);
        trace("Socket %d Game prepared failed due message format error", client->fd);
        return EXIT_FAILURE;
    }

    //unsigned char board[SHIP_GAME_BOARD_SIZE][SHIP_GAME_BOARD_SIZE];

    x = 0;
    y = 0;
    for (i = 0; i < GAME_BOARD_STRING_SIZE; i++)
    {
        //TODO kontrola znaku
        // send_message(client, "game_prepare_err%c%d\n", SPLIT_SYMBOL, 2);
        if (argv[0][i]  == ',')
        {
            continue;
        }

        if (client->playerNum == 1)
        {
            client->game->player1_board[y][x] = argv[0][i];
        }
        else
        {
            client->game->player2_board[y][x] = argv[0][i];
        }

        x = x + 1;
        if (x >= SHIP_GAME_BOARD_SIZE)
        {
            y = y + 1;
            x = 0;
        }
    }

    if (client->playerNum == 1)
    {
        client->game->player1_prepare = 1;
    }
    else if (client->playerNum == 2)
    {
        client->game->player2_prepare = 1;
    }

    send_message(client, "game_prepared_ok\n");

    printf("Player 1 prepare %d, player 2 prepare %d \n", client->game->player1_prepare, client->game->player2_prepare);
    if (client->game->player1_prepare == 1 && client->game->player2_prepare == 1)
    {
        client->game->player1->state = STATE_IN_GAME_PLAYING;
        client->game->player2->state = STATE_IN_GAME;
        if (client->playerNum == 1)
        {
            send_message(client, "game_play\n");
        }
        else
        {
            send_message(client->game->player1, "game_play\n");
        }
    }

    return EXIT_SUCCESS;
}

int cmd_game_fire(server *server, struct client *client, int argc, char **argv)
{
    char buff[64];
    int x, y, i, j;
    unsigned char c;
    if(!client || !argv)
    {
        sprintf(buff, "game_fire_err%c%d\n", SPLIT_SYMBOL, 1);
        send_message(client, buff);
        trace("Socket %d - Game fire request filed due internal error", client->fd);
        return EXIT_FAILURE;
    }

    if(argc < 2) {
        sprintf(buff, "game_fire_err%c%d\n", SPLIT_SYMBOL, 2);
        send_message(client, buff);
        trace("Socket %d - Game fire request failed due message format error", client->fd);
        return EXIT_FAILURE;
    }

    x = atoi(argv[0]);
    y = atoi(argv[1]);


    printf("Player 1 %s board: \n", client->game->player1->name);
    for (i = 0; i < SHIP_GAME_BOARD_SIZE; i++)
    {
        for (j = 0; j < SHIP_GAME_BOARD_SIZE; j++)
        {
            printf("%c", client->game->player1_board[i][j]);
        }
        printf("\n");
    }


    if (x >= SHIP_GAME_BOARD_SIZE || x < 0 || y >= SHIP_GAME_BOARD_SIZE || y < 0)
    {
        //TODO doplnit cislo erroru
        sprintf(buff, "game_fire_err%c%d\n", SPLIT_SYMBOL, 2);
        send_message(client, buff);
        trace("Socket %d - Game fire request failed due message format error", client->fd);
        return EXIT_FAILURE;
    }

    if (client->playerNum == 1)
    {
        c = client->game->player2_board[y][x];
    }
    else
    {
        c = client->game->player1_board[y][x];
    }

    if (c == 'H' || c == 'M')
    {

        //TODO spravny error spatne policko
        sprintf(buff, "game_fire_err%c%d\n", SPLIT_SYMBOL, 2);
        send_message(client, buff);
        trace("Socket %d - Game fire request failed due message format error", client->fd);
        return EXIT_FAILURE;
    }

    if (c == 'E')
    {
        if (client->playerNum == 1)
        {
            client->game->player2_board[x][y] = 'M';
        }
        else
        {
            client->game->player1_board[x][y] = 'M';
        }

        sprintf(buff, "game_fire_ok%c%d%c%d%c%c\n", SPLIT_SYMBOL, x, SPLIT_SYMBOL, y, SPLIT_SYMBOL, 'M');
        send_message(client, buff);
        trace("Socket %d - Game fire request succeeded - position:[%d][%d], fire missed", client->fd, x, y);

        sprintf(buff, "game_opp_fire%c%d%c%d%c%c\n", SPLIT_SYMBOL, x, SPLIT_SYMBOL, y, SPLIT_SYMBOL, 'M');
        if (client->playerNum == 1)
        {
            send_message(client->game->player2, buff);
        }
        else
        {
            send_message(client->game->player1, buff);
        }
        return EXIT_SUCCESS;
    }

    if (isdigit(c))
    {
        if (client->playerNum == 1)
        {
            client->game->player2_board[x][y] = 'H';
        }
        else
        {
            client->game->player1_board[x][y] = 'H';
        }
        //TODO kontrola zda lod potopena

        sprintf(buff, "game_fire_ok%c%d%c%d%c%c\n", SPLIT_SYMBOL, x, SPLIT_SYMBOL, y, SPLIT_SYMBOL, 'H');
        send_message(client, buff);

        sprintf(buff, "game_opp_fire%c%d%c%d%c%c\n", SPLIT_SYMBOL, x, SPLIT_SYMBOL, y, SPLIT_SYMBOL, 'H');
        trace("Socket %d - Game fire request succeeded - position:[%d][%d], fire hit its target", client->fd, x, y);
        if (client->playerNum == 1)
        {
            send_message(client->game->player2, buff);
            //TODO poslat zpravu informujici o znicene lodi
            //send_message(client->game->player2, "game_ship_end%c%d%c%d%c", SPLIT_SYMBOL, SHIP ID);
        }
        else
        {
            send_message(client->game->player1, buff);
            //send_message(client->game->player2, "game_ship_end%c%d%c%d%c", SPLIT_SYMBOL, SHIP ID);
        }
        return EXIT_SUCCESS;
    }

    sprintf(buff, "game_fire_err%c%d\n", SPLIT_SYMBOL, 1);
    // NEZNAMA hodnota policka
    //TODO spravny error spatna hodnota policka
    send_message(client, buff);
    trace("Socket %d - Game fire request failed due to internal error", client->fd);
    return EXIT_FAILURE;
}