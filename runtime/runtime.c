/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Author:  Tyler Gutowski, tgutowski2020@my.fit.edu
 * Course:  CSE 4101, Fall 2023
 * Project: Heap Heap Hooray
 * Charset: US-ASCII
 */

#include "runtime.h"
#include "config.h"
#include "heap.h"
#include "marksweep.h"
#include "refcount.h"

/**
 * @brief Array header
 */
typedef struct ArrayHeader {
    // Array length
    u32 length; // at 0x0
    // Array data
    u8 data[]; // at 0x4
} ArrayHeader;

/**
 * @brief Allocate memory for a MiniJava object
 *
 * @param size Object size
 * @return void* Object memory
 */
void* runtime_alloc_object(u32 size) { return heap_alloc(size); }

/**
 * @brief Allocate memory for a MiniJava array
 *
 * @param size Element size
 * @param n Number of elements
 * @return void* Array memory
 */
void* runtime_alloc_array(u32 size, u32 n) {
    ArrayHeader* array;

    // Allocate array memory
    array = heap_alloc(size * n + sizeof(ArrayHeader));

    // Set array length
    if (array != NULL) {
        array->length = n;
    }

    return array;
}

/**
 * @brief Cleanup any runtime-allocated memory before exiting
 */
void runtime_cleanup(void) {
    // TODO
}

/**
 * @brief Dump contents of the heap (for debug)
 */
void runtime_debug_dumpheap(void) { heap_dump(); }

/**
 * @brief Change the runtime GC type
 *
 * @param type New GC type
 */
void runtime_set_gctype(GCType type) { config_set_gctype(type); }

/**
 * @brief Increment a heap allocation's reference count
 *
 * @param block Memory block
 */
void runtime_ref_inc(void* block) {
    if (block == NULL) {
        return;
    }

    if (config_get_gctype() == GCType_Refcount) {
        refcount_increment(heap_get_header(block));
    }
}

/**
 * @brief Decrement a heap allocation's reference count
 *
 * @param block Memory block
 */
void runtime_ref_dec(void* block) {
    if (block == NULL) {
        return;
    }

    if (config_get_gctype() == GCType_Refcount) {
        refcount_decrement(heap_get_header(block));
    }
}

/**
 * @brief Push a new stack frame (for mark-sweep GC)
 *
 * @param frame Stack pointer
 * @param size Frame size (unaligned)
 */
void runtime_push_stack(void* frame, u32 size) {
    if (frame == NULL) {
        return;
    }

    if (config_get_gctype() == GCType_MarkSweep) {
        marksweep_push_stack(frame, size);
    }
}

/**
 * @brief Pop the current stack frame (for mark-sweep GC)
 */
void runtime_pop_stack(void) {
    if (config_get_gctype() == GCType_MarkSweep) {
        marksweep_pop_stack();
    }
}

/**
 * @brief Force a garbage collection cycle
 */
void runtime_do_gc_cycle(void) {
    const GCType t = config_get_gctype();

    switch (t) {
    case GCType_None:
    case GCType_Refcount:
        DEBUG_LOG("[runtime] Cannot force GC cycle on None/Refcount\n");
        break;
    case GCType_MarkSweep:
        marksweep_collect();
        break;
    default:
        DEBUG_LOG("[runtime] Unimplemented GC cycle: type=%d\n", t);
        break;
    }
}

/**
 * @brief Print integer value to the console
 *
 * @param value Integer value
 */
void runtime_print_integer(s32 value) { printf("%d\n", value); }

/**
 * @brief Print boolean value to the console
 *
 * @param value Boolean value
 */
void runtime_print_boolean(s32 value) {
    printf("%s\n", value ? "True" : "False");
}
