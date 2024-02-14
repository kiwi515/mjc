/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Author:  Tyler Gutowski, tgutowski2020@my.fit.edu
 * Course:  CSE 4101, Fall 2023
 * Project: Heap Heap Hooray
 * Charset: US-ASCII
 */

#ifndef MINI_JAVA_COMPILER_LINKLIST_H
#define MINI_JAVA_COMPILER_LINKLIST_H
#include "types.h"
#include <assert.h>

// Doubly-linked list
typedef struct LinkList {
    struct LinkNode* head;
    struct LinkNode* tail;
} LinkList;

// Link-list node
typedef struct LinkNode {
    struct LinkNode* prev;
    struct LinkNode* next;
    void* object;
} LinkNode;

void linklist_free(LinkList* list);

void linklist_append(LinkList* list, void* object);
BOOL linklist_remove(LinkList* list, void* object);
LinkNode* linklist_pop(LinkList* list);

BOOL linklist_contains(const LinkList* list, void* object);
void linklist_dump(const LinkList* list);

/**
 * @brief For-each loop macro
 * @note The current element can be accessed through the variable 'e'.
 *
 * @param list Pointer to list
 * @param T Type of list elements
 * @param stmt Code to run during each loop iteration
 */
#define LINKLIST_FOREACH(list, T, stmt)                                        \
    {                                                                          \
        for (LinkNode* __node = (list)->head; __node != NULL;                  \
             __node = __node->next) {                                          \
            T* e = (T*)(__node->object);                                       \
            assert(e != NULL);                                                 \
            stmt                                                               \
        }                                                                      \
    }

/**
 * @brief For-each loop macro (reverse)
 * @note The current element can be accessed through the variable 'e'.
 *
 * @param list Pointer to list
 * @param T Type of list elements
 * @param stmt Code to run during each loop iteration
 */
#define LINKLIST_FOREACH_REV(list, T, stmt)                                    \
    {                                                                          \
        for (LinkNode* __node = (list)->tail; __node != NULL;                  \
             __node = __node->prev) {                                          \
            T* e = (T*)(__node->object);                                       \
            assert(e != NULL);                                                 \
            stmt                                                               \
        }                                                                      \
    }

#endif
