//
// Created by vondr on 01.11.2022.
//

#ifndef SERVER_SHIP_H
#define SERVER_SHIP_H

#define SHIP_0_L 4
#define SHIP_1_L 3
#define SHIP_2_L 3
#define SHIP_3_L 2
#define SHIP_4_L 2
#define SHIP_5_L 2
#define SHIP_6_L 1

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
 * @return      0 - ship placed alright, -1 otherwise
 */
int ship_place(struct ship *ship, int x, int y);

/**
 * Hit given ship
 * @param ship  ship to be hit
 * @return      1 - ship destroyed, 0 -
 */
int ship_hit(struct ship *ship);
#endif //SERVER_SHIP_H
