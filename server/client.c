#include "client.h"
#include <stdio.h>
#include <stdlib.h>
#include <sys/socket.h>


int client_set_name(struct client *client, char *name) {
    if(!client || !name) {
        return EXIT_FAILURE;
    }

    if(client->state == 0) {
        client->state = 1;
    }

    strncpy(client->name, name, 20);
    client->name[20] = '\0';
}

