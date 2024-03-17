/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Author:  Tyler Gutowski, tgutowski2020@my.fit.edu
 * Course:  CSE 4101, Fall 2023
 * Project: Heap Heap Hooray
 * Charset: US-ASCII
 */

#ifndef MINI_JAVA_COMPILER_TYPES_H
#define MINI_JAVA_COMPILER_TYPES_H
#include "debug.h"
#include <stdarg.h>
#include <stddef.h>
#include <stdint.h>
#include <stdio.h>

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

#define ROUND_UP(x, align) (((x) + (align)-1) & (-(align)))
#define ARRAY_LENGTH(x) (sizeof((x)) / sizeof((x)[0]))

#endif
