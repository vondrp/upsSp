#ifndef ARRAY_LIST_H
#define ARRAY_LIST_H

typedef struct arraylist_
{
    int capacity;
    int size;
    int empty;
    int last;
    int first;
    struct game **data;
    int *next;
} arraylist;

/**
 * Function used to create array list with give capacity
 * @param capacity given capacity
 * @return  created arraylist
 */
arraylist *arraylist_create(int capacity);

/**
 * Insert new item to the array list
 * @param list  array list to which the item is inserted
 * @param item  item to be inserted
 * @return
 */
int arraylist_insert(arraylist *list, struct game *item);

/**
 * Delete item of given index in array list
 * @param list  array list
 * @param index index of item to be removed
 */
void arraylist_delete_item(arraylist *list, int index);

/**
 * Delete array list be clearing it out of memory
 * @param list  array list to free
 */
void arraylist_free(arraylist **list);

/**
 * Increase size of given list by doubling its capacity
 * @param list  array list to be resize
 */
void arraylist_double_capacity(arraylist *list);

/**
 * Get item of given index
 * @param list  array list
 * @param index index of item
 * @return      NULL - wrong index, otherwise data on index position
 */
void *arraylist_get(arraylist *list, int index);

#endif