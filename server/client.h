#include <string.h>

#ifndef CLIENT_H
#define CLIENT_H

#define CONNECTED 1
#define DISCONNECTED 0

#define STATE_UNLOGGED 0
#define STATE_IN_LOBBY 1
#define STATE_IN_ROOM 2
#define STATE_IN_GAME 3
#define STATE_IN_GAME_PLAYING 4

struct client {
    int fd;
    char name[21];
    int state;
    struct game *game;
    int connected;
    int invalid_count;
};

int send_message(struct client *client, char *message);

int client_set_name(struct client *client, char *name);

#endif