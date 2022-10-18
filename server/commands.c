
#include "commands.h"

const int COMMANDS_COUNT = 9;
const cmd_handler COMMANDS[] = {
        {"login_req", cmd_login},
        {"room_create_req", cmd_room_create},
        {"room_list_req", cmd_room_list},
        {"room_join_req", cmd_room_join},
        {"room_leave_req", cmd_room_leave},
        {"game_info_req", cmd_game_info},
        {"game_move_req", cmd_move},
        {"game_jump_req", cmd_jump},
        {"logout_req", cmd_logout}
};

fcmd get_handler(char command[]) {
    int i;

    int command_length = strlen(command);

    for(i = 0; i < command_length; i++) {
        command[i] = tolower(command[i]);
    }

    for(i = 0; i < COMMANDS_COUNT; ++i) {
        if(!strcmp(COMMANDS[i].cmd, command)) {
            return COMMANDS[i].handler;
        }
    }

    return NULL;
}

int cmd_login(server *server, struct client *client, int argc, char **argv) {
    if(!client || !argv) {
        send_message(client, "login_err|1\n");
        trace("Socket %d - Login request filed due internal error", client->fd);
        return EXIT_FAILURE;
    }

    if(argc < 1) {
        send_message(client, "login_err|2\n");
        trace("Socket %d - Login request failed due message format error", client->fd);
        return EXIT_FAILURE;
    }

    if(strlen(argv[0]) == 0 || strlen(argv[0]) > 20) {
        send_message(client, "login_err|3\n");
        trace("Socket %d - User is trying to login as %s, but this name does not meet requirements", client->fd, argv[0]);
        return EXIT_FAILURE;
    } else {
        int i;
        for(i = 0; i < strlen(argv[0]); i++) {
            if(argv[0][i] >= '0' && argv[0][i] <= '9') continue;
            if(argv[0][i] >= 'A' && argv[0][i] <= 'Z') continue;
            if(argv[0][i] >= 'a' && argv[0][i] <= 'z') continue;

            send_message(client, "login_err|3\n");
            trace("Socket %d - User is trying to login as %s, but this name does not meet requirements", client->fd, argv[0]);
            return EXIT_FAILURE;
        }
    }

    struct client *logged_client = ht_get(server->players, argv[0]);
    if(logged_client) {
        if(logged_client->connected) {
            send_message(client, "login_err|4\n");
            trace("Socket %d - User is trying to login as %s, but this name is using", client->fd, logged_client->name);
            return EXIT_SUCCESS;
        } else {
            logged_client->fd = client->fd;
            server->clients[client->fd] = logged_client;
            free(client);
            client = logged_client;
            client->connected = CONNECTED;
            trace("Socket %d - User %s is logged bud disconnected, reconnecting", client->fd, logged_client->name);
        }
    }

    if(client_set_name(client, argv[0])) {
        char buff[64];
        sprintf(buff, "login_ok|%s|%d\n", client->name, client->state);
        ht_put(server->players, client->name, client);
        send_message(client, buff);
        if(client->game && (client->state == STATE_IN_GAME || client->state == STATE_IN_GAME_PLAYING)) {
            struct client *opp;
            if(client->game->player1 == client) {
                opp = client->game->player2;
            } else if(client->game->player2 == client) {
                opp = client->game->player1;
            }

            if(opp && opp->connected) {
                char buf[64] = "";
                sprintf(buf, "room_join_opp|%s\n\0", client->name);
                send_message(opp, buf);
            }


        }
        trace("Socket %d - User %s logged in.", client->fd, client->name);
        return EXIT_SUCCESS;
    }

    send_message(client, "login_err|1\n");
    trace("Socket %d - Login internal error", client->fd, client->name);
    return EXIT_FAILURE;
}

int cmd_room_create(server *server, struct client *client, int argc, char **argv) {

    if(client->state != STATE_IN_LOBBY) {
        send_message(client, "room_create_err|5\n");
        trace("Socket %d - room creation failed due wrong client state (%d)", client->fd, client->state);
        return EXIT_FAILURE;
    }

    room_create(server, client);
    client->state = 2;

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
            send_message(client, "room_list_err|1\n");
            trace("Socket %d - Failure due preparing list of free roams.", client->fd);
            return EXIT_FAILURE;
        }

        int size;
        size = sprintf(temp, "%d|%s", i, player->name, "%s");

         if(size > buffer_size - copied) {
            char *temp2 = malloc(buffer_size * 2 * sizeof(char));
            memcpy(temp2, buffer, buffer_size);
            buffer_size = 2 * buffer_size;
            free(buffer);
            buffer = temp2;

        }

        sprintf(buffer, "%s|%s", buffer, temp);

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

    if(client->state != 1) {
        send_message(client, "room_join_err|5\n");
        trace("Sock1et %d - Room join was rejected due to wrong client state (%d)", client->fd, client->state);
        return EXIT_FAILURE;
    }

    if(argc < 1) {
        send_message(client, "room_join_err|2\n");
        trace("Socket %d - Room joining failed due to wrong message format", client->fd);
        return EXIT_FAILURE;
    }

    int length = strlen(argv[0]);
    if(length == 0) {
        send_message(client, "room_join_err|2\n");
        trace("Socket %d - Room joining failed due to wrong message format", client->fd);
        return EXIT_FAILURE;
    }

    int i;
    for(i = 0; i < length; i++) {
        if(isdigit(argv[0][i]) == 0) {
            send_message(client, "room_join_err|2\n");
            trace("Socket %d - Room joining failed due to wrong message format", client->fd);
            return EXIT_FAILURE;
        }
    }

    int room_id = atoi(argv[0]);

    struct game *room = arraylist_get(server->rooms, room_id);

    if(!room) {
        send_message(client, "room_join_err|9\n");
        trace("Socket %d - Room joining failed due room is unavailable", client->fd);
        return EXIT_FAILURE;
    }

    if(room->state != GAME_STATE_LOBBY) {
        send_message(client, "room_join_err|6\n");
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
        send_message(client, "room_join_err|6\n");
        trace("Socket %d - Room (%d) joining failed due room is full", client->fd, room->id);
        return EXIT_FAILURE;
    }

    client->game = room;
    client->state = STATE_IN_ROOM;
    char buf[64] = "";
    sprintf(buf, "room_join_ok|%s\n", opponent->name);
    send_message(client, buf);

    sprintf(buf, "room_join_opp|%s\n", client->name);
    send_message(opponent, buf);
    trace("Socket %d - Room (%d) join success.", client->fd, room->id);

    room->player1->state = STATE_IN_GAME;
    room->player2->state = STATE_IN_GAME;

    if(client == room->player1) {
        send_message(client, "game_start|white\n");
        send_message(opponent, "game_start|black\n");
        trace("Game %d - Room is full, game is starting", room->id);
    } else if (client == room->player2){
        send_message(client, "game_start|black\n");
        send_message(opponent, "game_start|white\n");
        trace("Game %d - Room is full, game is starting", room->id);
    }

    game_init(room);
    room->player1->state = STATE_IN_GAME_PLAYING;
    send_message(room->player1, "game_turn\n");
    return EXIT_SUCCESS;
}

int cmd_room_leave(server *server, struct client *client, int argc, char **argv) {

    char buf[64];
    if(client->state == STATE_IN_GAME || client->state == STATE_IN_GAME_PLAYING) {

        struct client *opp = NULL;
        signed char winner = 0;

        if(client->game->player1 == client) {
            opp = client->game->player2;
            winner = -1;
        } else if(client->game->player2 == client) {
            opp = client->game->player1;
            winner = 1;
        }

        sprintf(buf, "room_leave_opp|%s\n", client->name);
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
            arraylist_delete(server->rooms, client->game->id);
            free(client->game);
        }

        client->game = NULL;
        client->state = STATE_IN_LOBBY;
        send_message(client, "room_leave_ok\n");
        if (opp != NULL) {
            sprintf(buf, "room_leave_opp|%s\n", client->name);
            send_message(opp, buf);
        }
    } else {
        send_message(client, "room_leave_err|nespravny stav uzivatele\n");
        trace("Socket %d - Room leaving failed due wrong client state (%d)", client->fd, client->state);
        return EXIT_FAILURE;
    }

    trace("Socket %d - Client left the game/room", client->fd);
    return EXIT_SUCCESS;
}

int cmd_game_info(server *server, struct client *client, int argc, char **argv) {
    if(client->state != STATE_IN_GAME_PLAYING && client->state != STATE_IN_GAME) {
        send_message(client, "game_info_err|5\n");
        trace("Sock1et %d - User game info was rejected due to wrong client state (%d)", client->fd, client->state);
        return EXIT_FAILURE;
    }

    struct client *opp = NULL;
    char color[6] = "";
    if(client->game->player1 == client) {
        opp = client->game->player2;
        sprintf(color, "white");
    } else if(client->game->player2 == client) {
        opp = client->game->player1;
        sprintf(color, "black");
    }

    int playing = 0;
    if(client->state == STATE_IN_GAME_PLAYING) {
        playing = 1;
    }

    char message[256];
    sprintf(message, "game_info_data|%s|%d|%s", color, playing, opp->name);
    int i, j;
    for(i = 0; i < 8; i++) {
        for(j = 0; j < 8; j++) {
            if(client->game->board[i][j] == 0) continue;
            int figure = client->game->board[i][j];

            if(figure < 0) {
                figure += 5;
            }

            sprintf(message, "%s|%d%d%d", message, i, j, figure);
        }
    }

    message[strlen(message)] = '\n';
    message[strlen(message) + 1] = '\0';
    send_message(client, message);
    trace("Socket %d - Game information about game %d was sent.", client->fd, client->game->id);

    return EXIT_SUCCESS;
}

int cmd_move(server *server, struct client *client, int argc, char **argv) {
    print_board(client->game);

    if(argc < 2) {
        send_message(client, "game_move_err|2\n");
        trace("Socket %d - User move was rejected due to bad message format", client->fd);
        return EXIT_FAILURE;
    }

    if(client->state != STATE_IN_GAME_PLAYING) {
        send_message(client, "game_move_err|5\n");
        trace("Socket %d - User move was rejected due to bad state (%d)", client->fd, client->state);
        return EXIT_FAILURE;
    }

    if(strlen(argv[0]) != 2 || strlen(argv[1]) != 2) {
        send_message(client, "game_move_err|2\n");
        trace("Socket %d - User move was rejected due to bad message format", client->fd);
        return EXIT_FAILURE;
    }

    int f_x = argv[0][0] - '0';
    int f_y = argv[0][1] - '0';

    if(f_x < 0 || f_y < 0 || f_x > 7 || f_y > 7) {
        send_message(client, "game_move_err|2\n");
        trace("Socket %d - User move was rejected due to bad message format", client->fd);
        return EXIT_FAILURE;
    }

    int t_x = argv[1][0] - '0';
    int t_y = argv[1][1] - '0';

    if(t_x < 0 || t_y < 0 || t_x > 7 || t_y > 7) {
        send_message(client, "game_move_err|2\n");
        trace("Socket %d - User move was rejected due to bad message format", client->fd);
        return EXIT_FAILURE;
    }

    signed char color = 0;
    struct client *opp = NULL;

    if(client->game->player1 == client) {
        opp = client->game->player2;
        color = 1;
    } else if (client->game->player2 == client) {
        opp = client->game->player1;
        color = -1;
    }

    if(!opp) {
        send_message(client, "game_move_err|1\n");
        trace("Socket %d - User move was rejected due internal error (game opponent i null)", client->fd);
        return EXIT_FAILURE;
    }

    if(!game_verify_piece_move(client->game, color, f_x, f_y, t_x, t_y)) {
        send_message(client, "game_move_err|8\n");
        trace("Socket %d - User move was rejected due game model", client->fd);
        return EXIT_FAILURE;
    }

    client->state = STATE_IN_GAME;

    send_message(client, "game_move_ok\n");
    char buf[64] = "";
    sprintf(buf, "game_move_data|%d%d|%d%d\n", f_x, f_y, t_x, t_y);
    send_message(opp, buf);

    game_move_piece(client->game, f_x, f_y, t_x, t_y);
    trace("Socket %d - User (%s) played move from [%d;%d] to [%d;%d]", client->fd, client->name, f_x, f_y, t_x, t_y);

    if(!can_player_play(client->game, color * (-1))) {
        if(color < 0) {
            trace("Game %d - The game ends due no possible move. Black (%s) wins.", client->game->id, client->game->player2->name);
        } else if (color > 0) {
            trace("Game %d - The game ends due no possible move. White (%s) wins.", client->game->id, client->game->player1->name);
        }
        game_end(server, client->game, color);
    }

    opp->state = STATE_IN_GAME_PLAYING;
    send_message(opp, "game_turn\n");

    return EXIT_SUCCESS;

}

int cmd_jump(server *server, struct client *client, int argc, char **argv) {
    print_board(client->game);

    if(argc < 2) {
        send_message(client, "game_jump_err|2\n");
        trace("Socket %d - User jump was rejected due to bad message format", client->fd);
        return EXIT_FAILURE;
    }

    if(client->state != STATE_IN_GAME_PLAYING) {
        send_message(client, "game_jump_err|5\n");
        trace("Socket %d - User jump was rejected due to bad state (%d)", client->fd, client->state);
        return EXIT_FAILURE;
    }

    if(strlen(argv[0]) != 2 || strlen(argv[1]) != 2) {
        send_message(client, "game_jump_err|2\n");
        trace("Socket %d - User jump was rejected due to bad message format", client->fd);
        return EXIT_FAILURE;
    }

    int f_x_origin = argv[0][0] - '0';
    int f_y_origin = argv[0][1] - '0';
    int f_x = argv[0][0] - '0';
    int f_y = argv[0][1] - '0';

    if(f_x < 0 || f_y < 0 || f_x > 7 || f_y > 7) {
        send_message(client, "game_jump_err|2\n");
        trace("Socket %d - User jump was rejected due wrong coordinates", client->fd);
        return EXIT_FAILURE;
    }

    signed char color = 0;
    struct client *opp = NULL;

    if(client->game->player1 == client) {
        opp = client->game->player2;
        color = 1;
    } else if (client->game->player2 == client) {
        opp = client->game->player1;
        color = -1;
    }

    if(!opp) {
        send_message(client, "game_jump_err|1\n");
        trace("Socket %d - User jump was rejected due internal error (game opponent i null)", client->fd);
        return EXIT_FAILURE;
    }

    signed char board[8][8] = { 0 };
    int i, j;
    for(i = 0; i < 8; i++) {
        for(j = 0; j < 8; j++) {
            board[i][j] = client->game->board[i][j];
        }
    }

    for(i = 1; i < argc; i++) {
        int t_x = argv[i][0] - '0';
        int t_y = argv[i][1] - '0';

        if(t_x < 0 || t_y < 0 || t_x > 7 || t_y > 7) {
            send_message(client, "game_jump_err|2\n");
            trace("Socket %d - User jump was rejected due wrong coordinates", client->fd);
            return EXIT_FAILURE;
        }

        if(!game_verify_player_jump(board, color, f_x, f_y, t_x, t_y)) {
            send_message(client, "game_jump_err|8\n");
            trace("Socket %d - User jump was rejected due game model", client->fd);
            return EXIT_FAILURE;
        }

        game_jump_piece(board, f_x, f_y, t_x, t_y);
        f_x = t_x;
        f_y = t_y;
    }

    client->state = STATE_IN_GAME;

    char buf[128] = "";
    sprintf(buf, "%s", "game_jump_data");

    f_x = argv[0][0] - '0';
    f_y = argv[0][1] - '0';
    int t_x, t_y;
    for(i = 1; i < argc; i++) {
        t_x = argv[i][0] - '0';
        t_y = argv[i][1] - '0';

        sprintf(buf, "%s|%d%d", buf, f_x, f_y);
        game_jump_piece(client->game->board, f_x, f_y, t_x, t_y);

        f_x = t_x;
        f_y = t_y;
    }

    sprintf(buf, "%s|%d%d\n", buf, t_x, t_y);

    if(client->game->player1 == client) {
        client->game->p2_count -= (argc - 1);
    } else if(client->game->player2 == client) {
        client->game->p1_count -= (argc - 1);
    }

    send_message(client, "game_jump_ok\n");
    send_message(opp, buf);
    trace("Socket %d - User (%s) played jump from [%d;%d] to [%d;%d]", client->fd, client->name, f_x_origin, f_y_origin, t_x, t_y);

    if(client->game->p1_count == 0) {
        trace("Game %d - The game ends due lack of white pieces. Black (%s) wins.", client->game->id, client->game->player2->name);
        game_end(server, client->game, -1);
        return EXIT_SUCCESS;

    } else if(client->game->p2_count == 0) {
        trace("Game %d - The game ends due lack of black pieces. White (%s) wins.", client->game->id, client->game->player1->name);
        game_end(server, client->game, 1);
        return EXIT_SUCCESS;
    }

    if(!can_player_play(client->game, color * (-1))) {
        if(color < 0) {
            trace("Game %d - The game ends due no possible move. Black (%s) wins.", client->game->id, client->game->player2->name);
        } else if (color > 0) {
            trace("Game %d - The game ends due no possible move. White (%s) wins.", client->game->id, client->game->player1->name);
        }
        game_end(server, client->game, color);
    }

    opp->state = STATE_IN_GAME_PLAYING;
    send_message(opp, "game_turn\n");
    return EXIT_SUCCESS;
}

int cmd_logout(server *server, struct client *client, int argc, char **argv) {

    if(client->connected == DISCONNECTED) {
        send_message(client, "logout_err|5\n");
        trace("Socket %d - Tried to disconnect when his state was DISCONNECTED", client->fd);
        return EXIT_FAILURE;
    }

    if(client->state == STATE_UNLOGGED) {
        server_close_connection(server, client->fd);
        send_message(client, "logout_ok\n");
        trace("Socket %d - Socket was disconnected at its request", client->fd);
        return EXIT_SUCCESS;
    }

    char buf[64];

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

            sprintf(buf, "room_leave_opp|%s", client->name);
            send_message(opp, buf);
        } else {
            if(client->game->player1 == client || client->game->player2 == client) {
                arraylist_delete(server->rooms, client->game->id);
                free(client->game);
                client->game = NULL;
            }
        }
    } else if(client->state == STATE_IN_GAME || client->state == STATE_IN_GAME_PLAYING) {

        struct client *opp = NULL;
        signed char winner = 0;

        if(client->game->player1 == client) {
            opp = client->game->player2;
            winner = -1;
        } else if(client->game->player2 == client) {
            opp = client->game->player1;
            winner = 1;
        }

        sprintf(buf, "room_leave_opp|%s\n", client->name);
        send_message(opp, buf);

        if(winner > 0) {
            trace("Game %d - Game was ended due user (%d - %s) logout. White wins.", client->game->id, client->fd, client->name);
        } else if(winner < 0) {
            trace("Game %d - Game was ended due user (%d - %s) logout. Black wins.", client->game->id, client->fd, client->name);
        }
        game_end(server, client->game, winner);
    }

    ht_remove(server->players, client->name);
    send_message(client, "logout_ok\n");
    trace("Socket %d - Socket was disconnected at its request", client->fd);

    server_close_connection(server, client->fd);
    return EXIT_SUCCESS;
}
