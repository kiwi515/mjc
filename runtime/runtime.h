/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Course:  CSE 4251, Section 01, Spring 2023
 * Project: MiniJava Compiler Project
 * Charset: US-ASCII
 */

#ifndef MINI_JAVA_COMPILER_RUNTIME_H
#define MINI_JAVA_COMPILER_RUNTIME_H
#include "types.h"
#include <stddef.h>

/*=======================================================*/
/*                  Allocator functions                  */
/*=======================================================*/
void* runtime_alloc_object(u32 size);
void* runtime_alloc_array(u32 size, u32 n);

/*=======================================================*/
/*                  Debugging functions                  */
/*=======================================================*/
void runtime_debug_dumpheap(void);

/*=======================================================*/
/*              Garbage collector functions              */
/*=======================================================*/
void runtime_ref_inc(void* block);
void runtime_ref_dec(void* block);

/*=======================================================*/
/*                    Print functions                    */
/*=======================================================*/
void runtime_print_integer(s32 value);
void runtime_print_boolean(s32 value);

#endif
