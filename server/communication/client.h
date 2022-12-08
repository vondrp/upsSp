#ifndef CLIENT_H
#define CLIENT_H

#include <string.h>

// info in client is connected or not
#define CONNECTED 1
#define DISCONNECTED 0

// states of client
#define STATE_UNLOGGED 0
#define STATE_IN_LOBBY 1
#define STATE_IN_ROOM 2
#define STATE_IN_GAME_PREPARING 3
#define STATE_IN_GAME 4
#define STATE_IN_GAME_PLAYING 5


// max name length without \0
#define NAME_MAX_LENGTH 20

struct client {
    int fd;
    char name[NAME_MAX_LENGTH+1]; //+1 - represent \0
    int state;
    int playerNum;
    struct game *game;
    int connected;
    int invalid_count;
};

/**
 * Set name of the client
 * @param client    client which name is being set
 * @param name      name to be set
 * @return
 */
int client_set_name(struct client *client, char *name);

#endif