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

/**
 * @brief Allocate memory for a MiniJava object
 *
 * @param sz Memory block size
 * @return void* Memory block
 */
void* runtime_alloc_object(size_t sz);

/**
 * @brief Allocate memory for a MiniJava array
 *
 * @param elemSize Element size
 * @param numElem Number of elements
 * @return void* Memory block
 */
void* runtime_alloc_array(size_t elemSize, size_t numElem);

/**
 * @brief Assert that an array subscript is valid
 * (in the range 0 <= index < array.length)
 *
 * @note Function DOES NOT RETURN if index is invalid, execution is instead
 * halted
 *
 * @param object Array object
 * @param index Subscript index
 */
void runtime_assert_array_subscript(const void* const object, s32 index);

/**
 * @brief Print integer value to the console
 */
void runtime_print_integer(s32 value);

/**
 * @brief Print boolean value to the console
 */
void runtime_print_boolean(s32 value);

#endif
