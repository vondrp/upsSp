//
// Created by vondr on 01.11.2022.
//
#include "ship.h"

void ship_init(struct ship *ship, int id)
{
    ship->id = id;
    ship->length = 0;

    ship->startX = -1;
    ship->startY = -1;
    ship->vertical = 0;
    ship->health =  0;
}

void ship_place(struct ship *ship, int x, int y)
{
    if (ship->startX == -1 || ship->startY == -1)
    {
        ship->startX = x;
        ship->startY = y;
    }
    else
    {
        if ( (y - 1) == ship->startY && x == ship->startX)
        {
            ship->vertical = 1;
        }
    }

    ship->health++;
    ship->length++;
}

int ship_hit(struct ship *ship)
{
    ship->health--;

    if (ship->health <= 0)
    {
        return 1;
    }

    return 0;
}