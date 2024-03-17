/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Author:  Tyler Gutowski, tgutowski2020@my.fit.edu
 * Course:  CSE 4101, Fall 2023
 * Project: Heap Heap Hooray
 * Charset: US-ASCII
 */

#ifndef MINI_JAVA_COMPILER_GC_REFCOUNTGC_H
#define MINI_JAVA_COMPILER_GC_REFCOUNTGC_H
#include "gc/gc.h"
#include "types.h"

// Forward declarations
typedef struct Object;

/**
 * @brief Reference counting GC
 */
typedef struct RefCountGC {
    GC base; // Common GC structure
} RefCountGC;

GC* refcount_create(void);
void refcount_destroy(GC* gc);

void refcount_ref_incr(GC* gc, struct Object* obj);
void refcount_ref_decr(GC* gc, struct Object* obj);

#endif
