/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Author:  Tyler Gutowski, tgutowski2020@my.fit.edu
 * Course:  CSE 4101, Fall 2023
 * Project: Heap Heap Hooray
 * Charset: US-ASCII
 */

#include "heap/chunk_heap.h"
#include <string.h>

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
    memset(self, 0, sizeof(ChunkHeap));
    self->base.type = HeapType_ChunkHeap;

    // Register heap functions
    self->base._destroy = chunkheap_destroy;
    self->base._alloc = chunkheap_alloc;
    self->base._free = chunkheap_free;
    self->base._dump = chunkheap_dump;

    // Underlying memory
    void* begin = MJC_ALLOC(size);
    MJC_ASSERT(begin != NULL);
    memset(begin, 0, size);

    // Important for copying: This tells us the range of the whole thing
    self->begin = (u8*)begin;
    self->size = size;

    // Initially we have one big, continuous block
    ChunkBlock* block = MJC_ALLOC_OBJ(ChunkBlock);
    MJC_ASSERT(block != NULL);
    memset(block, 0, sizeof(ChunkBlock));
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

    // Release block structures
    // clang-format off
    LINKLIST_FOREACH(&self->blocks, ChunkBlock*,
        MJC_FREE(ELEM);
    );
    // clang-format on

    // Release list memory
    linklist_destroy(&self->blocks);

    // Release memory chunk
    MJC_ASSERT(self->begin != NULL);
    MJC_FREE(self->begin);

    MJC_FREE(self);
}

/**
 * @brief Allocate object from this heap
 *
 * @param heap Chunk heap
 * @param size Size of allocation
 * @return Object* New object
 */
Object* chunkheap_alloc(Heap* heap, u32 size) {
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
    MJC_ASSERT(!bestBlock->alloced);

    if (bestBlock->size != size) {
        // The block is bigger than what we need, so we need to break off a
        // piece. (This is done by creating a new block with the remaining size)
        ChunkBlock* otherPart = MJC_ALLOC_OBJ(ChunkBlock);
        MJC_ASSERT(otherPart != NULL);
        memset(otherPart, 0, sizeof(ChunkBlock));
        otherPart->begin = bestBlock->begin + size;
        otherPart->size = bestBlock->size - size;
        otherPart->alloced = FALSE;

        // By inserting this new node right after the one we split from, the
        // list remains sorted!
        linklist_insert(&self->blocks, bestBlockNode, otherPart);
    }

    bestBlock->size = size;
    bestBlock->alloced = TRUE;

    return (Object*)bestBlock->begin;
}

/**
 * @brief Free object to this heap
 *
 * @param heap Chunk heap
 * @param obj Object to free
 */
void chunkheap_free(Heap* heap, Object* obj) {
    ChunkHeap* self = HEAP_DYNAMIC_CAST(heap, ChunkHeap);
    MJC_ASSERT(self != NULL);

    MJC_ASSERT_MSG((u8*)obj >= (u8*)self->begin &&
                       (u8*)obj < (u8*)self->begin + self->size,
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
        if ((u8*)obj >= (u8*)ELEM->begin &&
            (u8*)obj < (u8*)ELEM->begin + ELEM->size) {
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
 * @brief Dump contents of this heap
 *
 * @param heap Chunk heap
 */
void chunkheap_dump(const Heap* heap) {
    const ChunkHeap* self = HEAP_DYNAMIC_CAST(heap, ChunkHeap);
    MJC_ASSERT(self != NULL);

    MJC_LOG("Chunk heap: %p:\n", self);

    // Chunk configuration
    MJC_LOG("self->begin: %p\n", self->begin);
    MJC_LOG("self->size: %08X\n", self->size);

    // Chunk blocks
    int i = 0;
    MJC_LOG("self->blocks = {\n");

    // clang-format off
    LINKLIST_FOREACH(&self->blocks, const ChunkBlock*,
        MJC_LOG("    {\n");
        MJC_LOG("        no: %d\n",      i++);
        MJC_LOG("        begin: %p\n",   ELEM->begin);
        MJC_LOG("        size: %08X\n",  ELEM->size);
        MJC_LOG("        alloced: %s\n", ELEM->alloced ? "true" : "false");
        
        // Chunks can contain objects
        if (ELEM->alloced && ELEM->begin != NULL) {
            MJC_LOG("        object: ");
            heap_dump_object((const Object*)ELEM->begin);
        }
        
        MJC_LOG("    },\n");
    )
    // clang-format on

    MJC_LOG("}\n");
}

/**
 * @brief Move live allocations of one chunk heap to another
 * @note DON'T FORGET TO FIRST USE __marksweep_mark!!!
 *
 * @param src Source heap (move from)
 * @param dst Destination heap (move to)
 */
void chunkheap_purify(Heap* src, Heap* dst) {
    MJC_ASSERT(src != NULL);
    MJC_ASSERT(dst != NULL);

    ChunkHeap* csrc = HEAP_DYNAMIC_CAST(src, ChunkHeap);
    MJC_ASSERT_MSG(csrc != NULL, "Source heap is not a chunk heap");

    ChunkHeap* cdst = HEAP_DYNAMIC_CAST(dst, ChunkHeap);
    MJC_ASSERT_MSG(cdst != NULL, "Destination heap is not a chunk heap");

    // clang-format off
    LINKLIST_FOREACH(&csrc->blocks, const ChunkBlock*,
        // Don't need to copy unused blocks
        if (!ELEM->alloced) {
            continue;
        }

        // Object will begin at the block data
        Object* obj = (Object*)ELEM->begin;

        // If alloced was set, this must be a real Object
        MJC_ASSERT_MSG(heap_is_object(src, ELEM->begin),
                      "Block is incorrectly marked as alloced");

        // Don't copy garbage
        if (!obj->marked) {
            continue;
        }

        // We do a little hack to copy the *contents* while not copying the header.
        MJC_LOG("copying object at %p\n", obj);
        const u8* contentBegin = ELEM->begin + sizeof(Object);
        u32 contentSize = ELEM->size - sizeof(Object);

        // Create a new header for the content and copy over the Object
        heap_dump(dst);
        void* block = heap_alloc(dst, contentSize);
        heap_dump(dst);

        MJC_ASSERT(block != NULL);
        memcpy(block, contentBegin, contentSize);

        // Free the old block (making the operation a move, not a copy)
        heap_free(src, obj);
    )
    // clang-format on
}
