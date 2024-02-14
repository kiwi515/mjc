/*
 * Author:  Tyler Gutowski, tgutowski2020@my.fit.edu
 * Course:  CSE 4101, Fall 2023
 * Project: Heap Heap Hooray
 * Charset: US-ASCII
 */

#include "copying.h"
#include "heap.h"
#include "slab.h"
#include <stdlib.h>

/**
 * @brief Size of each slab in bytes
 */
static const u32 SLAB_SIZE = 0xFFFF;

/**
 * @brief Copying only needs two slabs
 */
static Slab* from_slab = NULL;
static Slab* to_slab = NULL;

/**
 * @brief Swap from/to slabs
 */
static void swap_slabs(void) {
    Slab* tmp = from_slab;
    from_slab = to_slab;
    to_slab = tmp;
}

/**
 * @brief Create slabs
 */
static void init_slabs(void) {
    from_slab = slab_create(SLAB_SIZE);
    assert(from_slab != NULL);

    to_slab = slab_create(SLAB_SIZE);
    assert(to_slab != NULL);
}

/**
 * @brief Allocate a block of memory
 *
 * @param size Requested size
 */
void* copying_alloc(u32 size) {
    // Attempt to allocate from the active slab
    void* block = heap_alloc_ex(from_slab, size);
    if (block != NULL) {
        return block;
    }

    // Do garbage collection and try again
    copying_collect();

    void* block = heap_alloc_ex(from_slab, size);
    if (block != NULL) {
        return block;
    }

    // Out of memory
    DEBUG_LOG("[copying] cannot allocate %u from slab\n", size);
    exit(EXIT_FAILURE);
    return NULL;
}

/**
 * @brief Free a block of memory
 *
 * @param block Memory block
 */
void copying_free(void* block) {
    assert(block != NULL);

    // Should always be from the working slab
    heap_free_ex(from_slab, block, TRUE);
}

/**
 * @brief Perform a copying GC cycle
 */
void copying_collect(void) {}