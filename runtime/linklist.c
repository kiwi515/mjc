/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Author:  Tyler Gutowski, tgutowski2020@my.fit.edu
 * Course:  CSE 4101, Fall 2023
 * Project: Heap Heap Hooray
 * Charset: US-ASCII
 */

#include "linklist.h"
#include "heap.h"
#include <stdlib.h>

/**
 * @brief Append new node to linked list
 */
static void append_impl(LinkList* list, LinkNode* node) {
    assert(list != NULL);
    assert(node != NULL);

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
 * @brief Remove node to linked list
 */
static void remove_impl(LinkList* list, LinkNode* node) {
    assert(list != NULL);
    assert(node != NULL);

    // Handle next link
    if (node->next != NULL) {
        node->next->prev = node->prev;
    }
    // If next is NULL, this is the list tail
    else {
        assert(node == list->tail);
        list->tail = list->tail->prev;
    }

    // Handle prev link
    if (node->prev != NULL) {
        node->prev->next = node->next;
    }
    // If prev is NULL, this is the list head
    else {
        assert(node == list->head);
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
void linklist_free(LinkList* list) {
    LinkNode* iter;
    LinkNode* next;

    assert(list != NULL);

    for (iter = list->head; iter != NULL; iter = next) {
        // Get next ptr early before we break any links
        next = iter->next;

        // Isolate node and free memory
        remove_impl(list, iter);
        free(iter);
    }
}

/**
 * @brief Append new object to linked list
 *
 * @param list Linked list
 * @param object Object
 */
void linklist_append(LinkList* list, void* object) {
    LinkNode* node;

    assert(list != NULL);
    assert(object != NULL);

    // Create new list node
    node = malloc(sizeof(LinkNode));
    assert(node != NULL);

    // Append new node
    node->object = object;
    append_impl(list, node);
}

/**
 * @brief Pop tail node from linked list
 *
 * @param list Linked list
 * @return Popped node
 */
LinkNode* linklist_pop(LinkList* list) {
    LinkNode* node;

    // Empty list
    if (list->tail == NULL) {
        return NULL;
    }

    // Remove tail node
    node = list->tail;
    remove_impl(list, node);

    return node;
}

/**
 * @brief Remove object from linked list
 *
 * @param list Linked list
 * @param object Object
 * @return Whether object was removed
 */
BOOL linklist_remove(LinkList* list, void* object) {
    LinkNode* iter;

    assert(list != NULL);
    assert(object != NULL);

    // Search for object in this list
    for (iter = list->head; iter != NULL; iter = iter->next) {
        if (iter->object == object) {
            remove_impl(list, iter);
            return TRUE;
        }
    }

    return FALSE;
}

/**
 * @brief Search for object in linked list
 *
 * @param list Linked list
 * @param object Object
 * @return Whether list contains the object
 */
BOOL linklist_contains(const LinkList* list, void* object) {
    LinkNode* iter;

    assert(list != NULL);
    assert(object != NULL);

    // Search for object in this list
    for (iter = list->head; iter != NULL; iter = iter->next) {
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

    assert(list != NULL);

    DEBUG_LOG("[linklist] dump list: %p\n", list);

    // List head/tail (if possible)
    DEBUG_LOG("    head: %p, tail: %p\n",
              list->head != NULL ? list->head->object : NULL,
              list->tail != NULL ? list->tail->object : NULL);

    // Traverse list
    DEBUG_LOG("    {\n");
    for (i = 0, iter = list->head; iter != NULL; i++, iter = iter->next) {
        // Indent beginning of row
        if (i % columns == 0) {
            DEBUG_LOG("        ");
        }

        // Node object
        DEBUG_LOG("%p", iter->object);

        // Comma-separate if possible
        if (iter->next != NULL) {
            DEBUG_LOG(",");
        }

        // End row
        if ((i + 1) % columns == 0 || iter->next == NULL) {
            DEBUG_LOG("\n");
        }
    }
    DEBUG_LOG("    }\n");
}