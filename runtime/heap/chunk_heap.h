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
 * @brief Mapping for fixing pointers
 */
typedef struct ChunkMapping {
    void* from;
    void* to;
} ChunkMapping;

/**
 * @brief Memory chunk (previously "slab") backed heap
 */
typedef struct ChunkHeap {
    Heap base;         // Common heap structure
    u8* begin;         // Chunk start
    u32 size;          // Chunk size
    LinkList blocks;   // Block partitions
    LinkList mappings; // Mappings for purify
} ChunkHeap;

Heap* chunkheap_create(u32 size);
void chunkheap_destroy(Heap* heap);
Object* chunkheap_alloc(Heap* heap, u32 size);
void chunkheap_free(Heap* heap, Object* block);
void chunkheap_dump(const Heap* heap);

void chunkheap_purify(Heap* src, Heap* dst);

#endif
