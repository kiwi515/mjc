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

// Intrusive, doubly-linked list of heap headers
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

#endif
