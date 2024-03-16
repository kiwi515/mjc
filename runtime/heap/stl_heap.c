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
    self->base._destroy = stlheap_destroy;
    self->base._alloc = stlheap_alloc;
    self->base._free = stlheap_free;
    self->base._is_object = stlheap_is_object;
    self->base._dump = stlheap_dump;

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
    LINKLIST_FOREACH(&self->base.objects, Object*,
        stlheap_free(heap, ELEM);
    );
    // clang-format on

    MJC_FREE(self);
}

/**
 * @brief Allocate object from this heap
 *
 * @param heap STL heap
 * @param size Size of allocation
 * @return Object* New object
 */
Object* stlheap_alloc(Heap* heap, u32 size) {
    StlHeap* self = HEAP_DYNAMIC_CAST(heap, StlHeap);
    MJC_ASSERT(self != NULL);

    // STL provides malloc/free
    Object* obj = (Object*)MJC_ALLOC(size);
    if (obj == NULL) {
        return NULL;
    }

    return obj;
}

/**
 * @brief Free object to this heap
 *
 * @param heap STL heap
 * @param obj Object to free
 */
void stlheap_free(Heap* heap, Object* obj) {
    StlHeap* self = HEAP_DYNAMIC_CAST(heap, StlHeap);
    MJC_ASSERT(self != NULL);

    // STL provides malloc/free
    MJC_FREE(obj);
}

/**
 * @brief Check whether an address is an object
 *
 * @param heap STL heap
 * @param addr Address
 */
BOOL stlheap_is_object(const Heap* heap, const void* addr) {
    const StlHeap* self = HEAP_DYNAMIC_CAST(heap, StlHeap);
    MJC_ASSERT(self != NULL);

    if (addr == NULL) {
        return FALSE;
    }

    // clang-format off
    LINKLIST_FOREACH(&self->base.objects, const Object*,
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
    LINKLIST_FOREACH(&self->base.objects, const Object*,
        // indent
        MJC_LOG("   ");
        heap_dump_object(ELEM);
    );
    // clang-format on

    MJC_LOG("}\n");
}
