//
// Created by hintik on 26.12.21.
//

#include "client.h"
#include <stdbool.h>
#include "server.h"
#include <stdlib.h>

#ifndef SERVER_GAME_H
#define SERVER_GAME_H

#define GAME_STATE_LOBBY 0
#define GAME_STATE_PLAYING 1

/**
 *  structure to represent a game or a roomd
 */
struct game {
    int id;
    struct client *player1;
    struct client *player2;
    signed char left_board[10][10];
    signed char right_board[10][10];
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
 * Function to test whether the player is obliged to play
 * @param game game structure
 * @param color player color information
 * @return boolean information
 */
bool have_to_player_jump(struct game *game, signed char color);

/**
 * Function to test whether one piece is obliged to play
 * @param game game structure
 * @param x x coordination
 * @param y y coordination
 * @return boolean information
 */
bool have_to_jump_piece(struct game *game, int x, int y);

/**
 * Function to validate a player move
 * @param game game structure
 * @param color player color information
 * @param from_x x coordination of source
 * @param from_y y coordination of source
 * @param to_x x coordination of destination
 * @param to_y y coordination of destination
 * @return boolean infomation
 */
bool game_verify_piece_move(struct game *game, signed char color, int from_x, int from_y, int to_x, int to_y);

/**
 * Function to validate a player move
 * @param game game board array
 * @param color player color information
 * @param from_x x coordination of source
 * @param from_y y coordination of source
 * @param to_x x coordination of destination
 * @param to_y y coordination of destination
 * @return boolean infomation
 */
bool game_verify_player_jump(signed char board[8][8], signed char color, int from_x, int from_y, int to_x, int to_y);

/**
 * Function process a move
 * @param game game structure
 * @param from_x x coordination of source
 * @param from_y y coordination of source
 * @param to_x x coordination of destination
 * @param to_y y coordination of destination
 */
void game_move_piece(struct game *game, int from_x, int from_y, int to_x, int to_y);

/**
 * Function process a jump
 * @param game game structure
 * @param from_x x coordination of source
 * @param from_y y coordination of source
 * @param to_x x coordination of destination
 * @param to_y y coordination of destination
 */
void game_jump_piece(signed char board[8][8], int from_x, int from_y, int to_x, int to_y);

/**
 * Function to test, if player is able to make a move or jump
 * @param game game structure
 * @param color player color information
 * @return
 */
bool can_player_play(struct game *game, signed char color);

/**
 * Function to print game board state
 * @param game game structure
 */
void print_board(struct game *game);

/**
 * Function to provide game end procedure
 * @param server server structure
 * @param game game structure
 * @param color_winner color player information of the winner
 */
void game_end(server *server, struct game *game, signed char color_winner);

/**
 * Function to test if dame piece is able to jump
 * @param game game structure
 * @param x x coordination
 * @param y y coordination
 * @return boolean information
 */
bool have_to_jump_dame(struct game *game, int x, int y);

#endif