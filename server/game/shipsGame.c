#include <stdio.h>
#include <stdlib.h>

#include "shipsGame.h"
#include "../main.h"

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

    game->state = GAME_STATE_LOBBY;
    s_curr_rooms = s_curr_rooms + 1;
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

    for (i = 0; i < SHIP_GAME_BOARD_SIZE; i++) {
        for (j = 0; j < SHIP_GAME_BOARD_SIZE; j++) {
            game->player1_board[i][j] = 'E';
            game->player2_board[i][j] = 'E';
        }
    }

    for (i = 0; i < AMOUNT_OF_SHIP; i++)
    {
        ship_init(&game->player1Ships[i], i);
        ship_init(&game->player2Ships[i], i);
    }

}

void game_end(server *server, struct game *game, char* name) {

    char buff[64];
    sprintf(buff,  "game_end%c%s\n", SPLIT_SYMBOL, name);

    send_message(game->player1, buff);
    send_message(game->player2, buff);

    game->state = GAME_STATE_ERASED;
    game->player1->game = NULL;
    game->player2->game = NULL;
    game->player1->state = STATE_IN_LOBBY;
    game->player2->state = STATE_IN_LOBBY;

    arraylist_delete_item(server->rooms, game->id);
    s_curr_rooms = s_curr_rooms - 1;

    free(game);
}

void mark_destroyed_ship(struct game *game, struct ship *ship, int ship_owner)
{
    int i;
    int x = ship->startX;
    int y = ship->startY;

    if (ship->vertical == 1)
    {
        if (y > 0)
        {
            if (ship_owner == 1)
            {
                game->player1_board[y - 1][x] = 'M';

                if (x > 0)
                    game->player1_board[y - 1][x - 1] = 'M';

                if (x < SHIP_GAME_BOARD_SIZE - 1)
                    game->player1_board[y - 1][x + 1] = 'M';
            }
            else
            {
                game->player2_board[y - 1][x] = 'M';

                if (x > 0)
                    game->player2_board[y - 1][x - 1] = 'M';

                if (x < SHIP_GAME_BOARD_SIZE - 1)
                    game->player2_board[y - 1][x + 1] = 'M';
            }
        }

        for(i = 0; i < ship->length; i++)
        {

            if (ship_owner == 1)
            {
                if (x > 0)
                    game->player1_board[y][x - 1] = 'M';

                if (x < SHIP_GAME_BOARD_SIZE - 1)
                    game->player1_board[y][x + 1] = 'M';
            }
            else
            {
                if (x > 0)
                    game->player2_board[y][x - 1] = 'M';

                if (x < SHIP_GAME_BOARD_SIZE - 1)
                    game->player2_board[y][x + 1] = 'M';
            }
            y++;
        }

        y--; //posledni zvednuti y neplati - uz delsi nez delka

        if (y < SHIP_GAME_BOARD_SIZE - 1)
        {
            if (ship_owner == 1)
            {
                game->player1_board[y + 1][x] = 'M';

                if (x > 0)
                    game->player1_board[y + 1][x - 1] = 'M';

                if (x < SHIP_GAME_BOARD_SIZE - 1)
                    game->player1_board[y + 1][x + 1] = 'M';
            }
            else
            {
                game->player2_board[y + 1][x] = 'M';
                if (x > 0)
                    game->player2_board[y + 1][x - 1] = 'M';

                if (x < SHIP_GAME_BOARD_SIZE - 1)
                    game->player2_board[y + 1][x + 1] = 'M';
            }
        }
    }
    else
    {

        if (x > 0)
        {
            if (ship_owner == 1)
            {
                game->player1_board[y][x - 1] = 'M';

                if (y > 0)
                    game->player1_board[y - 1][x - 1] = 'M';

                if (y < SHIP_GAME_BOARD_SIZE - 1)
                    game->player1_board[y + 1][x - 1] = 'M';
            }
            else
            {
                game->player2_board[y][x - 1] = 'M';

                if (y > 0)
                    game->player2_board[y - 1][x - 1] = 'M';

                if (y < SHIP_GAME_BOARD_SIZE - 1)
                    game->player2_board[y + 1][x - 1] = 'M';
            }
        }

        for (i = 0; i < ship->length; i++)
        {
            if (ship_owner == 1)
            {
                if (y > 0)
                    game->player1_board[y - 1][x] = 'M';

                if (y < SHIP_GAME_BOARD_SIZE - 1)
                    game->player1_board[y + 1][x] = 'M';
            }
            else
            {
                if (y > 0)
                    game->player2_board[y - 1][x] = 'M';

                if (y < SHIP_GAME_BOARD_SIZE - 1)
                    game->player2_board[y + 1][x] = 'M';
            }
            x++;
        }
        x--; // posledni zvyseni x neplati - delsi nez delka

        if (x < SHIP_GAME_BOARD_SIZE - 1)
        {
            if (ship_owner == 1)
            {
                game->player1_board[y][x + 1] = 'M';

                if (y > 0)
                    game->player1_board[y - 1][x + 1] = 'M';

                if (y < SHIP_GAME_BOARD_SIZE - 1)
                    game->player1_board[y + 1][x + 1] = 'M';
            }
            else
            {
                game->player2_board[y][x + 1] = 'M';

                if (y > 0)
                    game->player2_board[y - 1][x + 1] = 'M';

                if (y < SHIP_GAME_BOARD_SIZE - 1)
                    game->player2_board[y + 1][x + 1] = 'M';
            }
        }
    }
}

void clean_client_ships(struct client *client, struct game *game)
{
    int i;
    if (client->playerNum == 1)
    {
        for (i = 0; i < AMOUNT_OF_SHIP; i++)
        {
            ship_init(&game->player1Ships[i], i);
        }
    }
    else
    {
        for (i = 0; i < AMOUNT_OF_SHIP; i++)
        {
            ship_init(&game->player2Ships[i], i);
        }
    }
}
