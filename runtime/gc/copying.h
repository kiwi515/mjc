/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Author:  Tyler Gutowski, tgutowski2020@my.fit.edu
 * Course:  CSE 4101, Fall 2023
 * Project: Heap Heap Hooray
 * Charset: US-ASCII
 */

#ifndef MINI_JAVA_COMPILER_GC_COPYING_H
#define MINI_JAVA_COMPILER_GC_COPYING_H
#include "gc/gc.h"
#include "types.h"

// Forward declarations
typedef struct Heap;

typedef struct CopyingGC {
    // Common GC structure
    GC base;
    // Built on mark-sweep GC
    GC* mark_sweep;
    // "To" heap
    struct Heap* to_heap;
} CopyingGC;

GC* copying_create(void);
void copying_destroy(GC* gc);

void copying_collect(GC* gc);
void copying_stack_push(GC* gc, const void* frame, u32 size);
void copying_stack_pop(GC* gc);

#endif
