#include <stdlib.h>

#include "client.h"

int client_set_name(struct client *client, char *name) {
    if(!client || !name)
    {
        return EXIT_FAILURE;
    }

    if(client->state == STATE_UNLOGGED)
    {
        client->state = STATE_IN_LOBBY;
    }

    strncpy(client->name, name, NAME_MAX_LENGTH);
    client->name[NAME_MAX_LENGTH] = '\0';
}

