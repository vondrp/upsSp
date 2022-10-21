#include <stdio.h>
#include <signal.h>
#include <time.h>
#include <sys/socket.h>
#include <stdarg.h>

#include "main.h"
#include "server.h"
#include "client.h"
#include "commands.h"


#define DEFAULT_PORT 9123


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
    srand(time(NULL));

    if(argc < 3)
    {
        printf("Use ./server -p <port> or ./server --port <port>\n");
        printf("<port> - integer from 1 to 65465\n");
        return EXIT_SUCCESS;
    }

    for(i = 1; i < argc - 1; i++)
    {
        if(!strcmp(argv[i], "-p") || !strcmp(argv[i], "--port"))
        {
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
                port = 9123;
            }
            else
            {
                port = atoi(argv[i + 1]);
            }
        }
    }

    if(port < 1 || port > MAX_PORT_NUMBER)
    {
        printf("Use ./server -p <port> or ./server --port <port>");
        printf("<port> - integer from 1 to %d", MAX_PORT_NUMBER);
        return EXIT_SUCCESS;
    }

    sprintf(buf_port, "%d", port);

    signal(SIGINT, intHandler); // handeling signal

    srv = server_init(); // init server

    // open trace file
    trace_file = fopen(TRACE_LOG_FILE_NAME, "a+");

    if(!srv)
    {
        printf("Initialization failure");
        return EXIT_FAILURE;
    }

    server_listen(srv, buf_port);
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

void send_message(struct client *client, char *message, ...)
{
    int nbytes;
    if(client->name)
    {
        printf("Sending to client %s (%d) message: %s", client->name, client->fd, message);
    }
    else
    {
        printf("Sending to socket %d message: %s", client->fd, message);
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

    char *message_type;
    int argc = 0;

    if(message[message_length - 1] == '\n')
    {
        message[message_length - 1] = '\0';
    }

    if(message_length > 1)
    {
        if(message[message_length - 2] == 13)
        {
            message[message_length - 2] = '\0';

        }
    }

    message_length = strlen(message);
    if(message[message_length - 1] == SPLIT_SYMBOL) {
        message[message_length - 1] == '\0';
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
        message_type = strtok(message, split_symbol_string);
    }

    for(i = 0; i < argc; i++)
    {
        argv[i] = strtok(NULL, split_symbol_string);
    }

    fcmd handler = get_handler(message_type);

    if(handler)
    {
        if(handler(server, server->clients[fd], argc, argv))
        {
            server->stat_fail_requests++;
        }
        server->clients[fd]->invalid_count = 0;
    } else {
        send_message(server->clients[fd], "error;unknown command\n");
        server->clients[fd]->invalid_count++;
        server->stat_unknown_commands++;
    }

    return EXIT_SUCCESS;
}
