//
// Created by hintik on 26.12.21.
//

#include "shipsGame.h"
#include <stdio.h>

bool room_create(server *server, struct client *client) {
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

void game_init(struct game *game) {

    game->p1_count = 0;
    game->p2_count = 0;

	int i, j;
    for(i = 0; i < 8; i++) {
        for(j = 0; j < 8; j++) {
            game->board[i][j] = 0;

            if((i + j) % 2 == 0) {
                if (j > 4) {
                    game->board[i][j] = -1;
                    game->p1_count++;
                }
                if (j < 3) {
                    game->board[i][j] = 1;
                    game->p2_count++;
                }
            }
        }
    }

}

bool have_to_player_jump(struct game *game, signed char color) {
    int i, j;

    for(i = 0; i < 8; i++) {
        for(j = 0; j < 8; j++) {
            if(game->board[i][j] == 0) continue;

            if(game->board[i][j] * color < 0) continue;

            if(have_to_jump_piece(game, i, j)) {
                return true;
            }
        }
    }

    return false;
}

bool can_player_play(struct game *game, signed char color) {
    if(have_to_player_jump(game, color)) {
        return true;
    }

    int i, j;
    for(i = 0; i < 8; i++) {
        for(j = 0; j < 8; j++) {
            if(game->board[i][j] * color <= 0) {
                continue;
            }

            if(color > 0 || abs(game->board[i][j]) == 2) {
                if(i > 0 && j < 7) {
                    if(game->board[i - 1][j + 1] == 0) {
                        return true;
                    }
                }

                if(i < 7 && j < 7) {
                    if(game->board[i + 1][j + 1] == 0) {
                        return true;
                    }
                }
            }

            if(color < 0 || abs(game->board[i][j]) == 2) {
                if(i > 0 && j > 0) {
                    if(game->board[i - 1][j - 1] == 0) {
                        return true;
                    }
                }

                if(i < 7 && j > 0) {
                    if(game->board[i + 1][j - 1] == 0) {
                        return true;
                    }
                }
            }
        }
    }

    return false;
}

bool have_to_jump_piece(struct game *game, int x, int y) {
    if(game->board[x][y] == 0) return false;

    if(x > 1) {
        if((game->board[x][y] > 0 || abs(game->board[x][y]) == 2) && y < 6) {
            if(game->board[x - 2][y + 2] == 0 && game->board[x - 1][y + 1] * game->board[x][y] < 0) {
                return true;
            }
        }

        if((game->board[x][y] < 0 || abs(game->board[x][y]) == 2) && y > 1) {
            if(game->board[x - 2][y - 2] == 0 && game->board[x - 1][y - 1] * game->board[x][y] < 0) {
                return true;
            }
        }
    }

    if(x < 6) {
        if((game->board[x][y] > 0 || abs(game->board[x][y]) == 2) && y < 6) {
            if(game->board[x + 2][y + 2] == 0 && game->board[x + 1][y + 1] * game->board[x][y] < 0) {
                return true;
            }
        }

        if((game->board[x][y] < 0 || abs(game->board[x][y]) == 2) && y > 1) {
            if(game->board[x + 2][y - 2] == 0 && game->board[x + 1][y - 1] * game->board[x][y] < 0) {
                return true;
            }
        }
    }

    return false;
}

bool have_to_jump_dame(struct game *game, int x, int y) {
    bool ld = true, rd = true, lu = true, ru = true;
    bool ld2 = false, rd2 = false, lu2 = false, ru2 = false;
    int tx, ty;
    int i;
    for(i = 1; i < 7; i++) {
        if(ld) {
            tx = x - i;
            ty = y - i;
            if(tx > 7 || tx < 0 || ty > 7 || ty < 0) {
                ld = false;
            } else if(game->board[tx][ty] * game->board[x][y] > 0) {
                ld = false;
            } else if(game->board[tx][ty] != 0 && ld2) {
                ld = false;
            } else if(game->board[tx][ty] * game->board[x][y] < 0 && !ld2) {
                ld2 = true;
            } else if(game->board[tx][ty] == 0 && ld2) {
                return true;
            }
        }
        if(rd) {
            tx = x + i;
            ty = y - i;
            if(tx > 7 || tx < 0 || ty > 7 || ty < 0) {
                rd = false;
            } else if(game->board[tx][ty] * game->board[x][y] > 0) {
                rd = false;
            } else if(game->board[tx][ty] != 0 && rd2) {
                rd = false;
            } else if(game->board[tx][ty] * game->board[x][y] < 0 && !rd2) {
                rd2 = true;
            } else if(game->board[tx][ty] == 0 && rd2) {
                return true;
            }
        }
        if(lu) {
            tx = x - i;
            ty = y + i;
            if(tx > 7 || tx < 0 || ty > 7 || ty < 0) {
                lu = false;
            } else if(game->board[tx][ty] * game->board[x][y] > 0) {
                lu = false;
            } else if(game->board[tx][ty] != 0 && lu2) {
                lu = false;
            } else if(game->board[tx][ty] * game->board[x][y] < 0 && !lu2) {
                lu2 = true;
            } else if(game->board[tx][ty] == 0 && lu2) {
                return true;
            }
        }
        if(ru) {
            tx = x + i;
            ty = y + i;
            if(tx > 7 || tx < 0 || ty > 7 || ty < 0) {
                ru = false;
            } else if(game->board[tx][ty] * game->board[x][y] > 0) {
                ru = false;
            } else if(game->board[tx][ty] != 0 && ru2) {
                ru = false;
            } else if(game->board[tx][ty] * game->board[x][y] < 0 && !ru2) {
                ru2 = true;
            } else if(game->board[tx][ty] == 0 && ru2) {
                return true;
            }
        }
    }
}

bool game_verify_piece_move(struct game *game, signed char color, int from_x, int from_y, int to_x, int to_y) {
    //printf("OVERENI: FX=%d, FY=%d, TX=%d, TY=%d\n", from_x, from_y, to_x, to_y);

    if(game->board[from_x][from_y] * color <= 0) {
        return false;
    }

    if(have_to_player_jump(game, color)) {
        return false;
    }

    int xdif = to_x - from_x;
    int ydif = to_y - from_y;
    int xdir = xdif > 0 ? 1 : -1;
    int ydir = ydif > 0 ? 1 : -1;

    if(abs(xdif) != abs(ydif)) return false;

    int i;
    if(abs(game->board[from_x][from_y]) == 2) {
        for(i = 1; i < xdif; i++) {
            if(game->board[from_x + i * xdir][from_y + i * ydir] != 0) {
                return false;
            }
        }
    } else {
        if(abs(xdif) != 1) return false;
        if(game->board[from_x][from_y] > 0 && ydif < 0) return false;
        if(game->board[from_x][from_y] < 0 && ydif > 0) return false;
        if(game->board[to_x][to_y] != 0) return false;
    }

    return true;
}

bool game_verify_player_jump(signed char board[8][8], signed char color, int from_x, int from_y, int to_x, int to_y) {
    int difx= to_x - from_x;
    int dify = to_y - from_y;

    int x3 = (int) ((from_x + to_x) * 0.5);
    int y3 = (int) ((from_y + to_y) * 0.5);
    if(abs(difx) != abs(dify)) return false;

    if(board[to_x][to_y] != 0) {
        return false;
    }
/*
    if(abs(board[from_x][from_y]) == 2) {

        int dirx = (from_x - to_x) / abs(from_x - to_x);
        int diry = (from_y - to_y) / abs(from_y - to_y);

        int figures = 0;
        int i;
        for(i = 1; i < difx; i++) {
            int x = from_x + i * dirx;
            int y = from_y + i * diry;

            if(board[x][y] * board[from_x][from_y] > 0) return false;
            if(board[x][y] * board[from_x][from_y] < 0) figures++;

            if(figures > 1) {
                return false;
            }
        }

    }*/

    if(abs(board[from_x][from_y]) > 0) {
        if((board[from_x][from_y] > 0 && dify < 0) && abs(board[from_x][from_y]) != 2) return false;
        if((board[from_x][from_y] < 0 && dify > 0) && abs(board[from_x][from_y]) != 2) return false;
        if(abs(difx) != 2) return false;
        if(board[from_x][from_y] * color <= 0) return false;
        if(board[x3][y3] * color >= 0) return false;
    }

    return true;

}

void game_move_piece(struct game *game, int from_x, int from_y, int to_x, int to_y) {
    game->board[to_x][to_y] = game->board[from_x][from_y];
    game->board[from_x][from_y] = 0;

    if(abs(game->board[to_x][to_y]) == 1) {
        if(game->board[to_x][to_y] > 0 && to_y == 7) {
            game->board[to_x][to_y] = 2;
        }

        if(game->board[to_x][to_y] < 0 && to_y == 0) {
            game->board[to_x][to_y] = -2;
        }
    }
}

void game_jump_piece(signed char board[8][8], int from_x, int from_y, int to_x, int to_y) {
    board[to_x][to_y] = board[from_x][from_y];
    board[(int)((from_x + to_x) * 0.5)][(int) ((from_y + to_y) * 0.5)] = 0;
    board[from_x][from_y] = 0;

    if(board[to_x][to_y] == 1 && to_y == 7) {
        board[to_x][to_y] = 2;
    }

    if(board[to_x][to_y] == -1 && to_y == 0) {
        board[to_x][to_y] = -2;
    }
}

void print_board(struct game *game) {
    int i, j;
    for(i = 0; i < 8; i++) {
        for(j = 0; j < 8; j++) {
            printf("%d ", game->board[j][i]);
        }
        printf("\n");
    }
}

void game_end(server *server, struct game *game, signed char color_winner) {

    if(color_winner > 0) {
        send_message(game->player1, "game_end|white\n");
        send_message(game->player2, "game_end|white\n");
    } else if(color_winner < 0) {
        send_message(game->player1, "game_end|black\n");
        send_message(game->player2, "game_end|black\n");
    }

    game->player1->game = NULL;
    game->player2->game = NULL;
    game->player1->state = 1;
    game->player2->state = 1;

    arraylist_delete(server->rooms, game->id);
    free(game);

}

