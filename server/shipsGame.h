#include "client.h"
#include <stdbool.h>
#include "server.h"
#include <stdlib.h>

#ifndef SERVER_GAME_H
#define SERVER_GAME_H

#define GAME_STATE_LOBBY 0
#define GAME_STATE_PLAYING 1

#define SHIP_GAME_BOARD_SIZE 10


//char board_symbols[] = {'H', 'E', 'M', '0', '1', '2', '3', '4', '5', '6'};
/**
 *  structure to represent a game or a room
 */
struct game {
    int id;
    struct client *player1;
    struct client *player2;
    signed char player1_board[SHIP_GAME_BOARD_SIZE][SHIP_GAME_BOARD_SIZE];
    signed char player2_board[SHIP_GAME_BOARD_SIZE][SHIP_GAME_BOARD_SIZE];
    int p1_count;
    int p2_count;
    int state;
};

/**
 * Function to create empty room and join a client to it
 * @param server server structure
 * @param client client structure
 * @return bool information about operation success
 */
bool room_create(server *server, struct client *client);

/**
 * Function to create structures and other default information in game structure
 * @param game  game structure
 */
void game_init(struct game *game);


/**
 * Function to provide game end procedure
 * @param server server structure
 * @param game game structure
 * @param name_winner name of the winner
 */
void game_end(server *server, struct game *game, char* name_winner);


void save_board(char * board, int playerNumber);
#endif