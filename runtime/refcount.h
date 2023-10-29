/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Course:  CSE 4251, Section 01, Spring 2023
 * Project: MiniJava Compiler Project
 * Charset: US-ASCII
 */

#ifndef MINI_JAVA_COMPILER_REFCOUNT_H
#define MINI_JAVA_COMPILER_REFCOUNT_H
#include "heap.h"
#include "types.h"
#include <stddef.h>

void refcount_increment(HeapHeader* header);
void refcount_decrement(HeapHeader* header);
void refcount_decr_children(HeapHeader* header);

#endif
