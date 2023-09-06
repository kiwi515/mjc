/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Course:  CSE 4251, Section 01, Spring 2023
 * Project: MiniJava Compiler Project
 * Charset: US-ASCII
 */

#include "runtime.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

/**
 * @brief MiniJava array
 */
typedef struct Array {
    // Array length
    u32 length; // at 0x0
    // Array data
    u8 data[]; // at 0x4
} Array;

/**
 * @brief Allocate memory for a MiniJava object
 *
 * @param sz Memory block size
 * @return void* Memory block
 */
void* runtime_alloc_object(size_t sz) {
    // Allocate memory block
    void* const block = malloc(sz);

    // Zero-initialize memory block
    if (block != NULL) {
        memset(block, 0, sz);
    }

    return block;
}

/**
 * @brief Allocate memory for a MiniJava array
 *
 * @param elemSize Element size
 * @param numElem Number of elements
 * @return void* Memory block
 */
void* runtime_alloc_array(size_t elemSize, size_t numElem) {
    // Allocate memory block
    void* const block = runtime_alloc_object(elemSize * numElem);

    // Set array length
    if (block != NULL) {
        Array* const arr = (Array*)block;
        arr->length = numElem;
    }

    return block;
}

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
void runtime_assert_array_subscript(const void* const object, s32 index) {
    const Array* const arr = (Array*)object;

    if (index < 0 || index >= arr->length) {
        printf("Runtime error: Array index out of bounds: %d\n", index);
        printf("Exiting program.\n");
        exit(EXIT_FAILURE);
    }
}

/**
 * @brief Print integer value to the console
 */
void runtime_print_integer(s32 value) { printf("%d\n", value); }

/**
 * @brief Print boolean value to the console
 */
void runtime_print_boolean(s32 value) {
    printf("%s\n", value ? "True" : "False");
}
