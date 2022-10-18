#ifndef ARRAY_LIST_H
#define ARRAY_LIST_H

typedef struct arraylist_ {
    int capacity;
    int size;
    int empty;
    int last;
    int first;
    struct game **data;
    int *next;
} arraylist;

arraylist *arraylist_create(int capacity);

int arraylist_insert(arraylist *list, struct game *item);

void arraylist_delete(arraylist *list, int index);

void arraylist_free(arraylist **list);

void arraylist_resize(arraylist *list);

void *arraylist_get(arraylist *list, int index);

#endif