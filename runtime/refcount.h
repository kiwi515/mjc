/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Author:  Tyler Gutowski, tgutowski2020@my.fit.edu
 * Course:  CSE 4101, Fall 2023
 * Project: Heap Heap Hooray
 * Charset: US-ASCII
 */

#ifndef MINI_JAVA_COMPILER_REFCOUNT_H
#define MINI_JAVA_COMPILER_REFCOUNT_H
#include "heap.h"
#include "types.h"

void refcount_increment(HeapHeader* header);
void refcount_decrement(HeapHeader* header);
void refcount_decr_children(HeapHeader* header);

#endif
