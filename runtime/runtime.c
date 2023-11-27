/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Author:  Tyler Gutowski, tgutowski2020@my.fit.edu
 * Course:  CSE 4101, Fall 2023
 * Project: Heap Heap Hooray
 * Charset: US-ASCII
 */

#include "runtime.h"
#include "heap.h"
#include "marksweep.h"
#include "refcount.h"
#include "types.h"

void set_garbage_collection_method(int argc, char *argv[]) {
    GCType gc_type = GC_NONE;
    for (int i = 1; i < argc; i++) {
        if (strncmp(argv[i], "--GC=", 5) == 0) {
            const char *gc_method = argv[i] + 5; // Skip over "--GC="
            if (strcmp(gc_method, "GC_MARK_SWEEP") == 0) {
                gc_type = GC_MARK_SWEEP;
            } else if (strcmp(gc_method, "GC_REF_COUNT") == 0) {
                gc_type = GC_REF_COUNT;
            }
        }
    }
    DEBUG_LOG("[gc] using garbage collection method: %d\n", gc_type);
}

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
void runtime_cleanup(void) { marksweep_collect(); }

/**
 * @brief Dump contents of the heap (for debug)
 */
void runtime_debug_dumpheap(void) { heap_dump(); }

/**
 * @brief Increment a heap allocation's reference count
 *
 * @param block Memory block
 */
void runtime_ref_inc(void* block) {
    if (block != NULL) {
        refcount_increment(heap_get_header(block));
    }
}

/**
 * @brief Decrement a heap allocation's reference count
 *
 * @param block Memory block
 */
void runtime_ref_dec(void* block) {
    if (block != NULL) {
        refcount_decrement(heap_get_header(block));
    }
}

/**
 * @brief Push a new stack frame (for mark-sweep GC)
 *
 * @param frame Stack pointer
 * @param size Frame size
 */
void runtime_push_stack(void* frame, u32 size) {
    if (frame != NULL) {
        marksweep_push_stack(frame, size);
    }
}

/**
 * @brief Pop the current stack frame (for mark-sweep GC)
 */
void runtime_pop_stack(void) { marksweep_pop_stack(); }

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
