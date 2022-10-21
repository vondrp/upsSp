cmake_minimum_required(VERSION 3.16.3)
project(server C)

set(CMAKE_C_STANDARD 99)

add_executable(
        server main.h main.c
        server.h server.c
        arraylist.h arraylist.c
        client.h client.c
        commands.h commands.c
        hashmap.h hashmap.c
        player.h
        shipsGame.h shipsGame.c
        )
