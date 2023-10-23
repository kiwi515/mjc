/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Course:  CSE 4251, Section 01, Spring 2023
 * Project: MiniJava Compiler Project
 * Charset: US-ASCII
 */

#include "refcount.h"
#include <assert.h>

/**
 * @brief Increment a heap allocation's reference count
 *
 * @param header Heap allocation header
 */
void refcount_increment(HeapHeader* header) {
    assert(header != NULL);
    header->ref++;
}

/**
 * @brief Decrement a heap allocation's reference count
 *
 * @param header Heap allocation header
 */
void refcount_decrement(HeapHeader* header) {
    assert(header != NULL);

    assert(header->ref > 0);
    header->ref--;

    // Free unreferenced allocations
    if (header->ref == 0) {
        heap_free(header);
    }
}
