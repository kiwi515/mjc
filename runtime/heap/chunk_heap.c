/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Author:  Tyler Gutowski, tgutowski2020@my.fit.edu
 * Course:  CSE 4101, Fall 2023
 * Project: Heap Heap Hooray
 * Charset: US-ASCII
 */

#include "heap/chunk_heap.h"

typedef struct ChunkBlock {
    // Memory owned by this block
    u8* begin;
    u32 size;

    // Whether this block is in use
    BOOL alloced;
} ChunkBlock;

/**
 * @brief Create chunk heap
 *
 * @param size Chunk size
 */
Heap* chunkheap_create(u32 size) {
    MJC_ASSERT_MSG(size > 0, "Invalid chunk size");

    ChunkHeap* self = MJC_ALLOC_OBJ(ChunkHeap);
    MJC_ASSERT(self != NULL);
    self->base.type = HeapType_ChunkHeap;

    // Register heap functions
    self->base.destroy = chunkheap_destroy;
    self->base.__alloc = chunkheap_alloc;
    self->base.__free = chunkheap_free;
    self->base.is_object = chunkheap_is_object;
    self->base.dump = chunkheap_dump;

    // Underlying memory
    void* begin = malloc(size);
    MJC_ASSERT(begin != NULL);

    // Important for copying: This tells us the range of the whole thing
    self->begin = (u8*)begin;
    self->size = size;

    // Initially we have one big, continuous block
    ChunkBlock* block = MJC_ALLOC_OBJ(ChunkBlock);
    MJC_ASSERT(block != NULL);
    block->begin = self->begin;
    block->size = self->size;
    block->alloced = FALSE;

    linklist_append(&self->blocks, block);

    // Handle to basic heap
    return &self->base;
}

/**
 * @brief Destroy this heap
 *
 * @param heap Chunk heap
 */
void chunkheap_destroy(Heap* heap) {
    ChunkHeap* self = HEAP_DYNAMIC_CAST(heap, ChunkHeap);
    MJC_ASSERT(self != NULL);

    // Free memory used for linked list
    linklist_destroy(&self->blocks);
    // Release memory chunk
    free(self->begin);

    // Just in case
    self->begin = NULL;
    self->size = 0;
}

/**
 * @brief Allocate memory block from this heap
 *
 * @param heap Chunk heap
 * @param size Size of allocation
 * @return void* Memory block
 */
void* chunkheap_alloc(Heap* heap, u32 size) {
    ChunkHeap* self = HEAP_DYNAMIC_CAST(heap, ChunkHeap);
    MJC_ASSERT(self != NULL);

    // Align allocations to 4 bytes
    size = ROUND_UP(size, 4);

    // Find the smallest block that can fit this allocation
    ChunkBlock* bestBlock = NULL;
    LinkNode* bestBlockNode = NULL;

    // clang-format off
    LINKLIST_FOREACH(&self->blocks, ChunkBlock*,
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
    ChunkBlock* otherPart = MJC_ALLOC_OBJ(ChunkBlock);
    MJC_ASSERT(otherPart != NULL);
    otherPart->begin = bestBlock->begin + size;
    otherPart->size = bestBlock->size - size;
    otherPart->alloced = FALSE;

    // By inserting this new node right after the one we split from, the list
    // remains sorted!
    linklist_insert(&self->blocks, bestBlockNode, otherPart);

    bestBlock->size = size;
    bestBlock->alloced = TRUE;

    // Strip ChunkBlock "header" for user
    return bestBlock->begin;
}

/**
 * @brief Free memory block to this heap
 *
 * @param heap Chunk heap
 * @param block Memory block
 */
void chunkheap_free(Heap* heap, void* block) {
    ChunkHeap* self = HEAP_DYNAMIC_CAST(heap, ChunkHeap);
    MJC_ASSERT(self != NULL);

    MJC_ASSERT_MSG((u8*)block >= (u8*)self->begin &&
                       (u8*)block < (u8*)self->begin + self->size,
                   "Wrong heap pal!!!");

    // Find the chunk block that contains this memory
    ChunkBlock* parent = NULL;

    // clang-format off
    LINKLIST_FOREACH(&self->blocks, ChunkBlock*,
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

    // We know this memory block is from this chunk,
    // so there *must* be a parent ChunkBlock.
    MJC_ASSERT(parent != NULL);

    // Free block
    parent->alloced = FALSE;

    // TODO: Coalesce free blocks (not really needed for this project)
}

/**
 * @brief Check whether an address is an object
 *
 * @param heap Chunk heap
 * @param addr Address
 */
BOOL chunkheap_is_object(const Heap* heap, void* addr) {
    const ChunkHeap* self = HEAP_DYNAMIC_CAST(heap, ChunkHeap);
    MJC_ASSERT(self != NULL);

    if (addr == NULL) {
        return FALSE;
    }

    // clang-format off
    LINKLIST_FOREACH(&self->blocks, const ChunkBlock*,
        if ((u8*)addr == ELEM->begin) {
            return TRUE;
        }
    )
    // clang-format on

    return FALSE;
}

/**
 * @brief Dump contents of this heap
 *
 * @param heap Chunk heap
 */
void chunkheap_dump(const Heap* heap) {
    const ChunkHeap* self = HEAP_DYNAMIC_CAST(heap, ChunkHeap);
    MJC_ASSERT(self != NULL);

    MJC_LOG("Chunk heap: %p\n", self);

    // Chunk configuration
    MJC_LOG("    self->begin: %p\n", self->begin);
    MJC_LOG("    self->size: %08X\n", self->size);

    // Chunk blocks
    int i = 0;
    MJC_LOG("    self->blocks = {\n");

    // clang-format off
    LINKLIST_FOREACH(&self->blocks, const ChunkBlock*,
        MJC_LOG("        {\n");
        MJC_LOG("            no: %d\n",      i++);
        MJC_LOG("            begin: %p\n",   ELEM->begin);
        MJC_LOG("            size: %08X\n",  ELEM->size);
        MJC_LOG("            alloced: %s\n", ELEM->alloced ? "true" : "false");
        
        // Chunks can contain objects
        if (ELEM->alloced) {
            MJC_LOG("            object: ");
            heap_dump_object((const Object*)ELEM->begin);
        }
        
        MJC_LOG("        },\n");
    )
    // clang-format on

    MJC_LOG("    }\n");
}
