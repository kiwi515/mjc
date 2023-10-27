/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Course:  CSE 4251, Section 01, Spring 2023
 * Project: MiniJava Compiler Project
 * Charset: US-ASCII
 */

#ifndef MINI_JAVA_COMPILER_TYPES_H
#define MINI_JAVA_COMPILER_TYPES_H
#include <stdint.h>

typedef uint64_t u64;
typedef int64_t s64;

typedef uint32_t u32;
typedef int32_t s32;

typedef uint16_t u16;
typedef int16_t s16;

typedef uint8_t u8;
typedef int8_t s8;

typedef float f32;
typedef double f64;

typedef int BOOL;
#define FALSE 0
#define TRUE 1

#define MIN(x, y) ((x) < (y) ? (x) : (y))
#define MAX(x, y) ((x) > (y) ? (x) : (y))

#ifndef NDEBUG
#define DEBUG_LOG printf
#else
#define DEBUG_LOG (void)
#endif

#endif