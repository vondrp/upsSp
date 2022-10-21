#include <stdio.h>
#include <stdlib.h>

#include "shipsGame.h"
#include "arraylist.h"
#include "main.h"

bool room_create(server *server, struct client *client)
{
    struct game *game = malloc(sizeof(struct game));
    if(rand() % 2 == 0) {
        game->player1 = client;
        game->player2 = NULL;
    } else {
        game->player2 = client;
        game->player1 = NULL;
    }

    client->game = game;

    game->state = 0;
    client->game->id = arraylist_insert(server->rooms, game);

    return true;
}

void game_init(struct game *game)
{
    int i, j;
    game->p1_count = 0;
    game->p2_count = 0;

    game->player1->playerNum = 1;
    game->player2->playerNum = 2;

    for(i = 0; i < SHIP_GAME_BOARD_SIZE; i++) {
        for(j = 0; j < SHIP_GAME_BOARD_SIZE; j++) {
            game->player1_board[i][j] = 'E';
            game->player2_board[i][j] = 'E';
        }
    }
}

void save_board(char * string_board, int playerNumber)
{
    int x = 0 , y = 0, i, j;
    //int arrLen = sizeof board_symbols / sizeof board_symbols[0];
    //int isElementPresent = 0;
    signed char board[SHIP_GAME_BOARD_SIZE][SHIP_GAME_BOARD_SIZE];
    for (i = 0; i < strlen(string_board); i++)
    {
        /*
        for (j = 0; i < arrLen; i++) {
            if (board_symbols[i] == x) {
                isElementPresent = 1;
            }
        }*/

        board[x][y] = string_board[i];
        x++;
        if (x >= SHIP_GAME_BOARD_SIZE)
        {
            x = 0;
            y++;
        }
    }
}
void game_end(server *server, struct game *game, char* name) {


    send_message(game->player1, "game_end\n"); //add name
    send_message(game->player2, "game_end\n");

    game->player1->game = NULL;
    game->player2->game = NULL;
    game->player1->state = 1;
    game->player2->state = 1;

    arraylist_delete_item(server->rooms, game->id);
    free(game);
}

