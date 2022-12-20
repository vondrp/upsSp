#ifndef SERVER_GAME_H
#define SERVER_GAME_H

#include <stdlib.h>
#include <stdbool.h>

#include "../communication/client.h"
#include "../communication/server.h"

#define GAME_STATE_ERASED 2
#define GAME_STATE_LOBBY 0
#define GAME_STATE_GAME 1

#define SHIP_GAME_BOARD_SIZE 10
#define AMOUNT_OF_SHIP 7

#define GAME_BOARD_STRING_SIZE (SHIP_GAME_BOARD_SIZE * SHIP_GAME_BOARD_SIZE)

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

/**
 * Mark ship as destroyed by making marking positions around it as missed
 * @param game      game structure
 * @param ship      ship structure
 * @param ship_owner 1 or 2 - player 1 or player 2 of the game ship belongs
 */
void mark_destroyed_ship(struct game *game, struct ship *ship, int ship_owner);

/**
 * Clean client ships
 * @param client   client ships to be cleaned
 * @param game      game where client is playing
 */
void clean_client_ships(struct client *client, struct game *game);
#endif