/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Author:  Tyler Gutowski, tgutowski2020@my.fit.edu
 * Course:  CSE 4101, Fall 2023
 * Project: Heap Heap Hooray
 * Charset: US-ASCII
 */

#ifndef MINI_JAVA_COMPILER_HEAP_STLHEAP_H
#define MINI_JAVA_COMPILER_HEAP_STLHEAP_H
#include "heap.h"
#include "linklist.h"
#include "types.h"

/**
 * @brief Standard-library (STL) backed heap
 */
typedef struct StlHeap {
    // Common heap structure
    Heap base;
    // List of live objects
    LinkList objects;
} StlHeap;

Heap* stlheap_create(void);
void stlheap_destroy(Heap* heap);
Object* stlheap_alloc(Heap* heap, u32 size);
void stlheap_free(Heap* heap, Object* obj);
BOOL stlheap_is_object(const Heap* heap, const void* addr);
void stlheap_dump(const Heap* heap);

#endif
