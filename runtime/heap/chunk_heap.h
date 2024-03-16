/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Author:  Tyler Gutowski, tgutowski2020@my.fit.edu
 * Course:  CSE 4101, Fall 2023
 * Project: Heap Heap Hooray
 * Charset: US-ASCII
 */

#ifndef MINI_JAVA_COMPILER_HEAP_CHUNKHEAP_H
#define MINI_JAVA_COMPILER_HEAP_CHUNKHEAP_H
#include "heap/heap.h"
#include "linklist.h"
#include "types.h"

/**
 * @brief Memory chunk (previously "slab") backed heap
 */
typedef struct ChunkHeap {
    // Common heap structure
    Heap base;
    // Memory chunk (always contiguous)
    u8* begin;
    u32 size;
    // Chunk is partitioned into blocks
    LinkList blocks;
} ChunkHeap;

Heap* chunkheap_create(u32 size);
void chunkheap_destroy(Heap* heap);
Object* chunkheap_alloc(Heap* heap, u32 size);
void chunkheap_free(Heap* heap, Object* block);
BOOL chunkheap_is_object(const Heap* heap, const void* addr);
void chunkheap_dump(const Heap* heap);

void chunkheap_purify(Heap* src, Heap* dst);

#endif
