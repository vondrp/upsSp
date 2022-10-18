//
// Created by vondr on 13.10.2022.
//


#ifndef SERVER_SERVER_H
#define SERVER_SERVER_H

#include "arraylist.h"
#include "hashmap.h"
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
 * Type definition of server structure
 */
typedef struct server_ server;

/**
 * Function to default initialization server structure
 * @return
 */
server *server_init();

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
 */
void server_listen(server *server, char *port);

/**
 * Function to print and save trace information
 * @param message message format
 * @param ... message values
 */
void trace(char *message,...);

#endif //SERVER_SERVER_H
