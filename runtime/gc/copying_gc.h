/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Author:  Tyler Gutowski, tgutowski2020@my.fit.edu
 * Course:  CSE 4101, Fall 2023
 * Project: Heap Heap Hooray
 * Charset: US-ASCII
 */

#ifndef MINI_JAVA_COMPILER_GC_COPYINGGC_H
#define MINI_JAVA_COMPILER_GC_COPYINGGC_H
#include "gc/gc.h"
#include "types.h"

// Forward declarations
typedef struct Heap;

/**
 * @brief Copying GC
 */
typedef struct CopyingGC {
    GC base;              // Common GC structure
    struct Heap* to_heap; // "To" heap
} CopyingGC;

GC* copying_create(void);
void copying_destroy(GC* gc);

void copying_collect(GC* gc);
void copying_stack_push(GC* gc, void* frame, u32 size);
void copying_stack_pop(GC* gc);

#endif
