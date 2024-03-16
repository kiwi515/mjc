/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Author:  Tyler Gutowski, tgutowski2020@my.fit.edu
 * Course:  CSE 4101, Fall 2023
 * Project: Heap Heap Hooray
 * Charset: US-ASCII
 */

#include "heap/heap.h"
#include "runtime.h"
#include <stdlib.h>

/**
 * @brief Derive header from a memory block pointer
 *
 * @param block Memory block
 */
Object* heap_get_object(void* block) {
    MJC_ASSERT(block != NULL);

    // Header is placed before block contents
    return (Object*)((u8*)block - sizeof(Object));
}

/**
 * @brief Log object record to the console
 *
 * @param obj Object
 */
void heap_dump_object(const Object* obj) {
    MJC_ASSERT(obj != NULL);

    MJC_LOG("addr:%p size:%d marked:%d, ref:%d\n", obj, obj->size, obj->marked,
            obj->ref);
}

/**
 * @brief Allocate memory from the specified heap
 *
 * @param heap Heap to use
 * @param size Size of allocation
 * @return void* Memory block
 */
void* heap_alloc(Heap* heap, u32 size) {
    MJC_ASSERT(heap != NULL);
    MJC_ASSERT(size > 0);
    MJC_ASSERT_MSG(heap->_alloc != NULL, "Missing alloc function");

    // Need extra space for header
    u32 full_size = size + sizeof(Object);

    // First attempt to allocate
    Object* obj = heap->_alloc(heap, full_size);

    // Run GC cycle if we need more space
    if (obj == NULL) {
        MJC_LOG("cant alloc %08X bytes, forcing gc cycle\n", full_size);
        runtime_collect();
    }

    // Last attempt to allocate
    obj = heap->_alloc(heap, full_size);

    // Terminate program, we are out of memory
    if (obj == NULL) {
        MJC_LOG("cant alloc %08X!!!\n", full_size);
        exit(EXIT_FAILURE);
        return NULL;
    }

    // Fill out object header
    obj->size = size;
    obj->marked = FALSE;
    obj->ref = 0;

    MJC_LOG("alloc %p (size:%d), userptr: %p\n", obj, size, obj->data);
    linklist_append(&heap->objects, obj);

    // Header is hidden from the user
    return obj->data;
}

/**
 * @brief Release memory to the specified heap
 *
 * @param heap Heap to use
 * @param obj Object to free
 */
void heap_free(Heap* heap, Object* obj) {
    MJC_ASSERT(heap != NULL);
    MJC_ASSERT(obj != NULL);
    MJC_ASSERT(heap->_free != NULL);

    linklist_remove(&heap->objects, obj);
    heap->_free(heap, obj);
}

/**
 * @brief Check whether an address is an object
 *
 * @param heap Heap to use
 * @param addr Address
 */
BOOL heap_is_object(const Heap* heap, const void* addr) {
    MJC_ASSERT(heap != NULL);
    MJC_ASSERT(heap->_is_object != NULL);

    return heap->_is_object(heap, addr);
}

/**
 * @brief Dump contents of this heap
 *
 * @param heap Heap to use
 */
void heap_dump(const Heap* heap) {
    MJC_ASSERT(heap != NULL);
    MJC_ASSERT(heap->_dump != NULL);

    heap->_dump(heap);
}

/**
 * @brief Destroy this heap
 *
 * @param heap Heap to use
 */
void heap_destroy(Heap* heap) {
    MJC_ASSERT(heap != NULL);
    MJC_ASSERT(heap->_destroy != NULL);

    heap->_destroy(heap);
}