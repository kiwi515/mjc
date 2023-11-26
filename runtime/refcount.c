/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Author:  Tyler Gutowski, tgutowski2020@my.fit.edu
 * Course:  CSE 4101, Fall 2023
 * Project: Heap Heap Hooray
 * Charset: US-ASCII
 */

#include "refcount.h"
#include "heap.h"

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

    // Free unreferenced allocations (and recurse the decrement operation)
    if (header->ref == 0) {
        heap_free(header->data, TRUE);
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
    HeapHeader* maybe_header;

    assert(header != NULL);

    // Search block data for pointers
    for (i = 0; i < header->size; i += sizeof(void*)) {
        // Intepret the current word of the block as a possible pointer.
        // (Specifically, 'ptr' is the ADDRESS of this current word.)
        ptr = (void**)(header->data + i);

        // Don't bother with null values
        // (Dereference 'ptr' to get the actual value)
        if (*ptr == NULL) {
            continue;
        }

        // Maybe this possible pointer is specifically a heap header pointer?
        // (Dereference 'ptr' to get the actual value)
        maybe_header = heap_get_header(*ptr);

        // If we really found a heap header pointer, decrement its refcount.
        if (heap_is_header(maybe_header)) {
            DEBUG_LOG("[refcount] decr child %p of %p\n", maybe_header, header);
            refcount_decrement(maybe_header);
        }
    }
}