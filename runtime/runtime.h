/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Author:  Tyler Gutowski, tgutowski2020@my.fit.edu
 * Course:  CSE 4101, Fall 2023
 * Project: Heap Heap Hooray
 * Charset: US-ASCII
 */

#ifndef MINI_JAVA_COMPILER_RUNTIME_H
#define MINI_JAVA_COMPILER_RUNTIME_H
#include "types.h"

/*=======================================================*/
/*                  Allocator functions                  */
/*=======================================================*/
void* runtime_alloc_object(u32 size);
void* runtime_alloc_array(u32 size, u32 n);
void runtime_cleanup(void);

/*=======================================================*/
/*                  Debugging functions                  */
/*=======================================================*/
void runtime_debug_dumpheap(void);

/*=======================================================*/
/*              Garbage collector functions              */
/*=======================================================*/
void runtime_ref_inc(void* block);
void runtime_ref_dec(void* block);
void runtime_push_stack(void* frame, u32 size);
void runtime_pop_stack(void);
void set_garbage_collection_method(u32 gcType);

/*=======================================================*/
/*                    Print functions                    */
/*=======================================================*/
void runtime_print_integer(s32 value);
void runtime_print_boolean(s32 value);

#endif
