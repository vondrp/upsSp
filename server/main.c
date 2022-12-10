#include <stdio.h>
#include <signal.h>
#include <time.h>
#include <sys/socket.h>
#include <stdarg.h>

#include "main.h"
#include "communication/server.h"
#include "communication/client.h"
#include "communication/commands.h"


#define DEFAULT_PORT 9123

#define DEFAULT_MAX_ROOMS 10
#define DEFAULT_MAX_PLAYERS_NUM 20

#define MIN_ROOMS 1
#define MIN_PLAYERS 2

#define MAX_PORT_NUMBER 65535

#define SPLIT_SYMBOL ';'

server *srv; // link to server structure
FILE *trace_file; // trace file link


int main(int argc, char **argv)
{
    int port = 0;
    int i, j;
    unsigned long len;
    char buf_port[10];
    char *ip = NULL;

    int max_rooms = DEFAULT_MAX_ROOMS;
    int max_player_num = DEFAULT_MAX_PLAYERS_NUM;

    //char c;
    int cont = 0;
    srand(time(NULL));

    if(argc < 3)
    {
        printf("Use ./server -p <port> or ./server --port <port>\n");
        printf("<port> - integer from 1 to %c\n", MAX_PORT_NUMBER);
        return EXIT_SUCCESS;
    }

    for(i = 1; i < argc - 1; i++)
    {
        if (cont == 1)
        {
            cont = 0;
            continue;
        }

        if(!strcmp(argv[i], "-p") || !strcmp(argv[i], "--port"))
        {
            if (i + 1 >= argc)
            {
                printf("Use ./server -p <port> or ./server --port <port>\n");
                EXIT_FAILURE;
            }

            len = strlen(argv[i + 1]);
            for(j = 0; j < len; j++)
            {
                if(argv[i + 1][j] < '0' || argv[i + 1][j] > '9')
                {
                    port = -1;
                }
            }

            if(port == -1) //set default port
            {
                port = DEFAULT_PORT;
            }
            else
            {
                port = atoi(argv[i + 1]);
            }
            cont = 1;
        }
        // PARAMETER rooms
        if(!strcmp(argv[i], "-r") || !strcmp(argv[i], "--rooms"))
        {
            len = strlen(argv[i + 1]);

            if (i + 1 >= argc)
            {
                printf("Use -r <rooms_amount> or --rooms <rooms_amount>\n");
                EXIT_FAILURE;
            }

            for(j = 0; j < len; j++)
            {
                if(argv[i + 1][j] < '0' || argv[i + 1][j] > '9')
                {
                    max_rooms = -1;
                }
            }

            if(max_rooms == -1) //set default port
            {
                printf("%s is unacceptable value for number of rooms  - default value %d will be used instead\n", argv[i + 1], DEFAULT_MAX_ROOMS);
                max_rooms = DEFAULT_MAX_ROOMS;
            }
            else
            {
                max_rooms = atoi(argv[i + 1]);
            }
            cont = 1;
        }

        // parameter amount of players
        if(!strcmp(argv[i], "-pl") || !strcmp(argv[i], "--players"))
        {
            len = strlen(argv[i + 1]);
            for(j = 0; j < len; j++)
            {
                if(argv[i + 1][j] < '0' || argv[i + 1][j] > '9')
                {
                    max_player_num = -1;
                }
            }

            if(max_player_num == -1) //set default port
            {
                printf("%s is unacceptable value for number of players  - default value %d will be used instead\n", argv[i + 1], DEFAULT_MAX_PLAYERS_NUM);
                max_player_num = DEFAULT_MAX_PLAYERS_NUM;
            }
            else
            {
                max_player_num = atoi(argv[i + 1]);
            }
            cont = 1;
        }

        // ip address parameter
        if(!strcmp(argv[i], "-ip"))
        {
            len = strlen(argv[i + 1]);

            if (i + 1 >= argc)
            {
                printf("Use -ip <ip address>\n");
                EXIT_FAILURE;
            }

            if (len > 0)
            {
                ip = malloc(len * sizeof(char));
                strcpy(ip, argv[i + 1]);
            }
            cont = 1;
        }
    }

    if(port < 1 || port > MAX_PORT_NUMBER)
    {
        printf("Use ./server -p <port> or ./server --port <port>");
        printf("<port> - integer from 1 to %d", MAX_PORT_NUMBER);
        return EXIT_SUCCESS;
    }

    if(max_rooms< MIN_ROOMS)
    {
        printf("Minimal amount of rooms is %d\n", MIN_ROOMS);
        return EXIT_SUCCESS;
    }

    if (max_player_num < MIN_PLAYERS)
    {
        printf("Minimal amount of players is %d\n", MIN_PLAYERS);
        return EXIT_SUCCESS;
    }

    sprintf(buf_port, "%d", port);

    signal(SIGINT, intHandler); // handeling signal

    printf("Server is running on port %d, max number of players is %d, max amount of rooms to be created is %d\n", port, max_player_num, max_rooms);

    srv = server_init(max_rooms, max_player_num); // init server

    // open trace file
    trace_file = fopen(TRACE_LOG_FILE_NAME, "a+");

    if(!srv)
    {
        printf("Initialization failure");
        return EXIT_FAILURE;
    }

    server_listen(srv, buf_port, ip);

    if (ip != NULL)
    {
        free(ip);
    }
}

void intHandler() {
    saveStats();
    fflush(trace_file);
    fclose(trace_file);
    if(srv)
    {
        server_free(&srv);
    }
}

void send_message(struct client *client, char *message)
{
    int nbytes;
    if(client->name)
    {
        printf("Sending to client %s (%d) message: %s\n", client->name, client->fd, message);
    }
    else
    {
        printf("Sending to socket %d message: %s\n", client->fd, message);
    }

    nbytes = (int) send(client->fd, message, strlen(message), 0);
    if(nbytes > 0)
    {
        srv->stat_messages_out++;
        srv->stat_bytes_out += nbytes;
    }
}

void trace(char *format,...) {
    va_list args;
    va_start(args, format);
    vprintf(format, args);
    va_end(args);
    printf("\n");

    va_start(args, format);
    vfprintf(trace_file, format, args);
    va_end(args);
    fprintf(trace_file, "\n");

    fflush(trace_file);
}

void saveStats() {
    FILE *stats_file = fopen(STATS_FILE_NAME, "w");
    fprintf(stats_file, "Sent messages: %d\n", srv->stat_messages_out);
    fprintf(stats_file, "Transmitted bytes: %d\n", srv->stat_bytes_out);
    fprintf(stats_file, "Received messages: %d\n", srv->stat_messages_in);
    fprintf(stats_file, "Received bytes: %d\n", srv->stat_bytes_in);
    fprintf(stats_file, "Fail requests: %d\n", srv->stat_fail_requests);
    fprintf(stats_file, "Unknown commands count: %d\n", srv->stat_unknown_commands);
    fflush(stats_file);
    fclose(stats_file);
}

int process_message(server *server, int fd, char *message)
{
    char split_symbol_string[2] = {SPLIT_SYMBOL , '\0'};
    int i;
    unsigned long message_length = strlen(message);
    char buff[64];
    char *message_type;

    int argc = 0;

    if(message[message_length - 1] == '\n')
    {
        message[message_length - 1] = '\0';
    }

    if(message_length > 1)
    {
        if(message[message_length - 2] == 13) //
        {
            message[message_length - 2] = '\0';

        }
    }

    message_length = strlen(message);
    if(message[message_length - 1] == SPLIT_SYMBOL) {
        message[message_length - 1] = '\0';
        message_length--;
    }

    if(message_length == 0)
    {
        return EXIT_SUCCESS;
    }


    for(i = 0; i < message_length; i++)
    {
        if(message[i] == SPLIT_SYMBOL)
        {
            argc++;
        }
    }

    char *argv[argc];

    if(argc == 0) {
        message_type = message;
    } else {
        message_type = strtok(message, ";");
    }

    for(i = 0; i < argc; i++)
    {
        argv[i] = strtok(NULL, ";");
    }

    fcmd handler = get_handler(message_type);

    if(handler)
    {
        if(handler(server, server->clients[fd], argc, argv))
        {
            server->stat_fail_requests++;
        }
        // can happend that client was deconnected (removed) during loring req
        if (server->clients[fd])
        {
            server->clients[fd]->invalid_count = 0;
        }
    }
    else
    {
        sprintf(buff, "error%c unknown_command\n", SPLIT_SYMBOL);
        send_message(server->clients[fd], buff);
        // can happend that client was deconnected (removed) during loring req
        if (server->clients[fd])
        {
            server->clients[fd]->invalid_count++;
        }

        server->stat_unknown_commands++;
    }

    return EXIT_SUCCESS;
}
