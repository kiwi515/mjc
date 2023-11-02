/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Course:  CSE 4251, Section 01, Spring 2023
 * Project: MiniJava Compiler Project
 * Charset: US-ASCII
 */

#include "refcount.h"
#include <assert.h>
#include <stdio.h>

/**
 * @brief Increment a heap allocation's reference count
 *
 * @param header Heap allocation header
 */
void refcount_increment(HeapHeader* header) {
    assert(header != NULL);

    header->ref++;
    DEBUG_LOG("[refcount] increment %p, now %d\n", header, header->ref);
}

/**
 * @brief Decrement a heap allocation's reference count
 *
 * @param header Heap allocation header
 */
void refcount_decrement(HeapHeader* header) {
    assert(header != NULL);

    // Sanity check: Ref count should be valid for decrement
    assert(header->ref > 0);

    header->ref--;
    DEBUG_LOG("[refcount] decrement %p, now %d\n", header, header->ref);

    // Free unreferenced allocations
    if (header->ref == 0) {
        heap_free(header->data);
        DEBUG_LOG("[refcount] free %p\n", header);
    }
}

/**
 * @brief Decrement reference count of a heap allocation's children
 *
 * @param header Heap allocation header
 */
void refcount_decr_children(HeapHeader* header) {
    int i;
    void** ptr;
    HeapHeader* child;

    assert(header != NULL);

    // Search block data for pointers
    for (i = 0; i < header->size; i += sizeof(void*)) {
        // Current word of the block
        ptr = (void**)(&header->data[i]);

        // Check for heap block header
        if (heap_is_header(*ptr)) {
            child = heap_get_header(*ptr);
            DEBUG_LOG("[refcount] decr child %p of %p\n", child, header);
            refcount_decrement(child);
        }
    }
}