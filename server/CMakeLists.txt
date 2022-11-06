cmake_minimum_required(VERSION 3.16.3)
project(server C)

set(CMAKE_C_STANDARD 99)

add_executable(
        server main.h main.c
        communication/server.h communication/server.c
        structures/arraylist.h structures/arraylist.c
        communication/client.h communication/client.c
        communication/commands.h communication/commands.c
        structures/hashmap.h structures/hashmap.c
        game/shipsGame.h game/shipsGame.c
        game/ship.h game/ship.c
        )
