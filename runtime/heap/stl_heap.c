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

    // Register heap functions
    self->base.destroy = stlheap_destroy;
    self->base.alloc = stlheap_alloc;
    self->base.free = stlheap_free;
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
void stlheap_destroy(struct Heap* heap) {
    MJC_ASSERT(heap != NULL);
    StlHeap* self = (StlHeap*)heap;

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
void* stlheap_alloc(struct Heap* heap, u32 size) {
    MJC_ASSERT(heap != NULL);
    StlHeap* self = (StlHeap*)heap;

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
void stlheap_free(struct Heap* heap, void* block) {
    MJC_ASSERT(heap != NULL);
    StlHeap* self = (StlHeap*)heap;

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
BOOL stlheap_is_object(struct Heap* heap, void* addr) {
    MJC_ASSERT(heap != NULL);
    StlHeap* self = (StlHeap*)heap;

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
void stlheap_dump(struct Heap* heap) {
    MJC_ASSERT(heap != NULL);
    StlHeap* self = (StlHeap*)heap;

    MJC_LOG("STL heap: %p:\n", self);
    MJC_LOG("self->objects:\n");

    // clang-format off
    LINKLIST_FOREACH(&self->objects, const Object*,
        // indent
        MJC_LOG("   ");
        heap_dump_object(ELEM);
    );
    // clang-format on
}
