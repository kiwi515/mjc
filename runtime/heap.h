/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Course:  CSE 4251, Section 01, Spring 2023
 * Project: MiniJava Compiler Project
 * Charset: US-ASCII
 */

#ifndef MINI_JAVA_COMPILER_HEAP_H
#define MINI_JAVA_COMPILER_HEAP_H
#include "types.h"
#include <stddef.h>

// 32-bit "tag" to identify heap blocks from all other memory
#define HEAP_BLOCK_TAG 0x48424C4B // 'HBLK'

/**
 * @brief Heap block header
 */
typedef struct HeapHeader {
    // Block identification
    u32 tag; // at 0x0

    // Intrusive, doubly-linked list
    struct HeapHeader* next; // at 0x4
    struct HeapHeader* prev; // at 0x8

    // Size of this allocation
    u32 size; // at 0xC

    // Mark bit (for mark-sweep GC)
    s32 marked : 1; // at 0x10
    // Reference count (for reference count GC)
    volatile s32 ref : 31; // at 0x10

    // Block data
    u8 data[]; // at 0x14
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
