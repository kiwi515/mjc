/*
 * Author:  Tyler Gutowski, tgutowski2020@my.fit.edu
 * Course:  CSE 4101, Fall 2023
 * Project: Heap Heap Hooray
 * Charset: US-ASCII
 */

#include "copying.h"
#include "heap.h"
#include "marksweep.h"
#include "slab.h"
#include <stdlib.h>
#include <string.h>

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
 * @brief Create slabs
 */
static void init_slabs(void) {
    from_slab = slab_create(SLAB_SIZE);
    MJC_ASSERT(from_slab != NULL);

    to_slab = slab_create(SLAB_SIZE);
    MJC_ASSERT(to_slab != NULL);
}

/**
 * @brief Swap from/to slabs
 */
static void swap_slabs(void) {
    Slab* tmp = from_slab;
    from_slab = to_slab;
    to_slab = tmp;
}

/**
 * @brief Allocate a block of memory
 *
 * @param size Requested size
 */
void* copying_alloc(u32 size) {
    // Create the slabs if they don't exist
    if (from_slab == NULL) {
        init_slabs();
    }

    // Attempt to allocate from the active slab.
    // If this fails, the runtime will call copying_collect before trying again.
    void* block = heap_alloc_ex(from_slab, size);

    // At this point, either the allocation was successful, or the program must
    // have terminated (heap failure).
    MJC_ASSERT(block != NULL);

    return block;
}

/**
 * @brief Free a block of memory
 *
 * @param block Memory block
 */
void copying_free(void* block) {
    MJC_ASSERT(block != NULL);

    // Should always be from the working slab
    heap_free_ex(from_slab, block, TRUE);
}

/**
 * @brief Perform a copying GC cycle
 */
void copying_collect(void) {
    // Mark live allocations
    marksweep_mark();

    // We need to clear the "to" slab.
    // This is done by destroying it, and then re-creating it.
    slab_destroy(to_slab);
    free(to_slab);
    to_slab = slab_create(SLAB_SIZE);
    MJC_ASSERT(to_slab != NULL);

    /**
     * Copy over all live allocations.
     *
     * Code from marksweep just marked all the live heap headers in the "from"
     * slab.
     *
     * Every used slab block has a heap header at the start, so we check those.
     *
     * If the block *is* used, we copy over the block contents to a new block in
     * the to slab.
     */
    // clang-format off
    LINKLIST_FOREACH(&from_slab->blocks, const SlabBlock*,
        // Don't need to copy unused blocks
        if (!ELEM->alloced) {
            continue;
        }

        // Don't copy garbage
        if (!heap_is_header(ELEM->begin)
            || !heap_get_header(ELEM->begin)->marked) {
            continue;
        }

        /**
         * Copy data over to new block.
         * 
         * Because slab operations go through the heap,
         * the beginning of the block contains the heap header.
         * 
         * We do a little hack to copy the *contents* while not copying the HeapHeader.
         */
        u8* contentBegin = ELEM->begin + sizeof(HeapHeader);
        u32 contentSize = ELEM->size - sizeof(HeapHeader);

        // This also means we must create a heap header for the new block,
        // so this memory allocation must go through heap_alloc_ex.
        void* block = heap_alloc_ex(to_slab, contentSize);
        MJC_ASSERT(block != NULL);
        memcpy(block, contentBegin, contentSize);
    )
    // clang-format on

    // Everything left in the "from" slab is garbage
    slab_destroy(from_slab);
    free(from_slab);
    from_slab = slab_create(SLAB_SIZE);
    MJC_ASSERT(from_slab != NULL);

    // Swap from/to
    swap_slabs();
}