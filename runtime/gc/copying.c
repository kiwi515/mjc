/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Author:  Tyler Gutowski, tgutowski2020@my.fit.edu
 * Course:  CSE 4101, Fall 2023
 * Project: Heap Heap Hooray
 * Charset: US-ASCII
 */

#include "gc/copying.h"
#include "config.h"
#include "gc/marksweep.h"
#include "heap/chunk_heap.h"
#include "runtime.h"
#include <stdlib.h>
#include <string.h>

static void __copying_swap_heaps(GC* gc);

/**
 * @brief Create a copying GC
 */
GC* copying_create(void) {
    CopyingGC* self = MJC_ALLOC_OBJ(CopyingGC);
    MJC_ASSERT(self != NULL);
    self->base.type = GcType_CopyingGC;

    // Register GC functions
    self->base._destroy = copying_destroy;
    self->base._collect = copying_collect;
    self->base._stack_push = copying_stack_push;
    self->base._stack_pop = copying_stack_pop;
    self->base._ref_incr = NULL;
    self->base._ref_decr = NULL;

    // Need mark-sweep GC for marking functionality
    self->mark_sweep = marksweep_create();
    MJC_ASSERT(self->mark_sweep != NULL);

    // Prepare "to" heap
    u32 to_size = config_get_heap_size();
    self->to_heap = chunkheap_create(to_size);
    MJC_ASSERT(self->to_heap != NULL);

    // Handle to basic GC
    return &self->base;
}

/**
 * @brief Destroy this GC
 *
 * @param gc Copying GC
 */
void copying_destroy(GC* gc) {
    CopyingGC* self = GC_DYNAMIC_CAST(gc, CopyingGC);
    MJC_ASSERT(self != NULL);

    MJC_ASSERT(self->to_heap != NULL);
    heap_destroy(self->to_heap);

    MJC_ASSERT(self->mark_sweep != NULL);
    marksweep_destroy(self->mark_sweep);

    MJC_FREE(self);
}

/**
 * @brief Perform a copying GC cycle
 *
 * @param gc Copying GC
 */
void copying_collect(GC* gc) {
    CopyingGC* self = GC_DYNAMIC_CAST(gc, CopyingGC);
    MJC_ASSERT(self != NULL);
    MJC_ASSERT(self->to_heap != NULL);
    MJC_ASSERT(self->mark_sweep != NULL);

    // Mark live allocations
    __marksweep_mark(self->mark_sweep);

    // Clear the "to" heap (re-create it)
    heap_destroy(self->to_heap);
    u32 to_size = config_get_heap_size();
    self->to_heap = chunkheap_create(to_size);
    MJC_ASSERT(self->to_heap != NULL);

    // Copy over live allocations
    chunkheap_purify(curr_heap, self->to_heap);

    // Everything left in the "from" heap is garbage
    heap_destroy(curr_heap);
    u32 from_size = config_get_heap_size();
    curr_heap = chunkheap_create(from_size);
    MJC_ASSERT(curr_heap != NULL);

    // Swap handles of "from" and "to" heaps
    __copying_swap_heaps(gc);
}

/**
 * @brief Push a new active stack frame
 *
 * @param gc Copying GC
 * @param frame Stack frame
 * @param size Stack frame size (before alignment)
 */
void copying_stack_push(GC* gc, const void* frame, u32 size) {
    CopyingGC* self = GC_DYNAMIC_CAST(gc, CopyingGC);
    MJC_ASSERT(self != NULL);
    MJC_ASSERT(self->mark_sweep != NULL);

    // Hand-off to mark-sweep
    marksweep_stack_push(self->mark_sweep, frame, size);
}

/**
 * @brief Pop the current active stack frame
 *
 * @param gc Copying GC
 */
void copying_stack_pop(GC* gc) {
    CopyingGC* self = GC_DYNAMIC_CAST(gc, CopyingGC);
    MJC_ASSERT(self != NULL);
    MJC_ASSERT(self->mark_sweep != NULL);

    // Hand-off to mark-sweep
    marksweep_stack_pop(self->mark_sweep);
}

/**
 * @brief Swap the "to" and "from" heaps
 *
 * @param gc Copying GC
 */
static void __copying_swap_heaps(GC* gc) {
    CopyingGC* self = GC_DYNAMIC_CAST(gc, CopyingGC);
    MJC_ASSERT(self != NULL);
    MJC_ASSERT(self->to_heap != NULL);
    MJC_ASSERT(curr_heap != NULL);

    Heap* temp = curr_heap;
    curr_heap = self->to_heap;
    self->to_heap = temp;
}