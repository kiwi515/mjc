/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Course:  CSE 4251, Section 01, Spring 2023
 * Project: MiniJava Compiler Project
 * Charset: US-ASCII
 */

#ifndef MINI_JAVA_COMPILER_HEAP_H
#define MINI_JAVA_COMPILER_HEAP_H
#include "types.h"

/**
 * @brief Heap block header
 */
typedef struct HeapHeader {
    // Intrusive, doubly-linked list
    struct HeapHeader* next; // at 0x0
    struct HeapHeader* prev; // at 0x4

    // Size of this allocation
    u32 size; // at 0x8

    // Mark bit (for mark-sweep GC)
    s32 marked : 1; // at 0xC
    // Reference count (for reference count GC)
    volatile s32 ref : 31; // at 0xC

    // Block data
    u8 data[]; // at 0x10
} HeapHeader;

// Linked list of heap allocations
extern HeapHeader* heap_list_head;
extern HeapHeader* heap_list_tail;

HeapHeader* heap_get_header(const void* block);
BOOL heap_is_header(const void* addr);
void* heap_alloc(u32 size);
void heap_free(void* block);
BOOL heap_contains(const void* addr);
void heap_dump(void);

#endif
