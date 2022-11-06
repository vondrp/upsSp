#include "arraylist.h"
#include <stdlib.h>
#include <string.h>

#include "../game/shipsGame.h"

arraylist *arraylist_create(int capacity)
{
    int i;
    arraylist *list = (arraylist *) malloc(sizeof(arraylist));
    list->capacity = capacity;
    list->size = 0;
    list->first = -1;
    list->empty = 0;

    list->data = malloc(sizeof(struct game *) * list->capacity);
    list->next = malloc(sizeof(int) * list->capacity);

    for(i = 0; i < list->capacity - 1; i++)
    {
        list->next[i] = i + 1;
    }

    list->next[list->capacity - 1] = -1;

    return list;
}

int arraylist_insert(arraylist *list, struct game *item)
{

    if(list->capacity == list->size)
    {
        arraylist_double_capacity(list);
    }

    list->data[list->empty] = item;
    int temp = list->first;
    list->first = list->empty;
    list->empty = list->next[list->empty];
    list->next[list->first] = temp;

    list->size++;

    return list->first;
}

void arraylist_delete_item(arraylist *list, int index)
{
    int i = list->first;
    if(list->size == 0)
    {
        return;
    }

    if(list->first == index)
    {
        list->first = list->next[list->first];
        list->next[index] = list->empty;
        list->empty = index;
        return;
    }

    while(i != -1)
    {
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

void arraylist_free(arraylist **list)
{
    void *item;
    int i;

    free((*list)->next);
    free((*list)->data);
    free(*list);

    list = NULL;
}

void arraylist_double_capacity(arraylist *list)
{
    int *next = malloc(sizeof(int) * list->capacity * 2);
    struct game **data = malloc(sizeof(struct game *) * list->capacity * 2);

    memcpy(next, list->next, sizeof(int) * list->capacity);
    memcpy(data, list->data, sizeof(struct game *) * list->capacity);

    free(list->next);
    free(list->data);
    list->next = next;
    list->data = data;
}

void *arraylist_get(arraylist *list, int index)
{
    if(index >= list->capacity || index < 0) return NULL;

    return list->data[index];
}
