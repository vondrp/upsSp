#include "arraylist.h"
#include <stdlib.h>
#include <stdio.h>
#include <string.h>

#include "shipsGame.h"

arraylist *arraylist_create(int capacity) {
    arraylist *list = (arraylist *) malloc(sizeof(arraylist));
    list->capacity = capacity;
    list->size = 0;
    list->first = -1;
    list->empty = 0;

    list->data = malloc(sizeof(struct game *) * list->capacity);
    list->next = malloc(sizeof(int) * list->capacity);

    int i;
    for(i = 0; i < list->capacity - 1; i++) {
        list->next[i] = i + 1;
    }

    list->next[list->capacity - 1] = -1;

    return list;

}

int arraylist_insert(arraylist *list, struct game *item) {

    if(list->capacity == list->size) {
        arraylist_resize(list);
    }

    list->data[list->empty] = item;
    int temp = list->first;
    list->first = list->empty;
    list->empty = list->next[list->empty];
    list->next[list->first] = temp;

    list->size++;

    return list->first;
}

void arraylist_delete(arraylist *list, int index) {
    if(list->size == 0) {
        return;
    }

    if(list->first == index) {
        list->first = list->next[list->first];
        list->next[index] = list->empty;
        list->empty = index;
        return;
    }
    int i = list->first;
    while(i != -1) {
        if(list->next[i] == index) {
            list->next[i] = list->next[index];
            list->next[index] = list->empty;
            list->empty = index;
            list->size--;
            return;
        }

        i = list->next[i];
    }
}

void arraylist_free(arraylist **list) {

    void *item;
    int i;
    /*
    for(i = 0; i < (*list)->capacity; i++) {
        item = arraylist_get(*list, i);
        if(item) {
            free(item);
        }
    }
    */

    free((*list)->next);
    free((*list)->data);
    free(*list);
}

void arraylist_resize(arraylist *list) {
    int *next = malloc(sizeof(int) * list->capacity * 2);
    struct game **data = malloc(sizeof(struct game *) * list->capacity * 2);

    memcpy(next, list->next, sizeof(int) * list->capacity);
    memcpy(data, list->data, sizeof(struct game *) * list->capacity);

    free(list->next);
    free(list->data);
    list->next = next;
    list->data = data;

}

void *arraylist_get(arraylist *list, int index) {

    if(index >= list->capacity) return NULL;

    return list->data[index];
}
