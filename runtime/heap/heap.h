/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Author:  Tyler Gutowski, tgutowski2020@my.fit.edu
 * Course:  CSE 4101, Fall 2023
 * Project: Heap Heap Hooray
 * Charset: US-ASCII
 */

#ifndef MINI_JAVA_COMPILER_HEAP_HEAP_H
#define MINI_JAVA_COMPILER_HEAP_HEAP_H
#include "linklist.h"
#include "types.h"

/**
 * @brief Garbage-collectible object header
 */
typedef struct Object {
    // Size of this object
    u32 size; // at 0x0

    // Mark bit (for mark-sweep GC)
    s32 marked : 1; // at 0x4
    // Reference count (for reference count GC)
    volatile s32 ref : 31; // at 0x4

    // Object data
    u8 data[]; // at 0x8
} Object;

/**
 * @brief Heap types
 */
typedef enum HeapType {
    HeapType_StlHeap,   // malloc/free
    HeapType_ChunkHeap, // contiguous
    HeapType_BuddyHeap, // 2^n lists

    HeapType_Max
} HeapType;

/**
 * @brief Base heap structure
 */
typedef struct Heap {
    // Type of this heap
    HeapType type;
    // Live objects on this heap
    LinkList objects;

    /**
     * Use heap_* family of functions! Don't call these directly!!!
     */
    // Allocate object from this heap
    Object* (*_alloc)(struct Heap* heap, u32 size);
    // Free object to this heap
    void (*_free)(struct Heap* heap, Object* obj);
    // Dump contents of this heap
    void (*_dump)(const struct Heap* heap);
    // Destroy this heap
    void (*_destroy)(struct Heap* heap);
} Heap;

/**
 * @brief "Dynamic" cast from Heap to subtypes by checking the 'type' field.
 * @details Returns NULL if cast is not possible.
 * @note Heap type is only checked when compiling for debug
 */
#define HEAP_DYNAMIC_CAST(heap, T)                                             \
    (MJC_ASSERT((heap)->type == HeapType_##T),                                 \
     (heap) != NULL ? (T*)(heap) : NULL)

Object* heap_get_object(void* block);
void heap_dump_object(const Object* obj);

void* heap_alloc(Heap* heap, u32 size);
void heap_free(Heap* heap, Object* obj);

BOOL heap_is_object(const Heap* heap, const void* addr);
void heap_dump(const Heap* heap);

void heap_destroy(Heap* heap);

#endif
