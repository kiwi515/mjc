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
    assert(size > 0);

    // Slab holds the list of available blocks
    Slab* slab = OBJ_ALLOC(Slab);
    assert(slab != NULL);

    // Underlying memory
    void* begin = malloc(size);
    assert(begin != NULL);

    // Important for copying: This tells us the range of the whole thing
    slab->begin = (u8*)begin;
    slab->size = size;

    // Initially we have one big, continuous block
    SlabBlock* block = OBJ_ALLOC(SlabBlock);
    assert(block != NULL);
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
    assert(slab != NULL);

    // Free allocations
    // clang-format off
    LINKLIST_FOREACH(&slab->blocks, SlabBlock*
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
    assert(slab != NULL);
    assert(size > 0);

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
    assert(bestBlock->size >= size);
    assert(bestBlock->begin != NULL);

    // If the block is exactly the right size, we can just take it.
    if (bestBlock->size == size) {
        return bestBlock->begin;
    }

    // The block is bigger than what we need, so we need to break off a piece.
    // (This is done by creating a new block with the remaining size)
    SlabBlock* otherPart = OBJ_ALLOC(SlabBlock);
    assert(otherPart != NULL);
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
    assert(slab != NULL);
    assert(block != NULL);

    // If this fails, we are freeing to the wrong slab
    assert((u32)block >= (u32)slab->begin &&
           (u32)block < (u32)slab->begin + slab->size);

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
    assert(parent != NULL);

    // Free block
    parent->alloced = FALSE;
}

/**
 * @brief Copy (NOT MOVE) a slab's contents
 *
 * @param to Copy destination
 * @param from Copy source
 */
void slab_copy(Slab* to, const Slab* from) {
    assert(to != NULL);
    assert(from != NULL);

    // Need to backup size before it gets overwritten
    u32 size = to->size;

    // Destroy existing slab
    slab_destroy(to);

    // Re-create empty slab
    to->size = size;
    to->begin = malloc(size);
    assert(to->begin != NULL);

    // Initial, single block
    SlabBlock* root = OBJ_ALLOC(SlabBlock);
    root->begin = to->begin;
    root->size = to->size;
    linklist_append(&to->blocks, root);

    // Copy over entries (defrag in the process)
    // clang-format off
    LINKLIST_FOREACH(&from->blocks, const SlabBlock*
        // Don't need to copy unused blocks
        if (!ELEM->alloced) {
            continue;
        }

        /**
         * Copy data over to new block.
         * 
         * Because slab operations go through the heap,
         * the beginning of the block contains the heap header.
         * 
         * We do a little hack to copy the *contents* while not copying the HeapHeader.
         * 
         * This also means we must create a new one,
         * so this memory allocation must go through heap_alloc_ex.
         */
        u8* contentBegin = ELEM->begin + sizeof(HeapHeader);
        u32 contentSize = ELEM->size - sizeof(HeapHeader);

        void* block = heap_alloc_ex(to, contentSize);
        assert(block != NULL);
        memcpy(block, contentBegin, contentSize);
    )
    // clang-format on
}