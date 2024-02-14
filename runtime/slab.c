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
    assert(size > 0);

    // Slab holds the list of available blocks
    Slab* slab = OBJ_NEW(Slab);
    assert(slab != NULL);

    // Underlying memory
    void* begin = heap_alloc(size);
    assert(begin != NULL);

    // Important for copying: This tells us the range of the whole thing
    slab->begin = begin;
    slab->size = size;

    // Initially we have one big, continuous block
    SlabBlock* block = OBJ_NEW(SlabBlock);
    assert(block != NULL);
    block->begin = slab->begin;
    block->size = slab->size;
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
    assert(slab != NULL);
    assert(size > 0);

    // Find the smallest block that can fit this allocation
    SlabBlock* bestBlock = NULL;
    LINKLIST_FOREACH(
        &slab->blocks, SlabBlock,

        // Block already in use
        if (ELEM->alloced) { continue; }

        // Block too small
        if (ELEM->size < size) { continue; }

        // New best block found?
        if (bestBlock == NULL || ELEM->size < bestBlock->size) {
            bestBlock = ELEM;
        });

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
    SlabBlock* otherPart = OBJ_NEW(SlabBlock);
    assert(otherPart != NULL);
    otherPart->begin = (char*)bestBlock->begin + size;
    otherPart->size = bestBlock->size - size;
    otherPart->alloced = FALSE;
    linklist_append(&slab->blocks, otherPart);

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
    LINKLIST_FOREACH(
        &slab->blocks, SlabBlock,

        // Block not in use (could not possibly contain this memory)
        if (!ELEM->alloced) { continue; }

        // Does the memory fall within this block?
        if ((u32)block >= (u32)ELEM->begin &&
            (u32)block < (u32)ELEM->begin + ELEM->size) {
            parent = ELEM;
            break;
        });

    // There *should* always be a parent slab block
    assert(parent != NULL);

    // Free block
    parent->alloced = FALSE;
}
