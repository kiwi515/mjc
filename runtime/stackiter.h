/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Course:  CSE 4251, Section 01, Spring 2023
 * Project: MiniJava Compiler Project
 * Charset: US-ASCII
 */

#ifndef MINI_JAVA_COMPILER_STACKITER_H
#define MINI_JAVA_COMPILER_STACKITER_H
#include "types.h"

// SPARC register window
typedef struct RegisterWindow {
    // Local registers (%l0 - %l8)
    u32 lreg[8]; // at 0x0
    // Input/output registers (%i0/%o0 - %i8/%o8)
    u32 ioreg[8]; // at 0x20
} RegisterWindow;

// Stack frame iterator
typedef struct StackIterator {
    // Whether the first iteration has occurred
    BOOL first;
    // Current register window contents
    const RegisterWindow* window;
    // Stack frame locals
    const u32* locals;
    // Number of locals in the frame
    u32 local_num;
} StackIterator;

void stack_iterator_init(StackIterator* it);
BOOL stack_iterator_next(StackIterator* it);

#endif
