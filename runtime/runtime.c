/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Author:  Tyler Gutowski, tgutowski2020@my.fit.edu
 * Course:  CSE 4101, Fall 2023
 * Project: Heap Heap Hooray
 * Charset: US-ASCII
 */

#include "runtime.h"
#include "config.h"
#include "gc/copying_gc.h"
#include "gc/gc.h"
#include "gc/marksweep_gc.h"
#include "gc/refcount_gc.h"
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

static BOOL __runtime_valid_heap(void);
static BOOL __runtime_valid_gc(void);

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
    case GcType_None:
        break;
    case GcType_RefCountGC:
        curr_gc = refcount_create();
        break;
    case GcType_MarkSweepGC:
        curr_gc = marksweep_create();
        break;
    case GcType_CopyingGC:
        curr_gc = copying_create();
        break;
    case GcType_GenerationalGC:
        MJC_ASSERT_MSG(FALSE, "Not implemented");
        break;
    default:
        MJC_ASSERT(FALSE);
        break;
    }

    MJC_ASSERT(__runtime_valid_heap());
    MJC_ASSERT(__runtime_valid_gc());
}

/**
 * @brief Perform a GC cycle
 */
void runtime_collect(void) {
    MJC_ASSERT(__runtime_valid_gc());
    gc_collect(curr_gc);
}

/**
 * @brief Terminate the MJC runtime
 */
void runtime_exit(void) {
    MJC_ASSERT(__runtime_valid_gc());
    gc_destroy(curr_gc);

    MJC_ASSERT(__runtime_valid_heap());
    heap_destroy(curr_heap);
}

/**
 * @brief Allocate memory for a MiniJava object
 *
 * @param size Object size
 * @return void* Object memory
 */
void* runtime_alloc_object(u32 size) {
    MJC_ASSERT(__runtime_valid_heap());
    return heap_alloc(curr_heap, size);
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
 * @brief Dump contents of the heap (for debug)
 */
void runtime_dump_heap(void) {
    MJC_ASSERT(__runtime_valid_heap());
    heap_dump(curr_heap);
}

/**
 * @brief Change the runtime GC type
 *
 * @param type New GC type
 */
void runtime_set_gc_type(GcType type) {
    MJC_ASSERT(type < GcType_Max);
    config_set_gc_type(type);
}

/**
 * @brief Change the runtime heap type
 *
 * @param type New heap type
 */
void runtime_set_heap_type(HeapType type) {
    MJC_ASSERT(type < HeapType_Max);
    config_set_heap_type(type);
}

/**
 * @brief Change the runtime heap size
 * @note Doesn't apply to STL heap
 *
 * @param size New heap size
 */
void runtime_set_heap_size(u32 size) {
    MJC_ASSERT(size > 0);
    config_set_heap_size(size);
}

/**
 * @brief Increment a heap allocation's reference count
 *
 * @param block Memory block
 */
void runtime_ref_incr(void* block) {
    MJC_ASSERT(__runtime_valid_gc());

    if (block != NULL) {
        gc_ref_incr(curr_gc, heap_get_object(block));
    }
}

/**
 * @brief Decrement a heap allocation's reference count
 *
 * @param block Memory block
 */
void runtime_ref_decr(void* block) {
    MJC_ASSERT(__runtime_valid_gc());

    if (block != NULL) {
        gc_ref_decr(curr_gc, heap_get_object(block));
    }
}

/**
 * @brief Push a new stack frame
 *
 * @param frame Stack pointer
 * @param size Frame size (unaligned)
 */
void runtime_stack_push(void* frame, u32 size) {
    MJC_ASSERT(__runtime_valid_gc());
    gc_stack_push(curr_gc, frame, size);
}

/**
 * @brief Pop the current stack frame
 */
void runtime_stack_pop(void) {
    MJC_ASSERT(__runtime_valid_gc());
    gc_stack_pop(curr_gc);
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

/**
 * @brief Check whether the current heap is valid
 */
static BOOL __runtime_valid_heap(void) {
    return curr_heap != NULL;
}

/**
 * @brief Check whether the current GC is valid
 */
static BOOL __runtime_valid_gc(void) {
    return curr_gc != NULL || config_get_gc_type() == GcType_None;
}
