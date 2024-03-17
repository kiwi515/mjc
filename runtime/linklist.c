/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Author:  Tyler Gutowski, tgutowski2020@my.fit.edu
 * Course:  CSE 4101, Fall 2023
 * Project: Heap Heap Hooray
 * Charset: US-ASCII
 */

#include "linklist.h"
#include "heap/heap.h"
#include <stdlib.h>
#include <string.h>

/**
 * @brief Append new node to linked list
 */
static void append_impl(LinkList* list, LinkNode* node) {
    MJC_ASSERT(list != NULL);
    MJC_ASSERT(node != NULL);

    if (list->head == NULL) {
        // Initialize list
        node->next = NULL;
        node->prev = NULL;
        list->head = node;
        list->tail = node;
    } else {
        // Extend list
        node->next = NULL;
        node->prev = list->tail;
        list->tail->next = node;
        list->tail = node;
    }
}

/**
 * @brief Remove node from linked list
 */
static void remove_impl(LinkList* list, LinkNode* node) {
    MJC_ASSERT(list != NULL);
    MJC_ASSERT(node != NULL);

    // Handle next link
    if (node->next != NULL) {
        node->next->prev = node->prev;
    }
    // If next is NULL, this is the list tail
    else {
        MJC_ASSERT(node == list->tail);
        list->tail = list->tail->prev;
    }

    // Handle prev link
    if (node->prev != NULL) {
        node->prev->next = node->next;
    }
    // If prev is NULL, this is the list head
    else {
        MJC_ASSERT(node == list->head);
        list->head = list->head->next;
    }

    // Isolate node
    node->next = NULL;
    node->prev = NULL;
}

/**
 * @brief Free memory allocated for linked list
 *
 * @param list Linked list
 */
void linklist_destroy(LinkList* list) {
    MJC_ASSERT(list != NULL);

    for (LinkNode* iter = list->head; iter != NULL;) {
        // Get next ptr early before we break any links
        LinkNode* next = iter->next;

        // Isolate node and free memory
        remove_impl(list, iter);
        MJC_FREE(iter);

        iter = next;
    }
}

/**
 * @brief Append new object to linked list
 *
 * @param list Linked list
 * @param object Object
 */
void linklist_append(LinkList* list, void* object) {
    MJC_ASSERT(list != NULL);
    MJC_ASSERT(object != NULL);

    // Create new list node
    LinkNode* node = MJC_ALLOC_OBJ(LinkNode);
    MJC_ASSERT(node != NULL);
    memset(node, 0, sizeof(LinkNode));

    // Append new node
    node->object = object;
    append_impl(list, node);
}

/**
 * @brief Remove object from linked list
 *
 * @param list Linked list
 * @param object Object
 * @return Whether object was removed
 */
BOOL linklist_remove(LinkList* list, void* object) {
    MJC_ASSERT(list != NULL);
    MJC_ASSERT(object != NULL);

    // Search for object in this list
    for (LinkNode* iter = list->head; iter != NULL; iter = iter->next) {
        if (iter->object == object) {
            remove_impl(list, iter);
            MJC_FREE(iter);
            return TRUE;
        }
    }

    return FALSE;
}

/**
 * @brief Pop tail node from linked list
 *
 * @param list Linked list
 * @return Popped node
 */
LinkNode* linklist_pop(LinkList* list) {
    // Empty list
    if (list->tail == NULL) {
        return NULL;
    }

    // Remove tail node
    LinkNode* node = list->tail;
    remove_impl(list, node);

    return node;
}

/**
 * @brief Insert object AFTER specified position
 *
 * @param list Linked list (unused, for clarity)
 * @param at Position to insert at (new node goes AFTER)
 * @param object Object
 */
void linklist_insert(LinkList* list, LinkNode* at, void* object) {
    MJC_ASSERT(list != NULL);
    MJC_ASSERT(object != NULL);

    // Create new list node
    LinkNode* node = MJC_ALLOC_OBJ(LinkNode);
    MJC_ASSERT(node != NULL);
    memset(node, 0, sizeof(LinkNode));

    node->object = object;

    // Inserting after list tail
    if (at->next == NULL) {
        // at <-> node <-> NULL
        at->next = node;
        node->prev = at;

        // Update list tail
        list->tail = node;
    }
    // Generic list insert
    else {
        // node <-> next
        node->next = at->next;
        node->next->prev = node;
        // at <-> node <-> next
        at->next = node;
        node->prev = at;
    }
}

/**
 * @brief Search for object in linked list
 *
 * @param list Linked list
 * @param object Object
 * @return Whether list contains the object
 */
BOOL linklist_contains(const LinkList* list, void* object) {
    MJC_ASSERT(list != NULL);
    MJC_ASSERT(object != NULL);

    // Search for object in this list
    for (LinkNode* iter = list->head; iter != NULL; iter = iter->next) {
        if (iter->object == object) {
            return TRUE;
        }
    }

    return FALSE;
}

/**
 * @brief Dump contents of linked list to the console (for debug)
 *
 * @param list Linked list
 */
void linklist_dump(const LinkList* list) {
    static const int columns = 6;

    LinkNode* iter;
    int i;

    MJC_ASSERT(list != NULL);

    MJC_LOG("dump list: %p\n", list);

    // List head/tail (if possible)
    MJC_LOG("    head: %p, tail: %p\n",
            list->head != NULL ? list->head->object : NULL,
            list->tail != NULL ? list->tail->object : NULL);

    // Traverse list
    MJC_LOG("    {\n");
    for (i = 0, iter = list->head; iter != NULL; i++, iter = iter->next) {
        // Indent beginning of row
        if (i % columns == 0) {
            MJC_LOG("        ");
        }

        // Node object
        MJC_LOG("%p", iter->object);

        // Comma-separate if possible
        if (iter->next != NULL) {
            MJC_LOG(",");
        }

        // End row
        if ((i + 1) % columns == 0 || iter->next == NULL) {
            MJC_LOG("\n");
        }
    }
    MJC_LOG("    }\n");
}
