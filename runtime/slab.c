/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Author:  Tyler Gutowski, tgutowski2020@my.fit.edu
 * Course:  CSE 4101, Fall 2023
 * Project: Heap Heap Hooray
 * Charset: US-ASCII
 */

#include "slab.h"
#include "heap.h"
#include "linklist.h"

/**
 * @brief Attempt to create a new slab
 *
 * @param size Slab size
 */
Slab* slab_create(u32 size) {
    // Slab holds the list of available blocks
    Slab* slab = OBJ_NEW(Slab);
    assert(slab != NULL);

    // Initially we have one big, continuous block
    SlabBlock* block = OBJ_NEW(SlabBlock);
    assert(block != NULL);

    // Underlying memory
    void* begin = heap_alloc(size);
    assert(begin != NULL);

    // Register block
    block->begin = begin;
    block->size = size;
    linklist_append(&slab->blocks, block);

    return slab;
}

/**
 * @brief Allocate a block of memory
 *
 * @param slab Slab from which to allocate
 * @param size Size of allocation
 */
void* slab_alloc(Slab* slab, u32 size) {
    return NULL;
}

/**
 * @brief Free a block of memory
 *
 * @param slab Slab from which to free
 * @param block Memory block
 */
void slab_free(Slab* slab, void* block) {
    return;
}
