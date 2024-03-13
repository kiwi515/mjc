/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
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
    GcType gc_type;
    // Heap configuration
    HeapType heap_type;
    // Heap size
    u32 heap_size;
} Config;

/**
 * @brief Initialize default configuration here
 */
static Config config = {
    .gc_type = GcType_None,        // no GC
    .heap_type = HeapType_StlHeap, // malloc/free
    .heap_size = 0x10000,          // 65KB
};

/**
 * @brief Get the runtime GC type
 */
GcType config_get_gc_type(void) {
    return config.gc_type;
}

/**
 * @brief Set the runtime GC type
 */
void config_set_gc_type(GcType type) {
    static const char* gc_str[] = {"None", "RefCount", "MarkSweep", "Copying",
                                   "Generational"};

    MJC_ASSERT(type < GcType_Max);
    MJC_LOG("setting gctype to %s\n", gc_str[type]);

    config.gc_type = type;
}

/**
 * @brief Get the runtime heap type
 */
HeapType config_get_heap_type(void) {
    return config.heap_type;
}

/**
 * @brief Set the runtime heap type
 */
void config_set_heap_type(HeapType type) {
    static const char* heap_str[] = {"Invalid", "Stl", "Chunk", "Buddy"};

    MJC_ASSERT(type < HeapType_Max);
    MJC_LOG("setting heaptype to %s\n", heap_str[type]);

    config.heap_type = type;
}

/**
 * @brief Get the runtime heap size
 */
u32 config_get_heap_size(void) {
    return config.heap_size;
}