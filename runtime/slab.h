/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Author:  Tyler Gutowski, tgutowski2020@my.fit.edu
 * Course:  CSE 4101, Fall 2023
 * Project: Heap Heap Hooray
 * Charset: US-ASCII
 */

#ifndef MINI_JAVA_COMPILER_SLAB_H
#define MINI_JAVA_COMPILER_SLAB_H
#include "linklist.h"
#include "types.h"

// Forward declarations
typedef struct HeapHeader;

/**
 * @brief One big "slab" of memory
 */
typedef struct Slab {
    // Memory owned by this slab (always contiguous)
    u8* begin;
    u32 size;
    // Slab is partitioned into blocks
    LinkList blocks;
} Slab;

/**
 * @brief Block from which memory can be allocated
 */
typedef struct SlabBlock {
    // Memory owned by this block
    // (If heap_is_header(begin), header is safe to use.)
    union {
        u8* begin;
        struct HeapHeader* header;
    };

    // Size of this block
    u32 size;

    // Whether this block is in use
    BOOL alloced;
} SlabBlock;

Slab* slab_create(u32 size);
void slab_destroy(Slab* slab);
void* slab_alloc(Slab* slab, u32 size);
void slab_free(Slab* slab, void* block);
void slab_dump(const Slab* slab);

struct HeapHeader* slab_block_get_header(const SlabBlock* block);
void* slab_block_get_contents(const SlabBlock* block);

#endif
