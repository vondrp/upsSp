#include <stdio.h>
#include <signal.h>
#include <time.h>

#include "main.h"
#include "server.h"


#define DEFAULT_PORT 9123

server *srv;
FILE *trace_file;


int main(int argc, char **argv) {
    srand(time(NULL));

    if(argc < 3) {
        printf("Use ./server -p <port> or ./server --port <port>\n");
        printf("<port> - integer from 1 to 65465\n");
        return EXIT_SUCCESS;
    }

    int port = 0;

    int i;
    for(i = 1; i < argc - 1; i++) {
        if(!strcmp(argv[i], "-p") || !strcmp(argv[i], "--port")) {
            int l = strlen(argv[i + 1]);
            int j;
            for(j = 0; j < l; j++) {
                if(argv[i + 1][j] < '0' || argv[i + 1][j] > '9') {
                    port = -1;
                }
            }

            if(port == -1) {
                port = 9123;
            } else {
                port = atoi(argv[i + 1]);
            }
        }
    }

    if(port < 1 || port > 65565) {
        printf("Use ./server -p <port> or ./server --port <port>");
        printf("<port> - integer from 1 to 65465");
        return EXIT_SUCCESS;
    }

    char buf_port[10];
    sprintf(buf_port, "%d", port);



    signal(SIGINT, intHandler);
    srv = server_init();
    trace_file = fopen(TRACE_LOG_FILE_NAME, "a+");

    if(!srv) {
        printf("Initialization failure");
        return EXIT_FAILURE;
    }

    server_listen(srv, buf_port);
}

void intHandler(int dummy) {
    saveStats();
    fflush(trace_file);
    fclose(trace_file);
    if(srv) {
        server_free(&srv);
    }
}
