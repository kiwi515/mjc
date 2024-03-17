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

typedef void (*LinkListForEachFunc)(void* object);

void linklist_destroy(LinkList* list);

void linklist_append(LinkList* list, void* object);
BOOL linklist_remove(LinkList* list, void* object);
void linklist_remove_iter(LinkList* list, LinkNode* at);
LinkNode* linklist_pop(LinkList* list);
void linklist_insert(LinkList* list, LinkNode* at, void* object);

BOOL linklist_contains(const LinkList* list, void* object);
void linklist_dump(const LinkList* list);

/**
 * @brief For-each loop macro
 * @note The current NODE can be accessed through the variable 'NODE'.
 * @note The current ELEMENT can be accessed through the variable 'ELEM'.
 *
 * @param list Pointer to list
 * @param T Type of list elements
 * @param stmt Code to run during each loop iteration
 */
#define LINKLIST_FOREACH(list, T, stmt)                                        \
    {                                                                          \
        for (LinkNode* NODE = (list)->head; NODE != NULL; NODE = NODE->next) { \
            T ELEM = (T)(NODE->object);                                        \
            MJC_ASSERT(ELEM != NULL);                                          \
            stmt                                                               \
        }                                                                      \
    }

/**
 * @brief For-each loop macro (reverse)
 * @note The current NODE can be accessed through the variable 'NODE'.
 * @note The current ELEMENT can be accessed through the variable 'ELEM'.
 *
 * @param list Pointer to list
 * @param T Type of list elements
 * @param stmt Code to run during each loop iteration
 */
#define LINKLIST_FOREACH_REV(list, T, stmt)                                    \
    {                                                                          \
        for (LinkNode* NODE = (list)->tail; NODE != NULL; NODE = NODE->prev) { \
            T ELEM = (T)(NODE->object);                                        \
            MJC_ASSERT(ELEM != NULL);                                          \
            stmt                                                               \
        }                                                                      \
    }

#endif
