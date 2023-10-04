/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Course:  CSE 4251, Section 01, Spring 2023
 * Project: MiniJava Compiler Project
 * Charset: US-ASCII
 */

#include "runtime.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

// 32-bit "tag" to identify heap blocks from all other memory
#define HEAP_BLOCK_TAG 'HBLK'

/**
 * @brief Heap block header
 */
typedef struct HeapHeader {
    // Block identification
    u32 tag; // at 0x0

    // Next block in list of runtime allocations
    struct HeapHeader* next; // at 0x4
    // Size of this allocation
    u32 size; // at 0x8

    // Mark bit (for mark-sweep GC)
    s32 marked : 1; // at 0xC
    // Reference count (for reference count GC)
    s32 ref : 31; // at 0xC

    // Block user data begins at offset 0x10 . . .
} HeapHeader;

/**
 * @brief Array header
 */
typedef struct ArrayHeader {
    // Array length
    u32 length; // at 0x0
    // Array data
    u8 data[]; // at 0x4
} ArrayHeader;

/**
 * @brief Allocate memory for a MiniJava object
 *
 * @param sz Memory block size
 * @return void* Memory block
 */
void* runtime_alloc_object(u32 sz) {
    void* block;
    HeapHeader* header;

    // Allocate memory block
    block = malloc(sz + sizeof(HeapHeader));
    header = (HeapHeader*)block;

    // Zero-initialize memory block
    if (block != NULL) {
        memset(block, 0, sz);
    }

    // Initialize block header
    header->tag = HEAP_BLOCK_TAG;
    header->next = NULL;
    header->size = sz;
    header->marked = FALSE;
    header->ref = 0;

    return block;
}

/**
 * @brief Allocate memory for a MiniJava array
 *
 * @param sz Element size
 * @param n Number of elements
 * @return void* Memory block
 */
void* runtime_alloc_array(u32 sz, u32 n) {
    void* block;
    ArrayHeader* header;

    // Allocate memory block
    block = runtime_alloc_object(sz * n + sizeof(ArrayHeader));

    // Set array length
    if (block != NULL) {
        header = (ArrayHeader*)block;
        header->length = n;
    }

    return block;
}

/**
 * @brief Print integer value to the console
 */
void runtime_print_integer(s32 value) { printf("%d\n", value); }

/**
 * @brief Print boolean value to the console
 */
void runtime_print_boolean(s32 value) {
    printf("%s\n", value ? "True" : "False");
}
