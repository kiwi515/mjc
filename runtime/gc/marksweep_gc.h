/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Author:  Tyler Gutowski, tgutowski2020@my.fit.edu
 * Course:  CSE 4101, Fall 2023
 * Project: Heap Heap Hooray
 * Charset: US-ASCII
 */

#ifndef MINI_JAVA_COMPILER_GC_MARKSWEEPGC_H
#define MINI_JAVA_COMPILER_GC_MARKSWEEPGC_H
#include "gc/gc.h"
#include "linklist.h"
#include "types.h"

/**
 * @brief Mark-sweep GC
 */
typedef struct MarkSweepGC {
    GC base;         // Common GC structure
    LinkList frames; // Active stack frames
} MarkSweepGC;

GC* marksweep_create(void);
void marksweep_destroy(GC* gc);

void marksweep_collect(GC* gc);
void marksweep_stack_push(GC* gc, void* frame, u32 size);
void marksweep_stack_pop(GC* gc);

#endif
