/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Author:  Tyler Gutowski, tgutowski2020@my.fit.edu
 * Course:  CSE 4101, Fall 2023
 * Project: Heap Heap Hooray
 * Charset: US-ASCII
 */

#ifndef MINI_JAVA_COMPILER_GC_GENERATIONALGC_H
#define MINI_JAVA_COMPILER_GC_GENERATIONALGC_H
#include "gc/gc.h"
#include "types.h"

// Forward declarations
typedef struct Heap;

/**
 * @brief Generational GC
 */
typedef struct GenerationalGC {
    GC base;              // Common GC structure
    struct Heap* gen_one; // Generation one
} GenerationalGC;

GC* generational_create(void);
void generational_destroy(GC* gc);

void generational_collect(GC* gc);
void generational_stack_push(GC* gc, void* frame, u32 size);
void generational_stack_pop(GC* gc);

#endif
