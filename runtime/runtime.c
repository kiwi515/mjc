/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Author:  Tyler Gutowski, tgutowski2020@my.fit.edu
 * Course:  CSE 4101, Fall 2023
 * Project: Heap Heap Hooray
 * Charset: US-ASCII
 */

#include "runtime.h"
#include "config.h"
#include "gc/copying.h"
#include "gc/marksweep.h"
#include "gc/refcount.h"
#include "heap/chunk_heap.h"
#include "heap/stl_heap.h"

/**
 * @brief Array object header
 */
typedef struct ArrayHeader {
    // Array length
    u32 length; // at 0x0
    // Array data
    u8 data[]; // at 0x4
} ArrayHeader;

// Current heap
Heap* curr_heap = NULL;

// Current garbage collector
GC* curr_gc = NULL;

/**
 * @brief Initialize the MJC runtime
 */
void runtime_enter(void) {
    u32 heap_size = config_get_heap_size();

    // Setup heap
    switch (config_get_heap_type()) {
    case HeapType_StlHeap:
        curr_heap = stlheap_create();
        break;
    case HeapType_ChunkHeap:
        curr_heap = chunkheap_create(heap_size);
        break;
    case HeapType_BuddyHeap:
        MJC_ASSERT_MSG(FALSE, "Not implemented");
        break;
    default:
        MJC_ASSERT(FALSE);
        break;
    }

    // Setup garbage collector
    switch (config_get_gc_type()) {
    case GcType_RefCountGC:
        curr_gc = refcount_create();
        break;
    case GcType_MarkSweepGC:
        curr_gc = marksweep_create();
        break;
    case GcType_CopyingGC:
        curr_gc = copying_create();
        break;
    default:
        MJC_ASSERT(FALSE);
        break;
    }

    MJC_ASSERT(curr_heap != NULL);
    MJC_ASSERT(curr_gc != NULL);
}

void runtime_collect(void);

void runtime_exit(void) {
    ;
}

/**
 * @brief Allocate memory for a MiniJava object
 *
 * @param size Object size
 * @return void* Object memory
 */
void* runtime_alloc_object(u32 size) {
    // Copying GC needs to use slabs
    if (config_get_gc_type() == GcType_Copying) {
        return copying_alloc(size);
    }

    return heap_alloc(size);
}

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
    array = runtime_alloc_object(size * n + sizeof(ArrayHeader));

    // Set array length
    if (array != NULL) {
        array->length = n;
    }

    return array;
}

/**
 * @brief Cleanup any runtime-allocated memory before exiting
 */
void runtime_exit(void) {
    // TODO
}

/**
 * @brief Dump contents of the heap (for debug)
 */
void runtime_dump_heap(void) {
    heap_dump();
}

/**
 * @brief Change the runtime GC type
 *
 * @param type New GC type
 */
void runtime_set_gc_type(GcType type) {
    config_set_gc_type(type);
}

/**
 * @brief Increment a heap allocation's reference count
 *
 * @param block Memory block
 */
void runtime_ref_inc(void* block) {
    if (config_get_gc_type() == GcType_RefCount) {

        if (block == NULL) {
            return;
        }

        refcount_increment(heap_get_header(block));
    }
}

/**
 * @brief Decrement a heap allocation's reference count
 *
 * @param block Memory block
 */
void runtime_ref_dec(void* block) {
    if (config_get_gc_type() == GcType_RefCount) {

        if (block == NULL) {
            return;
        }

        refcount_decrement(heap_get_header(block));
    }
}

/**
 * @brief Push a new stack frame
 *
 * @param frame Stack pointer
 * @param size Frame size (unaligned)
 */
void runtime_push_stack(void* frame, u32 size) {
    if (config_get_gc_type() == GcType_MarkSweep ||
        config_get_gc_type() == GcType_Copying) {

        if (frame == NULL) {
            return;
        }

        marksweep_push_stack(frame, size);
    }
}

/**
 * @brief Pop the current stack frame
 */
void runtime_pop_stack(void) {
    if (config_get_gc_type() == GcType_MarkSweep ||
        config_get_gc_type() == GcType_Copying) {
        marksweep_pop_stack();
    }
}

/**
 * @brief Force a garbage collection cycle
 */
void runtime_collect(void) {
    GcType t = config_get_gc_type();

    switch (t) {
    case GcType_None:
    case GcType_RefCount:
        MJC_LOG("Cannot force GC cycle on None/RefCount\n");
        break;
    case GcType_MarkSweep:
        marksweep_collect();
        break;
    case GcType_Copying:
        copying_collect();
        break;
    default:
        MJC_LOG("Unimplemented GC cycle: type=%d\n", t);
        break;
    }
}

/**
 * @brief Print integer value to the console
 *
 * @param value Integer value
 */
void runtime_print_integer(s32 value) {
    printf("%d\n", value);
}

/**
 * @brief Print boolean value to the console
 *
 * @param value Boolean value
 */
void runtime_print_boolean(s32 value) {
    printf("%s\n", value ? "True" : "False");
}
