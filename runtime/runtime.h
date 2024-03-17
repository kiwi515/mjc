/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Author:  Tyler Gutowski, tgutowski2020@my.fit.edu
 * Course:  CSE 4101, Fall 2023
 * Project: Heap Heap Hooray
 * Charset: US-ASCII
 */

#ifndef MINI_JAVA_COMPILER_RUNTIME_H
#define MINI_JAVA_COMPILER_RUNTIME_H
#include "gc/gc.h"
#include "heap/heap.h"
#include "types.h"

// Current heap
extern Heap* curr_heap;
// Current garbage collector
extern GC* curr_gc;

// State
void runtime_enter(void);
void runtime_collect(void);
void runtime_exit(void);

// Memory
void* runtime_alloc_object(u32 size);
void* runtime_alloc_array(u32 size, u32 n);

// Debug
void runtime_dump_heap(void);

// Config
void runtime_set_gc_type(GcType type);
void runtime_set_heap_type(HeapType type);
void runtime_set_heap_size(u32 size);

// Refcount
void runtime_ref_incr(void* block);
void runtime_ref_decr(void* block);

// Stack frame
void runtime_stack_push(void* frame, u32 size);
void runtime_stack_pop(void);

// Print
void runtime_print_integer(s32 value);
void runtime_print_boolean(s32 value);

#endif
