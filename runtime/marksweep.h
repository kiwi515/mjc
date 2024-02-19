/*
 * Author:  Tyler Gutowski, tgutowski2020@my.fit.edu
 * Course:  CSE 4101, Fall 2023
 * Project: Heap Heap Hooray
 * Charset: US-ASCII
 */
#ifndef MINI_JAVA_COMPILER_MARKSWEEP_H
#define MINI_JAVA_COMPILER_MARKSWEEP_H
#include "types.h"

void marksweep_push_stack(void* frame, u32 size);
void marksweep_pop_stack(void);

void marksweep_mark(void);
void marksweep_sweep(void);
void marksweep_collect(void);

#endif
