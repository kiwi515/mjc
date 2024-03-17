/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Author:  Tyler Gutowski, tgutowski2020@my.fit.edu
 * Course:  CSE 4101, Fall 2023
 * Project: Heap Heap Hooray
 * Charset: US-ASCII
 */

#ifndef MINI_JAVA_COMPILER_STACKFRAME_H
#define MINI_JAVA_COMPILER_STACKFRAME_H
#include "types.h"

// Forward declarations
typedef struct Object;

/**
 * @brief Function to apply to all reachable objects
 *
 * @param arg User argument (optional)
 * @param obj Heap object that was found
 * @param pp_obj Address of the pointer to the object
 */
typedef void (*StackFrameTraverseFunc)(void* arg, struct Object* obj,
                                       void** pp_obj);

void stackframe_push(void* frame, u32 size);
void stackframe_pop(void);
void stackframe_traverse(StackFrameTraverseFunc callback, void* callback_arg);

#endif
