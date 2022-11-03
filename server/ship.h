//
// Created by vondr on 01.11.2022.
//

#ifndef SERVER_SHIP_H
#define SERVER_SHIP_H

struct ship {
    int id;
    int startX;
    int startY;
    int vertical;
    int length;
    int health;
};

/**
 * Function to create structures and other default information ship
 * @param game  game structure
 */
void ship_init(struct ship *ship, int id);

/**
 * Place ship on given position
 * @param ship  ship to be placed
 * @param x     x-coordinate
 * @param y     y-coordinate
 */
void ship_place(struct ship *ship, int x, int y);

/**
 * Hit given ship
 * @param ship  ship to be hit
 * @return      1 - ship destroyed, 0 -
 */
int ship_hit(struct ship *ship);
#endif //SERVER_SHIP_H
