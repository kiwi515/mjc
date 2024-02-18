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
#include <stdlib.h>
#include <string.h>

/**
 * @brief Attempt to create a new slab
 *
 * @param size Slab size
 */
Slab* slab_create(u32 size) {
    MJC_ASSERT(size > 0);

    // Slab holds the list of available blocks
    Slab* slab = OBJ_ALLOC(Slab);
    MJC_ASSERT(slab != NULL);

    // Underlying memory
    void* begin = malloc(size);
    MJC_ASSERT(begin != NULL);

    // Important for copying: This tells us the range of the whole thing
    slab->begin = (u8*)begin;
    slab->size = size;

    // Initially we have one big, continuous block
    SlabBlock* block = OBJ_ALLOC(SlabBlock);
    MJC_ASSERT(block != NULL);
    block->begin = slab->begin;
    block->size = slab->size;
    linklist_append(&slab->blocks, block);

    return slab;
}

/**
 * @brief Destroy slab and free any memory
 *
 * @param slab Slab
 */
void slab_destroy(Slab* slab) {
    MJC_ASSERT(slab != NULL);

    // Free allocations
    // clang-format off
    LINKLIST_FOREACH(&slab->blocks, SlabBlock*,
        // Block isn't in use
        if (!ELEM->alloced) {
            continue;
        }

        // Heap manages them despite the memory coming from this slab
        heap_free_ex(slab, ELEM->begin, TRUE);
    )
    // clang-format on

    // Free memory used for linked list
    linklist_destroy(&slab->blocks);

    // Release memory slab
    free(slab->begin);

    // Just in case
    slab->begin = NULL;
    slab->size = 0;
}

/**
 * @brief Allocate a block of memory
 *
 * @param slab Slab from which to allocate
 * @param size Size of allocation
 */
void* slab_alloc(Slab* slab, u32 size) {
    MJC_ASSERT(slab != NULL);
    MJC_ASSERT(size > 0);

    // Find the smallest block that can fit this allocation
    SlabBlock* bestBlock = NULL;
    LinkNode* bestBlockNode = NULL;

    // clang-format off
    LINKLIST_FOREACH(&slab->blocks, SlabBlock*,
        // Block already in use
        if (ELEM->alloced) {
            continue;
        }

        // Block too small
        if (ELEM->size < size) {
            continue;
        }

        // New best block found?
        if (bestBlock == NULL || ELEM->size < bestBlock->size) {
            bestBlock = ELEM;
            bestBlockNode = NODE;
        }
    );
    // clang-format on

    // No available block, we need to GC
    if (bestBlock == NULL) {
        return NULL;
    }

    // Sanity check
    MJC_ASSERT(bestBlock->size >= size);
    MJC_ASSERT(bestBlock->begin != NULL);

    // If the block is exactly the right size, we can just take it.
    if (bestBlock->size == size) {
        return bestBlock->begin;
    }

    // The block is bigger than what we need, so we need to break off a piece.
    // (This is done by creating a new block with the remaining size)
    SlabBlock* otherPart = OBJ_ALLOC(SlabBlock);
    MJC_ASSERT(otherPart != NULL);
    otherPart->begin = bestBlock->begin + size;
    otherPart->size = bestBlock->size - size;
    otherPart->alloced = FALSE;

    // By inserting this new node right after the one we split from, the list
    // remains sorted!
    linklist_insert(&slab->blocks, bestBlockNode, otherPart);

    bestBlock->size = size;
    bestBlock->alloced = TRUE;
    return bestBlock->begin;
}

/**
 * @brief Free a block of memory
 *
 * @param slab Slab from which to free
 * @param block Memory block
 */
void slab_free(Slab* slab, void* block) {
    MJC_ASSERT(slab != NULL);
    MJC_ASSERT(block != NULL);

    MJC_ASSERT_MSG((u32)block >= (u32)slab->begin &&
                       (u32)block < (u32)slab->begin + slab->size,
                   "Block is not from this slab");

    // Find the slab block that contains this memory
    SlabBlock* parent = NULL;

    // clang-format off
    LINKLIST_FOREACH(&slab->blocks, SlabBlock*,
        // Block not in use (could not possibly contain this memory)
        if (!ELEM->alloced) {
            continue;
        }

        // Does the memory fall within this block?
        if ((u32)block >= (u32)ELEM->begin &&
            (u32)block < (u32)ELEM->begin + ELEM->size) {
            parent = ELEM;
            break;
        }
    );
    // clang-format on

    // There *should* always be a parent slab block
    MJC_ASSERT(parent != NULL);

    // Free block
    parent->alloced = FALSE;
}
