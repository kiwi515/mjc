/*
 * Author:  Tyler Gutowski, tgutowski2020@my.fit.edu
 * Course:  CSE 4101, Fall 2023
 * Project: Heap Heap Hooray
 * Charset: US-ASCII
 */
#ifndef MINI_JAVA_COMPILER_MARKSWEEP_H
#define MINI_JAVA_COMPILER_MARKSWEEP_H
#include "heap.h"
#include "types.h"

void marksweep_add_root(HeapHeader* header);
void marksweep_remove_root(HeapHeader* header);

void marksweep_mark(void);
void marksweep_sweep(void);
void marksweep_collect(void);

#endif
