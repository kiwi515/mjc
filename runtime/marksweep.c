/*
 * Author:  Tyler Gutowski, tgutowski2020@my.fit.edu
 * Course:  CSE 4101, Fall 2023
 * Project: Heap Heap Hooray
 * Charset: US-ASCII
 */

#include "marksweep.h"
#include "heap.h"

static void mark(HeapHeader* header);
static void sweep(void);

// Linked list of mark roots
static LinkList root_list;

/**
 * @brief Add new root for marking
 *
 * @param header Block header
 */
void marksweep_add_root(HeapHeader* header) {
    assert(header != NULL);

    // Don't overwrite existing list data
    if (linklist_contains(&root_list, header)) {
        return;
    }

    linklist_append(&root_list, header);
    DEBUG_LOG("[marksweep] add_root %p\n", header);
}

/**
 * @brief Remove root for marking
 *
 * @param header Block header
 */
void marksweep_remove_root(HeapHeader* header) {
    assert(header != NULL);

    if (linklist_remove(&root_list, header)) {
        DEBUG_LOG("[marksweep] remove_root %p\n", header);
    }
}

/**
 * @brief Mark all reachable objects
 */
void marksweep_mark(void) {
    LinkNode* iter = heap_list.head;
    HeapHeader* current = NULL;

    while (iter != NULL) {
        current = (HeapHeader*)iter->object;

        // ignore invalid heap headers
        if (heap_is_header(current)) {
            // mark the block if it's in use
            if (current->ref > 0) {
                mark(current);
            }
        }

        iter = iter->next;
    }
}

/**
 * @brief Mark the specified heap header
 *
 * @param header Block header
 */
static void mark(HeapHeader* header) {
    assert(header != NULL);

    // only mark the header if it's not already marked
    if (!header->marked) {
        header->marked = TRUE;
        DEBUG_LOG("[marksweep] mark %p\n", header);
    }
}

/**
 * @brief Sweep all unreachable objects
 */
void marksweep_sweep() { sweep(); }

/**
 * @brief Sweep all unreachable objects
 */
static void sweep(void) {
    LinkNode* iter = heap_list.head;
    HeapHeader* current = NULL;

    while (iter != NULL) {
        LinkNode* next = iter->next;
        current = (HeapHeader*)iter->object;

        // free unmarked objects
        if (!current->marked) {
            heap_free(current->data);
            DEBUG_LOG("[marksweep] sweep %p\n", current);
        } else {
            // unmark for next gc cycle and update previous pointer
            current->marked = FALSE;
        }

        iter = next;
    }
}

/**
 * @brief Perform mark-and-sweep garbage collection
 */
void marksweep_collect(void) {
    marksweep_mark();
    marksweep_sweep();
}
