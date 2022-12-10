//
// Created by vondr on 13.10.2022.
//

#ifndef SERVER_SERVER_H
#define SERVER_SERVER_H

#include "../structures/arraylist.h"
#include "../structures/hashmap.h"

extern int s_max_players;
extern int s_curr_players;

extern int s_max_rooms;
extern int s_curr_rooms;

/**
 * Server structure
 */
struct server_ {
    struct client **clients;
    int clients_size;
    arraylist *rooms;
    int listener;

    int stat_messages_out;
    int stat_messages_in;
    int stat_bytes_out;
    int stat_bytes_in;
    int stat_fail_requests;
    int stat_unknown_commands;
    hashtable_t *players;
};

/**
 *  Function to close connection with socket
 * @param fd file descriptor number
 */
void close_connection(int fd);

/**
 * Type definition of server structure
 */
typedef struct server_ server;


/**
 * Function to default initialization server structure
 * @param max_rooms         max amount of rooms
 * @param max_player_num    max amount of player
 * @return  instance of server structure
 */
server *server_init(int max_rooms, int max_player_num);

/**
 * Destroy server (deallocates memory)
 * @param server server structure
 */
void server_free(server **server);

/**
 * Function to handle new connection
 * @param server server structure
 * @param fd_number file descriptor number
 */
void server_new_connection(server *server, int fd_number);

/**
 * Function to disconnect client from to server
 * @param server server structure
 * @param fd file descriptor number
 */
void server_close_connection(server *server, int fd);

/**
 * Function to close connection with socket
 * @param server server structure
 * @param fd_number file descriptor number
 */
void server_disconnect(server *server, int fd_number);

/**
 * Function with endless loop to listen incoming messages
 * @param server server structure
 * @param port server port
 * @param ip   ip address of the port
 */
void server_listen(server *server, char *port, char *ip);

#endif //SERVER_SERVER_H
