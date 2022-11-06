#include "client.h"
#include <stdbool.h>
#include "server.h"
#include <stdlib.h>

#ifndef SERVER_GAME_H
#define SERVER_GAME_H

#define GAME_STATE_LOBBY 0
#define GAME_STATE_PLAYING 1

#define SHIP_GAME_BOARD_SIZE 10
#define AMOUNT_OF_SHIP 7

#define GAME_BOARD_STRING_SIZE (SHIP_GAME_BOARD_SIZE * SHIP_GAME_BOARD_SIZE) + SHIP_GAME_BOARD_SIZE

#include "ship.h"
//char board_symbols[] = {'H', 'E', 'M', '0', '1', '2', '3', '4', '5', '6'};
/**
 *  structure to represent a game or a room
 */
struct game {
    int id;
    struct client *player1;
    struct client *player2;
    unsigned char player1_board[SHIP_GAME_BOARD_SIZE][SHIP_GAME_BOARD_SIZE];
    unsigned char player2_board[SHIP_GAME_BOARD_SIZE][SHIP_GAME_BOARD_SIZE];

    struct ship player1Ships[AMOUNT_OF_SHIP];
    struct ship player2Ships[AMOUNT_OF_SHIP];
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


void mark_destroyed_ship(struct game *game, struct ship *ship, int ship_owner);

void save_board(char * string_board, struct game *game, int playerNumber);
#endif