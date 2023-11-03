/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Course:  CSE 4251, Section 01, Spring 2023
 * Project: MiniJava Compiler Project
 * Charset: US-ASCII
 */

#include "heap.h"
#include "refcount.h"
#include <assert.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

// Linked list of heap allocations
HeapHeader* heap_list_head = NULL;
HeapHeader* heap_list_tail = NULL;

/**
 * @brief Append new heap allocation to the runtime list
 *
 * @param header Block header
 */
static void list_append(HeapHeader* header) {
    assert(header != NULL);

    if (heap_list_head == NULL) {
        // Initialize list
        header->next = NULL;
        header->prev = NULL;
        heap_list_head = header;
        heap_list_tail = header;
    } else {
        // Extend list
        header->next = NULL;
        header->prev = heap_list_tail;
        heap_list_tail->next = header;
        heap_list_tail = header;
    }
}

/**
 * @brief Remove heap allocation from the runtime list
 *
 * @param header Block header
 */
static void list_remove(HeapHeader* header) {
    assert(header != NULL);

    // Handle next link
    if (header->next != NULL) {
        header->next->prev = header->prev;
    }
    // If next is NULL, this is the list tail
    else {
        assert(header == heap_list_tail);
        heap_list_tail = heap_list_tail->prev;
    }

    // Handle prev link
    if (header->prev != NULL) {
        header->prev->next = header->next;
    }
    // If prev is NULL, this is the list head
    else {
        assert(header == heap_list_head);
        heap_list_head = heap_list_head->next;
    }

    // Isolate node
    header->next = NULL;
    header->prev = NULL;
}

/**
 * @brief Derive header from a memory block pointer
 *
 * @param block Memory block
 */
HeapHeader* heap_get_header(const void* block) {
    assert(block != NULL);

    // Header is placed before block contents
    return (HeapHeader*)((char*)block - sizeof(HeapHeader));
}

/**
 * @brief Check if an address is a valid heap header
 *
 * @param addr Memory address
 * @return BOOL Whether addr points to a valid heap header
 */
BOOL heap_is_header(const void* addr) {
    HeapHeader* iter;

    // Null pointer
    if (addr == NULL) {
        return FALSE;
    }

    // Iterate over all heap allocations
    for (iter = heap_list_head; iter != NULL; iter = iter->next) {
        
        // Check if the specified address is the start of any allocation
        if ((u32)addr == (u32)iter->data) {
            return TRUE;
        }
    }

    return FALSE;
}

/**
 * @brief Allocate memory from the heap
 *
 * @param size Allocation size
 * @return void* Memory block
 */
void* heap_alloc(u32 size) {
    HeapHeader* header;

    // Extra space for block header
    const u32 internalSize = size + sizeof(HeapHeader);

    // Allocate memory block
    header = malloc(internalSize);
    if (header == NULL) {
        // TODO: Mark-sweep here
        DEBUG_LOG("[heap] cannot allocate %u from heap\n", internalSize);
        exit(EXIT_FAILURE);
        return NULL;
    }

    DEBUG_LOG("[heap] alloc %p (size:%d)\n", header, size);

    // Zero-initialize block
    memset(header, 0, internalSize);

    // Fill out block header structure
    header->size = size;
    header->marked = FALSE;
    header->ref = 0;

    // Add to runtime list
    list_append(header);

    // Header is hidden from user
    return header->data;
}

/**
 * @brief Free memory block back to the heap
 *
 * @param block Memory block
 */
void heap_free(void* block) {
    HeapHeader* header;

    assert(block != NULL);

    // Sanity check: block must be unreachable
    header = heap_get_header(block);
    assert(header->ref == 0);

    // Remove from runtime list
    list_remove(header);

    // Decrement refcount of children
    refcount_decr_children(header);

    // Release memory
    free(header);
}

/**
 * @brief Test whether a given address resides in the heap
 *
 * @param addr Memory address
 * @return BOOL Whether addr is a valid pointer to heap-memory
 */
BOOL heap_contains(const void* addr) {
    HeapHeader* iter;

    // Iterate over all heap allocations
    for (iter = heap_list_head; iter != NULL; iter = iter->next) {
        
        // Check if the specified address resides in this allocation
        if ((u32)addr >= (u32)iter &&
            (u32)addr < (u32)iter + (sizeof(HeapHeader) + iter->size)) {
            return TRUE;
        }
    }

    return FALSE;
}

/**
 * @brief Dump contents of the heap (for debug)
 */
void heap_dump(void) {
    HeapHeader* iter;

    DEBUG_LOG("[heap] alloced:\n");

    for (iter = heap_list_head; iter != NULL; iter = iter->next) {
        DEBUG_LOG("[heap]    addr:%p size:%d ref:%d\n", iter, iter->size,
                  iter->ref);
    }
}
