//
// Created by vondr on 13.10.2022.
//
#include <unistd.h>
#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <netdb.h>
#include <sys/ioctl.h>

#include "server.h"
#include "client.h"
#include "../game/shipsGame.h"
#include "../structures/hashmap.h"
#include "../main.h"
#include "commands.h"

#define MAX_INVALID_MESSAGES 5

fd_set master_fd_set;
int s_max_players;
int s_curr_players;

int s_max_rooms;
int s_curr_rooms;

server *server_init(int max_rooms, int max_player_num)
{
    server *server = (struct server_ *) malloc(sizeof(struct server_));

    if(!server)
    {
        return NULL;
    }
    server->clients_size = max_player_num;
    server->clients = malloc(sizeof(struct client *) * server->clients_size);
    server->rooms = arraylist_create(max_rooms);

    server->stat_messages_out = 0;
    server->stat_messages_in = 0;
    server->stat_bytes_out = 0;
    server->stat_bytes_in = 0;
    server->stat_fail_requests = 0;
    server->stat_unknown_commands = 0;
    server->players = (hashtable_t *) ht_create(max_player_num);

    s_max_rooms = max_rooms;
    s_curr_rooms = 0;
    s_max_players = max_player_num;
    s_curr_players = 0;

    return server;
}

void server_free(server **server) {
    free((*server)->clients);
    arraylist_free(&((*server)->rooms));
    ht_destroy((*server)->players);
    free(*server);
}

void *get_in_addr(struct sockaddr *sa)
{
    if (sa->sa_family == AF_INET) {
        return &(((struct sockaddr_in*)sa)->sin_addr);
    }

    return &(((struct sockaddr_in6*)sa)->sin6_addr);
}

void server_listen(server *server, char *port, char *ip) {
    fd_set read_fds;
    struct addrinfo hints, *ai, *p; // address info structure
    struct sockaddr_storage remoteaddr; // client address
    socklen_t addrlen;
    int listener;
    int fdmax, newfd, nbytes;
    char buf[1024];
    int yes=1;
    int i, j, rv;
    char remoteIP[INET6_ADDRSTRLEN];
    int can_read;

    memset(&hints, 0, sizeof hints);
    hints.ai_family = AF_UNSPEC;
    hints.ai_socktype = SOCK_STREAM;
    hints.ai_flags = AI_PASSIVE;

    if ((rv = getaddrinfo(ip, port, &hints, &ai)) != 0)
    {
        fprintf(stderr, "ERROR: %s\n", gai_strerror(rv)); //  gai_strerror - return string representation of addrinfo error
        exit(1);
    }

    for(p = ai; p != NULL; p = p->ai_next)
    {
        listener = socket(p->ai_family, p->ai_socktype, p->ai_protocol);
        if (listener < 0) {
            continue;
        }

        // lose the pesky "address already in use" error message
        setsockopt(listener, SOL_SOCKET, SO_REUSEADDR, &yes, sizeof(int));
        if (bind(listener, p->ai_addr, p->ai_addrlen) < 0)
        {
            close(listener);
            continue;
        }
        break;
    }

    struct sockaddr_in *addr;
    addr = (struct sockaddr_in *)ai->ai_addr;
    printf("Server run on IP address: %s\n",inet_ntoa((struct in_addr)addr->sin_addr));

    if (p == NULL)
    {
        trace("Server failed to bind - port or ip address is unavailable");
        fprintf(stderr, "select server: failed to bind\n");
        exit(2);
    }

    freeaddrinfo(ai);

    if (listen(listener, 10) == -1)
    {
        perror("listen");
        exit(3);
    }

    // Initialize the set of active sockets
    FD_ZERO(&master_fd_set);
    FD_SET(listener, &master_fd_set);  // add the listener to the master set

    fdmax = listener;

    // main loop
    trace("Server has started on port %s\n", port);
    while(1)
    {
        read_fds = master_fd_set;

        if (select(fdmax+1, &read_fds, NULL, NULL, NULL) == -1)
        {
            perror("select");
            exit(4);
        }

        for(i = 0; i <= fdmax; i++)
        {
            if (FD_ISSET(i, &read_fds))
            {
                if (i == listener) {
                    addrlen = sizeof remoteaddr;
                    newfd = accept(listener,
                                   (struct sockaddr *)&remoteaddr,
                                   &addrlen);

                    if (newfd == -1)
                    {
                        perror("accept");
                    }
                    else
                    {
                        FD_SET(newfd, &master_fd_set);
                        if (newfd > fdmax) {

                            fdmax = newfd;
                        }

                        server_new_connection(server, newfd);

                        trace("Socket %d - new connection from %s\n", newfd,
                              inet_ntop(remoteaddr.ss_family,
                                        get_in_addr((struct sockaddr*)&remoteaddr),
                                        remoteIP, INET6_ADDRSTRLEN));

                    }
                } else {
                    ioctl(i, FIONREAD, &can_read);
                    if(can_read > 0) {

                        memset(buf, '\0', sizeof(char) * 1024);
                        if ((nbytes = (int) recv(i, buf, sizeof buf, 0)) <= 0) {
                            // got error or connection closed by client
                            printf("Socket %d - Connection hang up", i);
                            server_disconnect(server, i);
                        } else {
                            trace("Socket %d receiving transmission %s", i, buf);
                            server->stat_messages_in++;
                            server->stat_bytes_in += nbytes;

                            process_message(server, i, buf);

                            if(server->clients[i] && server->clients[i]->invalid_count >= MAX_INVALID_MESSAGES) {
                                trace("Socket %d exceeded the invalid message limit, was disconnected", i);
                                send_message(server->clients[i], "logout_ok\n");
                                server_disconnect(server, i);
                            }
                        }
                    }
                    else if(can_read == 0)
                    {
                        trace("Socket %d disconnected, closing connection", i);
                        server_disconnect(server, i);
                    }
                    else
                    {
                        trace("Socket %d disconnected, closing connection", i);
                        close_connection(i);
                    }
                }
            }
        }
    }
}

void server_disconnect(server *server, int fd_number) {
    struct client *client = server->clients[fd_number];
    if(!client) {
        return;
    }

    struct game *game = client->game;

    // if player is in room -> will be disconnected
    if (game && client->state == STATE_IN_ROOM)
    {
        cmd_room_leave(server, client, 0, NULL);
    }

    if(game && (client->state == STATE_IN_GAME || client->state == STATE_IN_GAME_PLAYING
                || client->state == STATE_IN_GAME_PREPARING)) {
        struct client *opp;
        if(game->player1 == client)
        {
            opp = game->player2;
        }
        else
        {
            opp = game->player1;
        }

        if(opp && opp->connected)
        {
            char buf[64] = "";
            sprintf(buf, "room_leave_opp%c%s\n",SPLIT_SYMBOL, client->name);
            send_message(opp, buf);
        }
        else // opponent is not connected -> disconnected too - destroy game (allows another people to create a room
        {
            game_end(server, game, NULL);
        }
    }

    client->connected = DISCONNECTED;
    client->fd = -1;
    server->clients[fd_number] = NULL;

    server_close_connection(server, fd_number);
}

void server_new_connection(server *server, int fd_number) {
    struct client *client = (struct client *) malloc(sizeof(struct client));

    if(!client) {
        close(fd_number);
        return;
    }

    if(server->clients_size <= fd_number) {
        struct client **temp = (struct client **) malloc(2 * fd_number * sizeof(struct client *));
        if(!temp) {
            close(fd_number);
            free(client);
            return;
        }
        memcpy(temp, server->clients, server->clients_size * sizeof(struct client *));
        free(&(server->clients));
        server->clients = temp;
        server->clients_size = 2 * fd_number;
    }

    client->fd = fd_number;
    client->state = STATE_UNLOGGED;
    client->connected = CONNECTED;
    client->invalid_count = 0;

    server->clients[fd_number] = client;
    send_message(client, "connected\n");
}


void server_close_connection(server *server, int fd)
{
    close_connection(fd);
}

void close_connection(int fd)
{
    s_curr_players = s_curr_players - 1;
    close(fd);
    FD_CLR(fd, &master_fd_set);
}

struct client* server_get_client_by_socket(server *server, int fd) {
    if(fd > 0 || fd < server->clients_size) {
        return server->clients[fd];
    } else {
        return NULL;
    }
}

struct client* server_get_client_by_name(server *server, char *name) {

    return (struct client *) ht_get(server->players, name);
}