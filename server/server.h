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

#endif //SERVER_SERVER_H
