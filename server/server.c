//
// Created by vondr on 13.10.2022.
//
#include <unistd.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <netdb.h>
#include <time.h>
#include <sys/ioctl.h>


#include "server.h"

server *server_init() {
    server *server = (struct server_ *) malloc(sizeof(struct server_));

    if(!server) {
        return NULL;
    }
    //server->players = hashmap_new();
    server->clients_size = 20;
    server->clients = malloc(sizeof(struct client *) * server->clients_size);
    server->rooms = arraylist_create(20);

    server->stat_messages_out = 0;
    server->stat_messages_in = 0;
    server->stat_bytes_out = 0;
    server->stat_bytes_in = 0;
    server->stat_fail_requests = 0;
    server->stat_unknown_commands = 0;

    server->players = (hashtable_t *) ht_create(20);
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

void server_listen(server *server, char *port) {
    fd_set read_fds;
    struct addrinfo hints, *ai, *p;
    struct sockaddr_storage remoteaddr; // client address
    socklen_t addrlen;
    int listener;
    int fdmax;
    int newfd;
    int nbytes;
    char buf[1024];
    int yes=1;
    int i, j, rv;
    char remoteIP[INET6_ADDRSTRLEN];

    memset(&hints, 0, sizeof hints);
    hints.ai_family = AF_UNSPEC;
    hints.ai_socktype = SOCK_STREAM;
    hints.ai_flags = AI_PASSIVE;
    if ((rv = getaddrinfo(NULL, port, &hints, &ai)) != 0) {
        fprintf(stderr, "ERROR: %s\n", gai_strerror(rv));
        exit(1);
    }

    for(p = ai; p != NULL; p = p->ai_next) {
        listener = socket(p->ai_family, p->ai_socktype, p->ai_protocol);
        if (listener < 0) {
            continue;
        }

        // lose the pesky "address already in use" error message
        setsockopt(listener, SOL_SOCKET, SO_REUSEADDR, &yes, sizeof(int));

        if (bind(listener, p->ai_addr, p->ai_addrlen) < 0) {
            close(listener);
            continue;
        }
        break;
    }

    if (p == NULL) {
        trace("Server failed to bind");
        fprintf(stderr, "selectserver: failed to bind\n");
        exit(2);
    }

    freeaddrinfo(ai);

    if (listen(listener, 10) == -1) {
        perror("listen");
        exit(3);
    }

    FD_ZERO(&master_fd_set);
    // add the listener to the master set
    FD_SET(listener, &master_fd_set);

    fdmax = listener;

    int can_read;
    // main loop
    trace("Server has started on port %s\n", port);
    for(;;) {
        read_fds = master_fd_set;
        if (select(fdmax+1, &read_fds, NULL, NULL, NULL) == -1) {
            perror("select");
            exit(4);
        }

        for(i = 0; i <= fdmax; i++) {
            if (FD_ISSET(i, &read_fds)) {
                if (i == listener) {
                    addrlen = sizeof remoteaddr;
                    newfd = accept(listener,
                                   (struct sockaddr *)&remoteaddr,
                                   &addrlen);

                    if (newfd == -1) {
                        perror("accept");
                    } else {
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
                        if ((nbytes = recv(i, buf, sizeof buf, 0)) <= 0) {
                            // got error or connection closed by client
                            printf("Socket %d - Connection hang up", i);

                            //trace("")
                            server_disconnect(server, i);
                        } else {
                            trace("Socket %d receiving transmission %s", i, buf);
                            server->stat_messages_in++;
                            server->stat_bytes_in += nbytes;
                            process_message(server, i, buf);
                            if(server->clients[i]->invalid_count >= MAX_INVALID_MESSAGES) {
                                trace("Socket %d exceeded the invalid message limit, was disconnected", i);
                                send_message(server->clients[i], "logout_ok\n");
                                server_disconnect(server, i);
                            }
                        }
                    } else if(can_read == 0){
                        trace("Socket %d disconnected, closing connection", i);
                        server_disconnect(server, i);
                    } else {
                        trace("Socket %d disconnected, closing connection", i);
                        close_connection(i);
                    }
                }
            }
        }
    }
}