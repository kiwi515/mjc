/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Author:  Tyler Gutowski, tgutowski2020@my.fit.edu
 * Course:  CSE 4101, Fall 2023
 * Project: Heap Heap Hooray
 * Charset: US-ASCII
 */

#include "heap.h"
#include "config.h"
#include "linklist.h"
#include "marksweep.h"
#include "refcount.h"
#include "runtime.h"
#include "slab.h"
#include <stdlib.h>
#include <string.h>

// Linked list of heap allocations
LinkList heap_list;

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
    LinkNode* iter;
    HeapHeader* header;

    // Null pointer
    if (addr == NULL) {
        return FALSE;
    }

    // Iterate over all heap allocations
    for (iter = heap_list.head; iter != NULL; iter = iter->next) {
        header = (HeapHeader*)iter->object;

        // Check if the specified address is the start of any allocation
        if ((u32)addr == (u32)header) {
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
    return heap_alloc_ex(NULL, size);
}

/**
 * @brief Free memory block back to the heap
 *
 * @param block Memory block
 * @param recurse Whether to recurse
 */
void heap_free(void* block, BOOL recurse) {
    return heap_free_ex(NULL, block, recurse);
}

/**
 * @brief Test whether a given address resides in the heap
 *
 * @param addr Memory address
 * @return BOOL Whether addr is a valid pointer to heap-memory
 */
BOOL heap_contains(const void* addr) {
    LinkNode* iter;
    HeapHeader* header;

    // Iterate over all heap allocations
    for (iter = heap_list.head; iter != NULL; iter = iter->next) {
        header = (HeapHeader*)iter->object;

        // Check if the specified address resides in this allocation
        if ((u32)addr >= (u32)header &&
            (u32)addr < (u32)header + (sizeof(HeapHeader) + header->size)) {
            return TRUE;
        }
    }

    return FALSE;
}

/**
 * @brief Dump contents of the heap (for debug)
 */
void heap_dump(void) {
    LinkNode* iter;
    HeapHeader* header;

    DEBUG_LOG("[heap] alloced:\n");

    for (iter = heap_list.head; iter != NULL; iter = iter->next) {
        header = (HeapHeader*)iter->object;
        DEBUG_LOG("[heap]    addr:%p size:%d ref:%d\n", header, header->size,
                  header->ref);
    }
}

/**
 * @brief Allocate memory from a specific slab
 *
 * @param slab Slab to use (NULL -> use system heap)
 * @param size Allocation size
 * @return void* Memory block
 */
void* heap_alloc_ex(Slab* slab, u32 size) {
    HeapHeader* header;

    // Extra space for block header
    const u32 internal_size = size + sizeof(HeapHeader);

    // Allocate memory block
    header =
        slab != NULL ? slab_alloc(slab, internal_size) : malloc(internal_size);

    // If allocation fails, force a GC cycle
    if (header == NULL) {
        DEBUG_LOG("[heap] running a gc cycle to free memory\n");
        runtime_do_gc_cycle();

        // Try allocation one last time
        header = slab != NULL ? slab_alloc(slab, internal_size)
                              : malloc(internal_size);
        if (header == NULL) {
            // Out of memory, terminate the program
            DEBUG_LOG("[heap] cannot allocate %u from heap\n", internal_size);
            exit(EXIT_FAILURE);
            return NULL;
        }
    }

    DEBUG_LOG("[heap] alloc %p (size:%d), userptr: %p\n", header, size,
              header->data);

    // Zero-initialize block
    memset(header, 0, internal_size);

    // Fill out block header structure
    header->size = size;
    header->marked = FALSE;
    header->ref = 0;

    // Add to runtime list
    linklist_append(&heap_list, header);

    // Header is hidden from user
    return header->data;
}

/**
 * @brief Free memory block to a specific slab
 *
 * @param slab Slab to use (NULL -> system heap)
 * @param block Memory block
 * @param recurse Whether to recurse
 */
void heap_free_ex(Slab* slab, void* block, BOOL recurse) {
    HeapHeader* header;

    assert(block != NULL);

    header = heap_get_header(block);

    // Decrement refcount of children
    if (recurse) {
        if (config_get_gctype() == GCType_Refcount) {
            refcount_decr_children(header);
        }
    }

    // Remove from runtime list
    linklist_remove(&heap_list, header);

    // Release memory
    if (slab != NULL) {
        slab_free(slab, block);
    } else {
        free(block);
    }
}