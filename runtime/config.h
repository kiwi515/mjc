/*
 * Author:  Tyler Gutowski, tgutowski2020@my.fit.edu
 * Course:  CSE 4101, Fall 2023
 * Project: Heap Heap Hooray
 * Charset: US-ASCII
 */

#ifndef MINI_JAVA_COMPILER_CONFIG_H
#define MINI_JAVA_COMPILER_CONFIG_H
#include "types.h"

/**
 * @brief Garbage collection method
 */
typedef enum {
    GCType_None,
    GCType_Refcount,
    GCType_MarkSweep,
    GCType_Copying,
    GCType_Generational,

    GCType_Max
} GCType;

GCType config_get_gctype(void);
void config_set_gctype(GCType type);

#endif