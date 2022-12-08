//
// Created by vondr on 20.09.2022.
//

#ifndef SERVER_MAIN_H
#define SERVER_MAIN_H

#include "communication/server.h"


#define TRACE_LOG_FILE_NAME "trace.log"
#define STATS_FILE_NAME "stats.txt"

#define SPLIT_SYMBOL ';'

/**
 * Function to evaluate messages
 * @param server server structure
 * @param fd file descriptor source number
 * @param message message
 * @return information about operation success
 */
int process_message(server *server, int fd, char *message);

/**
 * Function used to send given client a message
 * @param client    client to who the message is being send
 * @param message   message to sed
 */
void send_message(struct client *client, char *message);

/**
 * Function to print and save trace information
 * @param message message format
 * @param ... message values
 */
void trace(char *message, ...);

/**
 * Function to save statistics data into file
 */
void saveStats();


void intHandler();

#endif //SERVER_MAIN_H
