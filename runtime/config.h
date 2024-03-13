/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Author:  Tyler Gutowski, tgutowski2020@my.fit.edu
 * Course:  CSE 4101, Fall 2023
 * Project: Heap Heap Hooray
 * Charset: US-ASCII
 */

#ifndef MINI_JAVA_COMPILER_CONFIG_H
#define MINI_JAVA_COMPILER_CONFIG_H
#include "gc/gc.h"
#include "heap/heap.h"
#include "types.h"

GcType config_get_gc_type(void);
void config_set_gc_type(GcType type);

HeapType config_get_heap_type(void);
void config_set_heap_type(HeapType type);

u32 config_get_heap_size(void);
void config_set_heap_size(u32 size);

#endif