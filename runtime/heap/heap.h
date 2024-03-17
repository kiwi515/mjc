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

#define HEAP_MAX_AGE 4 // See Object::age

/**
 * @brief Garbage-collectible object header
 */
typedef struct Object {
    /* 0x0 */ u32 size;              // Size of this object
    /* 0x4 */ s32 marked : 1;        // Mark bit (mark-sweep GC)
    /* 0x4 */ s32 age : 2;           // Age bits (generational GC)
    /* 0x4 */ volatile s32 ref : 29; // Reference count (reference count GC)
    /* 0x8 */ u8 data[];             // Object data
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
    HeapType type;    // Type of this heap
    LinkList objects; // Live objects on this heap

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
