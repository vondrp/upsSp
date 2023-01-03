
#include "commands.h"
#include "../main.h"

#include "errors.h"

const int COMMANDS_COUNT = 9;
const cmd_handler COMMANDS[] =
    {
        {"login_req", cmd_login},
        {"room_create_req", cmd_room_create},
        {"room_list_req", cmd_room_list},
        {"room_join_req", cmd_room_join},
        {"room_leave_req", cmd_room_leave},
        {"game_prepared", cmd_game_prepared},
        {"game_fire_req", cmd_game_fire},
        {"game_info_req", cmd_game_info},
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
    int i;
    if(!client || !argv)
    {
        sprintf(buff,"login_err%c%d\n", SPLIT_SYMBOL, ERROR_INTERNAL);
        send_message(client, buff);
        trace("Socket %d - Login request filed due internal error", client->fd);
        disconnect_login_err(server,client);
        return EXIT_FAILURE;
    }


    if(argc < 1) {
        sprintf(buff,"login_err%c%d\n", SPLIT_SYMBOL, ERROR_FORMAT);
        send_message(client, buff);
        trace("Socket %d - Login request failed due message format error", client->fd);
        disconnect_login_err(server,client);
        return EXIT_FAILURE;
    }

    if (s_curr_players + 1 > s_max_players)
    {
        s_curr_players= s_curr_players + 1; // raise +1, because after failed login connection is closed and at the connection -> curr_player is reduced
        sprintf(buff,"login_err%c%d\n", SPLIT_SYMBOL, ERROR_INTERNAL);
        send_message(client, buff);
        trace("Socket %d - User is trying to login as %s, but max amount of players (%d) already reached", client->fd, argv[0], s_max_players);
        disconnect_login_err(server,client);
        return EXIT_FAILURE;
    }

    if (argv[0] == NULL)
    {
        sprintf(buff,"login_err%c%d\n", SPLIT_SYMBOL, ERROR_USERNAME_WRONG_FORMAT);
        send_message(client, buff);
        trace("Socket %d - User is trying to login as %s, but this name does not meet requirements", client->fd, argv[0]);
        disconnect_login_err(server,client);
        return EXIT_FAILURE;
    }

    if(strlen(argv[0]) == 0 || strlen(argv[0]) > NAME_MAX_LENGTH)
    {
        sprintf(buff,"login_err%c%d\n", SPLIT_SYMBOL, ERROR_USERNAME_WRONG_FORMAT);
        send_message(client, buff);
        trace("Socket %d - User is trying to login as %s, but this name does not meet requirements", client->fd, argv[0]);
        disconnect_login_err(server,client);
        return EXIT_FAILURE;
    }
    else
    {
        for(i = 0; i < strlen(argv[0]); i++) {
            if(argv[0][i] >= '0' && argv[0][i] <= '9') continue;
            if(argv[0][i] >= 'A' && argv[0][i] <= 'Z') continue;
            if(argv[0][i] >= 'a' && argv[0][i] <= 'z') continue;

            sprintf(buff,"login_err%c%d\n", SPLIT_SYMBOL, ERROR_USERNAME_WRONG_FORMAT);
            send_message(client, buff);
            trace("Socket %d - User is trying to login as %s, but this name does not meet requirements", client->fd, argv[0]);
            disconnect_login_err(server, client);
            return EXIT_FAILURE;
        }
    }

    struct client *logged_client = ht_get(server->players, argv[0]);
    if(logged_client)
    {
        if(logged_client->connected)
        {
            sprintf(buff, "login_err%c%d\n", SPLIT_SYMBOL, ERROR_USED_USERNAME);
            send_message(client, buff);;
            trace("Socket %d - User is trying to login as %s, but this name is using", client->fd, logged_client->name);
            disconnect_login_err(server, client);
            return EXIT_SUCCESS;
        }
        else
        {
            logged_client->fd = client->fd;
            server->clients[client->fd] = logged_client;
            free(client);
            client = logged_client;
            client->connected = CONNECTED;
            trace("Socket %d - User %s is logged but disconnected, reconnecting", client->fd, logged_client->name);
        }
    }

    if(client_set_name(client, argv[0]))
    {
        struct client *opp = NULL;

        s_curr_players = s_curr_players + 1;

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

    sprintf(buff,  "login_err%c%d\n", SPLIT_SYMBOL, ERROR_INTERNAL);
    send_message(client, buff);
    trace("Socket %d - Login internal error", client->fd, client->name);
    disconnect_login_err(server, client);
    return EXIT_FAILURE;
}

void disconnect_login_err(server *server, struct client *client)
{
    server->clients[client->fd] = client;
    trace("Socket %d disconnected, closing connection", client->fd);
    server_disconnect(server, client->fd);
}

int cmd_room_create(server *server, struct client *client, int argc, char **argv)
{
    char buff[64];
    if (s_curr_rooms + 1 > s_max_rooms)
    {
        sprintf(buff, "room_create_err%c%d\n", SPLIT_SYMBOL, ERROR_SERVER_LIMIT_REACHED);
        send_message(client, buff);
        trace("Socket %d - room creation failed due to max amount of rooms (%d) reached.", client->fd, s_max_rooms);
        return EXIT_SUCCESS;
    }

    if(client->state != STATE_IN_LOBBY)
    {
        sprintf(buff, "room_create_err%c%d\n", SPLIT_SYMBOL, ERROR_USER_STATE);
        send_message(client, buff);
        trace("Socket %d - room creation failed due to wrong client state (%d)", client->fd, client->state);
        return EXIT_FAILURE;
    }

    //CREATE ROOM
    room_create(server, client);
    client->state = STATE_IN_ROOM;

    send_message(client, "room_create_ok\n");
    trace("Socket %d - room creation success (%d)", client->fd, client->game->id);
    return EXIT_SUCCESS;
}

int cmd_room_list(server *server, struct client *client, int argc, char **argv) {

    size_t buffer_size = 1024;
    size_t copied;
    char *buffer = malloc(sizeof(char) * buffer_size);
    char *temp = malloc(sizeof(char) * 32);

    char buff[64]; //buffer used to error messages

    if(client->state != STATE_IN_LOBBY)
    {
        sprintf(buff,  "room_list_err%c%d\n", SPLIT_SYMBOL, ERROR_USER_STATE);
        send_message(client, buff);
        trace("Socket %d - Room list request fail - due to client state", client->fd);

        free(temp);
        free(buffer);
        return EXIT_FAILURE;
    }

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
            sprintf(buff,  "room_list_err%c%d\n", SPLIT_SYMBOL, ERROR_INTERNAL);
            send_message(client, buff);
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

    char buff[64];
    if(client->state != STATE_IN_LOBBY)
    {
        sprintf(buff,  "room_join_err%c%d\n", SPLIT_SYMBOL, ERROR_USER_STATE);
        send_message(client, buff);
        trace("Sock1et %d - Room join was rejected due to wrong client state (%d)", client->fd, client->state);
        return EXIT_FAILURE;
    }

    if(argc < 1) {
        sprintf(buff, "room_join_err%c%d\n", SPLIT_SYMBOL, ERROR_FORMAT);
        send_message(client, buff);
        trace("Socket %d - Room joining failed due to wrong message format", client->fd);
        return EXIT_FAILURE;
    }

    if (argv[0] == NULL)
    {
        sprintf(buff,  "room_join_err%c%d\n", SPLIT_SYMBOL, ERROR_FORMAT);
        send_message(client, buff);
        trace("Socket %d - Room joining failed due to wrong message format", client->fd);
        return EXIT_FAILURE;
    }

    length = (int) strlen(argv[0]);
    if(length == 0) {
        sprintf(buff,  "room_join_err%c%d\n", SPLIT_SYMBOL, ERROR_FORMAT);
        send_message(client, buff);
        trace("Socket %d - Room joining failed due to wrong message format", client->fd);
        return EXIT_FAILURE;
    }


    for(i = 0; i < length; i++) {
        if(isdigit(argv[0][i]) == 0) {
            sprintf(buff,"room_join_err%c%d\n", SPLIT_SYMBOL, ERROR_FORMAT);
            send_message(client, buff);
            trace("Socket %d - Room joining failed due to wrong message format", client->fd);
            return EXIT_FAILURE;
        }
    }

    int room_id = atoi(argv[0]);

    struct game *room = arraylist_get(server->rooms, room_id);

    if (room == NULL)
    {
        sprintf(buff, "room_join_err%c%d\n", SPLIT_SYMBOL, ERROR_ROOM_NOT_ACCESSIBLE);
        send_message(client, buff);
        trace("Socket %d - Room joining failed due room is unavailable", client->fd);
        return EXIT_FAILURE;
    }

    if(room->state == GAME_STATE_ERASED)
    {
        sprintf(buff, "room_join_err%c%d\n", SPLIT_SYMBOL, ERROR_ROOM_NOT_ACCESSIBLE);
        send_message(client, buff);
        trace("Socket %d - Room joining failed due room is unavailable", client->fd);
        return EXIT_FAILURE;
    }

    if(room->state != GAME_STATE_LOBBY) {
        sprintf(buff, "room_join_err%c%d\n", SPLIT_SYMBOL, ERROR_ROOM_FULL);
        send_message(client, buff);
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
        sprintf(buff, "room_join_err%c%d\n", SPLIT_SYMBOL, ERROR_ROOM_FULL);
        send_message(client, buff);
        trace("Socket %d - Room (%d) joining failed due room is full", client->fd, room->id);
        return EXIT_FAILURE;
    }

    client->game = room;
    client->state = STATE_IN_ROOM;

    sprintf(buff, "room_join_ok%c%s\n", SPLIT_SYMBOL, opponent->name);
    send_message(client, buff);

    sprintf(buff, "room_join_opp%c%s\n", SPLIT_SYMBOL, client->name);
    send_message(opponent, buff);
    trace("Socket %d - Room (%d) join success.", client->fd, room->id);

    if (room->player1->state == STATE_IN_ROOM && room->player2->state == STATE_IN_ROOM)
    {
        room->player1->state = STATE_IN_GAME_PREPARING;
        room->player2->state = STATE_IN_GAME_PREPARING;

        room->player1->playerNum = 1;
        room->player2->playerNum = 2;

        room->state = GAME_STATE_GAME;
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
        } else {
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

        if (client->game->player1 == NULL && client->game->player2 == NULL)
        {
            client->game->state = GAME_STATE_ERASED;
            arraylist_delete_item(server->rooms, client->game->id);
            s_curr_rooms = s_curr_rooms - 1;
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
        sprintf(buf,  "room_leave_err%c%d\n", SPLIT_SYMBOL, ERROR_USER_STATE);
        send_message(client, buf);
        trace("Socket %d - Room leaving failed due wrong client state (%d)", client->fd, client->state);
        return EXIT_FAILURE;
    }

    trace("Socket %d - Client left the game/room", client->fd);
    return EXIT_SUCCESS;
}

int cmd_logout(server *server, struct client *client, int argc, char **argv) {

    char buff[64];

    if(client->connected == DISCONNECTED) {
        sprintf(buff,"logout_err%c%d\n", SPLIT_SYMBOL, ERROR_USER_STATE);
        send_message(client, buff);
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

            sprintf(buff, "room_leave_opp%c%s", SPLIT_SYMBOL, client->name);
            send_message(opp, buff);
        } else {
            if(client->game->player1 == client || client->game->player2 == client)
            {
                client->game->state = GAME_STATE_ERASED;
                arraylist_delete_item(server->rooms, client->game->id);
                s_curr_rooms = s_curr_rooms - 1;

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

        sprintf(buff, "room_leave_opp%c%s\n", SPLIT_SYMBOL, client->name);
        send_message(opp, buff);

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
    int i;
    int x, y;
    int ship_number;
    char buff[64];
    unsigned char check_char;
    int check_neigh_ship = 0;
    int ship_neighbor;

    if (client->state != STATE_IN_GAME_PREPARING)
    {
        sprintf(buff,  "game_prepared_err%c%d\n", SPLIT_SYMBOL, ERROR_USER_STATE);
        send_message(client, buff);
        trace("Socket %d - Tried to prepared game when not in right state", client->fd);
        return EXIT_FAILURE;
    }

    if(argc < 1) {
        sprintf(buff,  "game_prepared_err%c%d\n", SPLIT_SYMBOL, ERROR_FORMAT);
        send_message(client,buff);
        trace("Socket %d Game prepared failed due message format error", client->fd);
        return EXIT_FAILURE;
    }

    if (argv[0] == NULL)
    {
        sprintf(buff,  "game_prepared_err%c%d\n", SPLIT_SYMBOL, ERROR_FORMAT);
        send_message(client, buff);
        trace("Socket %d Game prepared failed due message format error", client->fd);
        return EXIT_FAILURE;
    }

    if (strlen(argv[0]) != GAME_BOARD_STRING_SIZE) // can be bigger than game board string size -> there is +1 for \n of klient
    {
        sprintf(buff,  "game_prepared_err%c%d\n", SPLIT_SYMBOL, ERROR_FORMAT);
        send_message(client, buff);
        trace("Socket %d Game prepared failed due message format error", client->fd);
        return EXIT_FAILURE;
    }

    x = 0;
    y = 0;
    for (i = 0; i < GAME_BOARD_STRING_SIZE; i++)
    {
        ship_number = -1;

        if (isdigit(argv[0][i]))
        {
            ship_number = argv[0][i] - '0';
            if (ship_number < 0 || ship_number >= AMOUNT_OF_SHIP)
            {
                clean_client_ships(client, client->game);
                sprintf(buff, "game_prepared_err%c%d\n", SPLIT_SYMBOL, ERROR_SHIP_NUMBER);
                send_message(client, buff);
                trace("Socket %d - Game prepare request failed due message format error", client->fd);
                return EXIT_FAILURE;
            }
        }
        else
        {
            if (argv[0][i] != 'E')
            {
                clean_client_ships(client, client->game);
                sprintf(buff, "game_prepared_err%c%d\n", SPLIT_SYMBOL, ERROR_FORMAT);
                send_message(client, buff);
                trace("Socket %d - Game prepare request failed due message format error", client->fd);
                return EXIT_FAILURE;
            }
        }

        if (client->playerNum == 1)
        {
            if (ship_number != -1)
            {
                check_neigh_ship = 0;
                // checking if around the given position is not another ship
                if (y > 0)
                {
                    check_char = client->game->player1_board[y - 1][x];
                    if (isdigit(check_char))
                    {
                        ship_neighbor = check_char - '0';
                        if (ship_neighbor != ship_number)
                        {
                            check_neigh_ship = 1;
                        }
                    }
                }

                if (x > 0)
                {
                    check_char = client->game->player1_board[y][x - 1];
                    if (isdigit(check_char))
                    {
                        ship_neighbor = check_char - '0';
                        if (ship_neighbor != ship_number)
                        {
                            check_neigh_ship = 1;
                        }
                    }
                }

                if (x > 0 && y > 0)
                {
                    check_char = client->game->player1_board[y - 1][x - 1];
                    if (isdigit(check_char))
                    {
                        ship_neighbor = check_char - '0';
                        if (ship_neighbor != ship_number)
                        {
                            check_neigh_ship = 1;
                        }
                    }
                }

                if (x < (SHIP_GAME_BOARD_SIZE - 1) && y > 0)
                {
                    check_char = client->game->player1_board[y - 1][x + 1];
                    if (isdigit(check_char))
                    {
                        ship_neighbor = check_char - '0';
                        if (ship_neighbor != ship_number)
                        {
                            check_neigh_ship = 1;
                        }
                    }
                }

                if (check_neigh_ship == 1)
                {
                    clean_client_ships(client, client->game);
                    sprintf(buff, "game_prepared_err%c%d\n", SPLIT_SYMBOL, ERROR_SHIP_PLACEMENT);
                    send_message(client, buff);
                    trace("Socket %d - Game prepare request failed due to two ships placed next to each other.", client->fd);
                    return EXIT_FAILURE;
                }

                if (ship_place(&client->game->player1Ships[ship_number], x, y) == -1)
                {
                    clean_client_ships(client, client->game);
                    sprintf(buff, "game_prepared_err%c%d\n", SPLIT_SYMBOL, ERROR_SHIP_PLACEMENT);
                    send_message(client, buff);
                    trace("Socket %d - Game prepare request failed due to ships part wrong placement", client->fd);
                    return EXIT_FAILURE;
                }
            }

            client->game->player1_board[y][x] = argv[0][i];
        }
        else
        {
            if (ship_number != -1)
            {
                check_neigh_ship = 0;
                // checking if around the given position is not another ship
                if (y > 0)
                {
                    check_char = client->game->player2_board[y - 1][x];
                    if (isdigit(check_char))
                    {
                        ship_neighbor = check_char - '0';
                        if (ship_neighbor != ship_number)
                        {
                            check_neigh_ship = 1;
                        }
                    }
                }

                if (x > 0)
                {
                    check_char = client->game->player2_board[y][x - 1];
                    if (isdigit(check_char))
                    {
                        ship_neighbor = check_char - '0';
                        if (ship_neighbor != ship_number)
                        {
                            check_neigh_ship = 1;
                        }
                    }
                }

                if (x > 0 && y > 0)
                {
                    check_char = client->game->player2_board[y - 1][x - 1];
                    if (isdigit(check_char))
                    {
                        ship_neighbor = check_char - '0';
                        if (ship_neighbor != ship_number)
                        {
                            check_neigh_ship = 1;
                        }
                    }
                }

                if (x < (SHIP_GAME_BOARD_SIZE - 1) && y > 0)
                {
                    check_char = client->game->player2_board[y - 1][x + 1];
                    if (isdigit(check_char))
                    {
                        ship_neighbor = check_char - '0';
                        if (ship_neighbor != ship_number)
                        {
                            check_neigh_ship = 1;
                        }
                    }
                }

                if (check_neigh_ship == 1)
                {
                    clean_client_ships(client, client->game);
                    sprintf(buff, "game_prepared_err%c%d\n", SPLIT_SYMBOL, ERROR_SHIP_PLACEMENT);
                    send_message(client, buff);
                    trace("Socket %d - Game prepare request failed due to two ships placed next to each other.", client->fd);
                    return EXIT_FAILURE;
                }

                if (ship_place(&client->game->player2Ships[ship_number], x, y) == -1)
                {
                    clean_client_ships(client, client->game);
                    sprintf(buff, "game_prepared_err%c%d\n", SPLIT_SYMBOL, ERROR_SHIP_PLACEMENT);
                    send_message(client, buff);
                    trace("Socket %d - Game prepare request failed due to ships part wrong placement", client->fd);
                    return EXIT_FAILURE;
                }
            }

            client->game->player2_board[y][x] = argv[0][i];
        }

        x = x + 1;
        if (x >= SHIP_GAME_BOARD_SIZE)
        {
            y = y + 1;
            x = 0;
        }
    }

    // checking if ships length is at it should be
    if (client->playerNum == 1)
    {
        if (client->game->player1Ships[0].length != SHIP_0_L ||
            client->game->player1Ships[1].length != SHIP_1_L ||
            client->game->player1Ships[2].length != SHIP_2_L ||
            client->game->player1Ships[3].length != SHIP_3_L ||
            client->game->player1Ships[4].length != SHIP_4_L ||
            client->game->player1Ships[5].length != SHIP_5_L ||
            client->game->player1Ships[6].length != SHIP_6_L
            )
        {
            clean_client_ships(client, client->game);
            sprintf(buff, "game_prepared_err%c%d\n", SPLIT_SYMBOL, ERROR_SHIPS_WRONG_LENGTH);
            send_message(client, buff);
            trace("Socket %d - Game prepare request failed due to wrong length of ships.", client->fd);
            return EXIT_FAILURE;
        }
    }
    else
    {
        if (client->game->player2Ships[0].length != SHIP_0_L ||
            client->game->player2Ships[1].length != SHIP_1_L ||
            client->game->player2Ships[2].length != SHIP_2_L ||
            client->game->player2Ships[3].length != SHIP_3_L ||
            client->game->player2Ships[4].length != SHIP_4_L ||
            client->game->player2Ships[5].length != SHIP_5_L ||
            client->game->player2Ships[6].length != SHIP_6_L
                )
        {
            clean_client_ships(client, client->game);
            sprintf(buff, "game_prepared_err%c%d\n", SPLIT_SYMBOL, ERROR_SHIP_PLACEMENT);
            send_message(client, buff);
            trace("Socket %d - Game prepare request failed due to wrong length of ships.", client->fd);
            return EXIT_FAILURE;
        }
    }

    // CLIENT IS prepared
    client->state = STATE_IN_GAME;
    send_message(client, "game_prepared_ok\n");

    if (client->game->player1->state == STATE_IN_GAME && client->game->player2->state == STATE_IN_GAME)
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
    int x, y;
    unsigned char c;
    int ship_number;
    int ship_destroyed = 0;
    if(!client || !argv)
    {
        sprintf(buff, "game_fire_err%c%d\n", SPLIT_SYMBOL, ERROR_INTERNAL);
        send_message(client, buff);
        trace("Socket %d - Game fire request failed due internal error", client->fd);
        return EXIT_FAILURE;
    }

    if (client->state != STATE_IN_GAME_PLAYING)
    {
        sprintf(buff,  "game_fire_err%c%d\n", SPLIT_SYMBOL, ERROR_USER_STATE);
        send_message(client, buff);
        trace("Socket %d - Tried to fire when not in the right state", client->fd);
        return EXIT_FAILURE;
    }

    if(argc < 2) {
        sprintf(buff, "game_fire_err%c%d\n", SPLIT_SYMBOL, ERROR_FORMAT);
        send_message(client, buff);
        trace("Socket %d - Game fire request failed due message format error", client->fd);
        return EXIT_FAILURE;
    }

    if (argv[0]  == NULL || argv[1] == NULL)
    {
        sprintf(buff, "game_fire_err%c%d\n", SPLIT_SYMBOL, ERROR_FORMAT);
        send_message(client, buff);
        trace("Socket %d - Game fire request failed due message format error", client->fd);
        return EXIT_FAILURE;
    }

    if (strlen(argv[0]) == 0 || strlen(argv[1]) == 0)
    {
        sprintf(buff, "game_fire_err%c%d\n", SPLIT_SYMBOL, ERROR_FORMAT);
        send_message(client, buff);
        trace("Socket %d - Game fire request failed due message format error", client->fd);
        return EXIT_FAILURE;
    }
    int i;
    for (i = 0; i < strlen(argv[0]); i++)
    {
        if (!isdigit(argv[0][i]))
        {
            sprintf(buff, "game_fire_err%c%d\n", SPLIT_SYMBOL, ERROR_FORMAT);
            send_message(client, buff);
            trace("Socket %d - Game fire request failed due message format error", client->fd);
            return EXIT_FAILURE;
        }
    }

    for (i = 0; i < strlen(argv[1]); i++)
    {
        if (!isdigit(argv[1][i]))
        {
            sprintf(buff, "game_fire_err%c%d\n", SPLIT_SYMBOL, ERROR_FORMAT);
            send_message(client, buff);
            trace("Socket %d - Game fire request failed due message format error", client->fd);
            return EXIT_FAILURE;
        }
    }


    x = atoi(argv[0]);
    y = atoi(argv[1]);

    if (x >= SHIP_GAME_BOARD_SIZE || x < 0 || y >= SHIP_GAME_BOARD_SIZE || y < 0)
    {
        sprintf(buff, "game_fire_err%c%d\n", SPLIT_SYMBOL, ERROR_OUT_OF_PLAY_FIELD);
        send_message(client, buff);
        trace("Socket %d - Game fire request failed. Given position out of game board.", client->fd);
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
        sprintf(buff, "game_fire_err%c%d\n", SPLIT_SYMBOL, ERROR_ALREADY_HIT);
        send_message(client, buff);
        trace("Socket %d - Game fire request failed due to position being already hit.", client->fd);
        return EXIT_FAILURE;
    }

    if (c == 'E')
    {
        if (client->playerNum == 1)
        {
            client->game->player2_board[y][x] = 'M';
        }
        else
        {
            client->game->player1_board[y][x] = 'M';
        }

        sprintf(buff, "game_fire_ok%c%d%c%d%c%c%c%d\n", SPLIT_SYMBOL, x, SPLIT_SYMBOL, y, SPLIT_SYMBOL, 'M', SPLIT_SYMBOL, ship_destroyed);
        send_message(client, buff);
        trace("Socket %d - Game fire request succeeded - position:[%d][%d], fire missed", client->fd, x, y);

        sprintf(buff, "game_opp_fire%c%d%c%d%c%c%c%d\n", SPLIT_SYMBOL, x, SPLIT_SYMBOL, y, SPLIT_SYMBOL, 'M',  SPLIT_SYMBOL, ship_destroyed);
        if (client->playerNum == 1)
        {
            client->state = STATE_IN_GAME;
            client->game->player2->state = STATE_IN_GAME_PLAYING;
            send_message(client->game->player2, buff);
        }
        else
        {
            client->state = STATE_IN_GAME;
            client->game->player1->state = STATE_IN_GAME_PLAYING;
            send_message(client->game->player1, buff);
        }
        return EXIT_SUCCESS;
    }

    if (isdigit(c))
    {
        ship_number = c - '0';

        if (client->playerNum == 1)
        {
            client->game->player2_board[y][x] = 'H';
            ship_destroyed = ship_hit(&client->game->player2Ships[ship_number]);

            if (ship_destroyed == 1)
            {
                mark_destroyed_ship(client->game, &client->game->player2Ships[ship_number], 2);
                client->game->p1_count++;
            }
        }
        else
        {
            client->game->player1_board[y][x] = 'H';
            ship_destroyed = ship_hit(&client->game->player1Ships[ship_number]);

            if (ship_destroyed == 1)
            {
                mark_destroyed_ship(client->game, &client->game->player1Ships[ship_number], 1);
                client->game->p2_count++;
            }
        }

        sprintf(buff, "game_fire_ok%c%d%c%d%c%c%c%d\n", SPLIT_SYMBOL, x, SPLIT_SYMBOL, y, SPLIT_SYMBOL, 'H', SPLIT_SYMBOL, ship_destroyed);
        send_message(client, buff);

        if (ship_destroyed == 1)
        {
            trace("Socket %d - Game fire request succeeded - position:[%d][%d], fire hit its target, ship %d is destroyed", client->fd, x, y, ship_number);
        }
        else
        {
            trace("Socket %d - Game fire request succeeded - position:[%d][%d], fire hit its target, ship %d is still alive", client->fd, x, y, ship_number);
        }

        sprintf(buff, "game_opp_fire%c%d%c%d%c%c%c%d\n", SPLIT_SYMBOL, x, SPLIT_SYMBOL, y, SPLIT_SYMBOL, 'H', SPLIT_SYMBOL, ship_destroyed);
        if (client->playerNum == 1)
        {
            client->state = STATE_IN_GAME;
            client->game->player2->state = STATE_IN_GAME_PLAYING;
            send_message(client->game->player2, buff);
        }
        else
        {
            client->state = STATE_IN_GAME;
            client->game->player1->state = STATE_IN_GAME_PLAYING;
            send_message(client->game->player1, buff);
        }

        // check if current player win

        if (client->game->p1_count >= AMOUNT_OF_SHIP || client->game->p2_count >= AMOUNT_OF_SHIP)
            game_end(server, client->game, client->name);

        return EXIT_SUCCESS;
    }

    // NEZNAMA hodnota policka - nemelo by se stat - kontrola v prepare game
    sprintf(buff, "game_fire_err%c%d\n", SPLIT_SYMBOL, ERROR_INTERNAL);
    send_message(client, buff);
    trace("Socket %d - Game fire request failed due to internal error", client->fd);
    return EXIT_FAILURE;
}

int cmd_game_info(server *server, struct client *client, int argc, char **argv)
{
    int i, j;
    struct client *opp = NULL;
    //char buff[30 + (NAME_MAX_LENGTH * 2) + (SHIP_GAME_BOARD_SIZE * SHIP_GAME_BOARD_SIZE) * 2];

    char buff[250];
    unsigned char client_board[GAME_BOARD_STRING_SIZE];
    unsigned char opponent_board[GAME_BOARD_STRING_SIZE];

    if(client->state != STATE_IN_GAME_PLAYING && client->state != STATE_IN_GAME
    && client->state != STATE_IN_GAME_PREPARING
    ) {
        sprintf(buff,  "game_info_err%c%d\n", SPLIT_SYMBOL, ERROR_USER_STATE);
        send_message(client, buff);
        trace("Socket %d - User game info was rejected due to wrong client state (%d)", client->fd, client->state);
        return EXIT_FAILURE;
    }

    if(client->game->player1 == client)
    {
        opp = client->game->player2;
        for (i = 0; i < SHIP_GAME_BOARD_SIZE; i++)
        {
            for (j = 0; j < SHIP_GAME_BOARD_SIZE; j++)
            {

                if (isdigit(client->game->player2_board[i][j]))
                {
                    opponent_board[i * SHIP_GAME_BOARD_SIZE + j] = 'E';
                }
                else
                {
                    opponent_board[i * SHIP_GAME_BOARD_SIZE + j] = client->game->player2_board[i][j];
                }
                client_board[i * SHIP_GAME_BOARD_SIZE + j] = client->game->player1_board[i][j];
            }
        }

    }
    else if(client->game->player2 == client)
    {
        opp = client->game->player1;
        for (i = 0; i < SHIP_GAME_BOARD_SIZE; i++)
        {
            for (j = 0; j < SHIP_GAME_BOARD_SIZE; j++)
            {
                if (isdigit(client->game->player1_board[i][j]))
                {
                    opponent_board[i * SHIP_GAME_BOARD_SIZE + j] = 'E';
                }
                else
                {
                    opponent_board[i * SHIP_GAME_BOARD_SIZE + j] = client->game->player1_board[i][j];
                }

                client_board[i * SHIP_GAME_BOARD_SIZE + j] = client->game->player2_board[i][j];
            }
        }
    }

    sprintf(buff, "game_info_data%c%d%c%s%c%s%c%s\n", SPLIT_SYMBOL,
            client->state, SPLIT_SYMBOL, client_board, SPLIT_SYMBOL,
            opp->name, SPLIT_SYMBOL, opponent_board);

    send_message(client, buff);
    return EXIT_SUCCESS;
}