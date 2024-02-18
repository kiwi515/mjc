/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Author:  Tyler Gutowski, tgutowski2020@my.fit.edu
 * Course:  CSE 4101, Fall 2023
 * Project: Heap Heap Hooray
 * Charset: US-ASCII
 */

#ifndef MINI_JAVA_COMPILER_HEAP_H
#define MINI_JAVA_COMPILER_HEAP_H
#include "linklist.h"
#include "slab.h"
#include "types.h"

/**
 * @brief Heap block header
 */
typedef struct HeapHeader {
    // Size of this allocation
    u32 size; // at 0x0

    // Mark bit (for mark-sweep GC)
    s32 marked : 1; // at 0x4
    // Reference count (for reference count GC)
    volatile s32 ref : 31; // at 0x4

    // Block data
    u8 data[]; // at 0x8
} HeapHeader;

// Linked list of heap allocations
extern LinkList heap_list;

HeapHeader* heap_get_header(const void* block);
BOOL heap_is_header(const void* addr);

void* heap_alloc(u32 size);
void heap_free(void* block, BOOL recurse);
BOOL heap_contains(const void* addr);
void heap_dump(void);

void* heap_alloc_ex(Slab* slab, u32 size);
void heap_free_ex(Slab* slab, void* block, BOOL recurse);

#endif
