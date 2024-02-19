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
} Config;

/**
 * @brief Initialize default configuration here
 */
static Config config = {
    // clang-format off
    .gc_type = GCType_None
    // clang-format on
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
    static const char* type2str[] = {"None", "Refcount", "MarkSweep", "Copying",
                                     "Generational"};

    MJC_ASSERT(type < GCType_Max);
    MJC_LOG("setting gctype to %s\n", type2str[type]);

    config.gc_type = type;
}
