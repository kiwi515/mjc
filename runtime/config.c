/*
 * Author:  Tyler Gutowski, tgutowski2020@my.fit.edu
 * Course:  CSE 4101, Fall 2023
 * Project: Heap Heap Hooray
 * Charset: US-ASCII
 */

#include "config.h"

/**
 * @brief Runtime configuration
 */
typedef struct Config {
    // Garbage collector configuration
    GCType gc_type;
    // Heap configuration
    HeapType heap_type;
} Config;

/**
 * @brief Initialize default configuration here
 */
static Config config = {
    .gc_type = GCType_None,
    .heap_type = HeapType_StlHeap,
};

/**
 * @brief Get the runtime GC type
 */
GCType config_get_gctype(void) {
    return config.gc_type;
}

/**
 * @brief Set the runtime GC type
 */
void config_set_gctype(GCType type) {
    static const char* gc_str[] = {"None", "Refcount", "MarkSweep", "Copying",
                                   "Generational"};

    MJC_ASSERT(type < GCType_Max);
    MJC_LOG("setting gctype to %s\n", gc_str[type]);

    config.gc_type = type;
}

/**
 * @brief Get the runtime heap type
 */
HeapType config_get_heaptype(void) {
    return config.heap_type;
}

/**
 * @brief Set the runtime heap type
 */
void config_set_heaptype(HeapType type) {
    static const char* heap_str[] = {"Invalid", "Stl", "Chunk", "Buddy"};

    MJC_ASSERT(type < HeapType_Max);
    MJC_LOG("setting heaptype to %s\n", heap_str[type]);

    config.heap_type = type;
}
