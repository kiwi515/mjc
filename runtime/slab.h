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

/**
 * @brief One big "slab" of memory
 */
typedef struct Slab {
    // List of available blocks (chunks of memory)
    LinkList blocks;
} Slab;

/**
 * @brief Block from which memory can be allocated
 */
typedef struct SlabBlock {
    void* begin;
    u32 size;
    BOOL alloced;
} SlabBlock;

Slab* slab_create(u32 size);
void* slab_alloc(Slab* slab, u32 size);
void slab_free(Slab* slab, void* block);

#endif
