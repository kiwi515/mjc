/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Course:  CSE 4251, Section 01, Spring 2023
 * Project: MiniJava Compiler Project
 * Charset: US-ASCII
 */

#ifndef MINI_JAVA_CC_RUNTIME_H
#define MINI_JAVA_CC_RUNTIME_H
#include "types.h"
#include <stddef.h>

/*=======================================================*/
/*                  Allocator functions                  */
/*=======================================================*/

/**
 * @brief Allocate memory for a MiniJava object
 *
 * @param sz Memory block size
 * @return void* Memory block
 */
void* runtime_alloc_object(u32 sz);

/**
 * @brief Allocate memory for a MiniJava array
 *
 * @param sz Element size
 * @param n Number of elements
 * @return void* Memory block
 */
void* runtime_alloc_array(u32 sz, u32 n);

/*=======================================================*/
/*                    Print functions                    */
/*=======================================================*/

/**
 * @brief Print integer value to the console
 */
void runtime_print_integer(s32 value);

/**
 * @brief Print boolean value to the console
 */
void runtime_print_boolean(s32 value);

#endif
