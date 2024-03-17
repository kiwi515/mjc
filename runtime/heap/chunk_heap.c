/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Author:  Tyler Gutowski, tgutowski2020@my.fit.edu
 * Course:  CSE 4101, Fall 2023
 * Project: Heap Heap Hooray
 * Charset: US-ASCII
 */

#include "heap/chunk_heap.h"
#include "runtime.h"
#include "stackframe.h"
#include <string.h>

typedef struct ChunkBlock {
    u8* begin;    // Block start
    u32 size;     // Block size
    BOOL alloced; // Whether this block is in use
} ChunkBlock;

// Forward declarations
static void __chunkheap_fix_obj(void* arg, Object* obj, void** pp_obj);

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

    // Release structures
    // clang-format off
    LINKLIST_FOREACH(&self->blocks, ChunkBlock*,
        MJC_FREE(ELEM);
    );
    LINKLIST_FOREACH(&self->mappings, ChunkMapping*,
        MJC_FREE(ELEM);
    );
    // clang-format on

    // Release list memory
    linklist_destroy(&self->blocks);
    linklist_destroy(&self->mappings);

    // Release chunk
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

    // Find the smallest block that can fit this allocation
    ChunkBlock* best_block = NULL;
    LinkNode* best_node = NULL;

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
        if (best_block == NULL || ELEM->size < best_block->size) {
            best_block = ELEM;
            best_node = NODE;
        }
    );
    // clang-format on

    // No available block, we need to GC
    if (best_block == NULL) {
        return NULL;
    }

    // Sanity check
    MJC_ASSERT(best_block->size >= size);
    MJC_ASSERT(best_block->begin != NULL);
    MJC_ASSERT(!best_block->alloced);

    if (best_block->size != size) {
        // The block is bigger than what we need, so we need to break off a
        // piece. (This is done by creating a new block with the remaining size)
        ChunkBlock* otherPart = MJC_ALLOC_OBJ(ChunkBlock);
        MJC_ASSERT(otherPart != NULL);
        memset(otherPart, 0, sizeof(ChunkBlock));
        otherPart->begin = best_block->begin + size;
        otherPart->size = best_block->size - size;
        otherPart->alloced = FALSE;

        // By inserting this new node right after the one we split from, the
        // list remains sorted!
        linklist_insert(&self->blocks, best_node, otherPart);
    }

    best_block->size = size;
    best_block->alloced = TRUE;

    return (Object*)best_block->begin;
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
 * @brief Copy live allocations of one chunk heap to another
 * @note References are updated to access the copies in the destination heap
 *
 * @param src Source heap (copy from)
 * @param dst Destination heap (copy to)
 *
 * @return Whether the destination heap had enough room for ALL live allocations
 * from the source heap
 */
BOOL chunkheap_purify(Heap* src, Heap* dst) {
    MJC_ASSERT(src != NULL);
    MJC_ASSERT(dst != NULL);

    ChunkHeap* csrc = HEAP_DYNAMIC_CAST(src, ChunkHeap);
    MJC_ASSERT_MSG(csrc != NULL, "Source heap is not a chunk heap");

    ChunkHeap* cdst = HEAP_DYNAMIC_CAST(dst, ChunkHeap);
    MJC_ASSERT_MSG(cdst != NULL, "Destination heap is not a chunk heap");

    // Clear mappings from last time
    linklist_destroy(&csrc->mappings);

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

        // Create a new header for the content and copy over the object
        void* block = heap_alloc(dst, contentSize);
        if (block == NULL) {
            return FALSE;
        }
        memcpy(block, contentBegin, contentSize);

        // Create mapping for pointer redirection
        ChunkMapping* map = MJC_ALLOC_OBJ(ChunkMapping);
        MJC_ASSERT(map != NULL);
        memset(map, 0, sizeof(ChunkMapping));
        map->from = obj;
        map->to = block;
        linklist_append(&csrc->mappings, map);
    )
    // clang-format on

    // Fix object pointers that were changed
    stackframe_traverse(__chunkheap_fix_obj, src);

    return TRUE;
}

/**
 * @brief Repair object pointers after copying (stack traversal function)
 *
 * @param arg User argument (optional)
 * @param obj Heap object that was found
 * @param pp_obj Address of the pointer to the object
 */
static void __chunkheap_fix_obj(void* arg, Object* obj, void** pp_obj) {
    MJC_ASSERT(obj != NULL);
    MJC_ASSERT(pp_obj != NULL);

    ChunkHeap* src = HEAP_DYNAMIC_CAST((Heap*)arg, ChunkHeap);
    MJC_ASSERT(src != NULL);

    // Nothing that was just copied over should be reachable yet.
    // Therefore, this reference must be from the source heap:
    MJC_ASSERT(heap_is_object(&src->base, obj));

    // clang-format off
    const ChunkMapping* map = NULL;
    // Convert address in source heap -> address in destination heap
    LINKLIST_FOREACH(&src->mappings, const ChunkMapping*,
        if (ELEM->from == obj) {
            map = ELEM;
            break;
        }
    );
    // clang-format on

    // Overwrite reference
    if (map != NULL) {
        *pp_obj = map->to;
        MJC_LOG("chunkheap fix %p -> %p\n", map->from, map->to);
    } else {
        MJC_LOG("chunkheap fix %p -> NONE\n", obj);
    }
}