/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Author:  Tyler Gutowski, tgutowski2020@my.fit.edu
 * Course:  CSE 4101, Fall 2023
 * Project: Heap Heap Hooray
 * Charset: US-ASCII
 */

#include "heap/stl_heap.h"
#include <stdlib.h>

/**
 * @brief Create standard-library (STL) heap
 */
Heap* stlheap_create(void) {
    StlHeap* self = MJC_ALLOC_OBJ(StlHeap);
    MJC_ASSERT(self != NULL);
    self->base.type = HeapType_StlHeap;

    // Register heap functions
    self->base.destroy = stlheap_destroy;
    self->base.__alloc = stlheap_alloc;
    self->base.__free = stlheap_free;
    self->base.is_object = stlheap_is_object;
    self->base.dump = stlheap_dump;

    // Handle to basic heap
    return &self->base;
}

/**
 * @brief Destroy this heap
 *
 * @param heap STL heap
 */
void stlheap_destroy(Heap* heap) {
    StlHeap* self = HEAP_DYNAMIC_CAST(heap, StlHeap);
    MJC_ASSERT(self != NULL);

    // clang-format off
    LINKLIST_FOREACH(&self->objects, Object*,
        stlheap_free(&self->base, ELEM, TRUE);
    );
    // clang-format on
}

/**
 * @brief Allocate memory block from this heap
 *
 * @param heap STL heap
 * @param size Size of allocation
 * @return void* Memory block
 */
void* stlheap_alloc(Heap* heap, u32 size) {
    StlHeap* self = HEAP_DYNAMIC_CAST(heap, StlHeap);
    MJC_ASSERT(self != NULL);

    // STL provides malloc/free
    void* block = malloc(size);
    if (block == NULL) {
        return NULL;
    }

    // Need to track live objects
    linklist_append(&self->objects, block);
    return block;
}

/**
 * @brief Free memory block to this heap
 *
 * @param heap STL heap
 * @param block Memory block
 */
void stlheap_free(Heap* heap, void* block) {
    StlHeap* self = HEAP_DYNAMIC_CAST(heap, StlHeap);
    MJC_ASSERT(self != NULL);

    // Remove from live list
    MJC_ASSERT(block != NULL);
    Object* obj = heap_get_object(block);
    linklist_remove(&self->objects, obj);

    // STL provides malloc/free
    free(obj);
}

/**
 * @brief Check whether an address is an object
 *
 * @param heap STL heap
 * @param addr Address
 */
BOOL stlheap_is_object(const Heap* heap, void* addr) {
    const StlHeap* self = HEAP_DYNAMIC_CAST(heap, StlHeap);
    MJC_ASSERT(self != NULL);

    if (addr == NULL) {
        return FALSE;
    }

    // clang-format off
    LINKLIST_FOREACH(&self->objects, const Object*,
        if ((u8*)addr == (u8*)ELEM) {
            return TRUE;
        }
    );
    // clang-format on

    return FALSE;
}

/**
 * @brief Dump contents of this heap
 *
 * @param heap STL heap
 */
void stlheap_dump(const Heap* heap) {
    const StlHeap* self = HEAP_DYNAMIC_CAST(heap, StlHeap);
    MJC_ASSERT(self != NULL);

    MJC_LOG("STL heap: %p:\n", self);
    MJC_LOG("self->objects = {\n");

    // clang-format off
    LINKLIST_FOREACH(&self->objects, const Object*,
        // indent
        MJC_LOG("   ");
        heap_dump_object(ELEM);
    );
    // clang-format on

    MJC_LOG("    }\n");
}
