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
 * @brief Base heap structure
 */
typedef struct Heap {
    // Allocate memory block from this heap
    void* (*alloc)(struct Heap* heap, u32 size);
    // Free memory block to this heap
    void (*free)(struct Heap* heap, void* block);
    // Check whether an address is an object
    BOOL (*is_object)(struct Heap* heap, void* addr);
    // Dump contents of this heap
    void (*dump)(struct Heap* heap);
    // Destroy this heap
    void (*destroy)(struct Heap* heap);
} Heap;

Object* heap_get_object(void* block);
void heap_dump_object(const Object* obj);

void* heap_alloc(Heap* heap, u32 size);
void heap_free(Heap* heap, void* block);

#endif
