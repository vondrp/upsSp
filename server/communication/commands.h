#ifndef SP_COMMANDS_H
#define SP_COMMANDS_H
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <ctype.h>
#include "client.h"
#include "server.h"
#include "../game/shipsGame.h"

typedef int(*fcmd)(server *server, struct client *client, int, char **);


typedef struct _cmd_handler {
    char *cmd;
    fcmd handler;
} cmd_handler;


fcmd get_handler(char commands[]);

/**
 * Function to handle login request.
 * @param server server structure
 * @param client client structure
 * @param argc number of arguments
 * @param argv values of arguments
 * @return Information about operation success
 */
int cmd_login(server *server, struct client *client, int argc, char **argv);

/**
 * Method used to disconnect user in method cmd_login in case of login_err
 * @param server    server structure
 * @param client    client to be disconnected
 */
void disconnect_login_err(server *server, struct client *client);

/**
 * Function to handle room creation request.
 * @param server server structure
 * @param client client structure
 * @param argc number of arguments
 * @param argv values of arguments
 * @return Information about operation success
 */
int cmd_room_create(server *server, struct client *client, int argc, char **argv);

/**
 * Function to handle room list request.
 * @param server server structure
 * @param client client structure
 * @param argc number of arguments
 * @param argv values of arguments
 * @return Information about operation success
 */
 int cmd_room_list(server *server, struct client *client, int argc, char **argv);

/**
 * Function to handle room join request.
 * @param server server structure
 * @param client client structure
 * @param argc number of arguments
 * @param argv values of arguments
 * @return Information about operation success
 */
int cmd_room_join(server *server, struct client *client, int argc, char **argv);

/**
 * Function to handle room leave request.
 * @param server server structure
 * @param client client structure
 * @param argc number of arguments
 * @param argv values of arguments
 * @return Information about operation success
 */
int cmd_room_leave(server *server, struct client *client, int argc, char **argv);


/**
 * Function to handle logout request
 * @param server server structure
 * @param client client structure
 * @param argc number of arguments
 * @param argv values of arguments
 * @return Information about operation success
 */
int cmd_logout(server *server, struct client *client, int argc, char **argv);

/**
 * Functions to handle game prepared request - set up player gaming boards
 * @param server server structure
 * @param client client structure
 * @param argc number of arguments
 * @param argv values of arguments
 * @return  Information about operation success
 */
int cmd_game_prepared(server *server, struct client *client, int argc, char **argv);

/**
 * Handle fire request
 * @param server server structure
 * @param client client structure
 * @param argc number of arguments
 * @param argv values of arguments
 * @return  Information about operation success
 */
int cmd_game_fire(server *server, struct client *client, int argc, char **argv);

/**
 * Handle game info request - send client game data
 * @param server server structure
 * @param client client structure
 * @param argc number of arguments
 * @param argv values of arguments
 * @return  Information about operation success
 */
int cmd_game_info(server *server, struct client *client, int argc, char **argv);
#endif //SP_COMMANDS_H
