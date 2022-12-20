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

int ship_place(struct ship *ship, int x, int y)
{
    int alright = -1;

    // placing first ship placement
    if (ship->startX == -1 || ship->startY == -1)
    {
        ship->startX = x;
        ship->startY = y;
        alright = 0;
    }
    // placing second ship square - check if vertical or not
    if (ship->length == 1)
    {
        // set ship vertical if is
        if (ship->startX == x)
        {
            ship->vertical = 1;
        }
        else
        {
            ship->vertical = 0;
        }
    }

    if (ship->vertical == 1)
    {
        if (ship->startX != x || (ship->startY + ship->length) != y)
        {
            alright = -1;
        }
        else
        {
            alright = 0;
        }
    }
    else
    {
        if (ship->startY != y || (ship->startX + ship->length) != x)
        {
            alright = -1;
        }
        else
        {
            alright = 0;
        }
    }

    ship->health++;
    ship->length++;

    return alright;
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