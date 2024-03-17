/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Author:  Tyler Gutowski, tgutowski2020@my.fit.edu
 * Course:  CSE 4101, Fall 2023
 * Project: Heap Heap Hooray
 * Charset: US-ASCII
 */

#include "gc/marksweep_gc.h"
#include "heap/heap.h"
#include "runtime.h"
#include "stackframe.h"
#include <stdlib.h>
#include <string.h>

// Forward declarations
static void __marksweep_mark_obj(Object* obj, u32* pp_obj);
static void __marksweep_sweep(GC* gc);

/**
 * @brief Create a mark-sweep GC
 */
GC* marksweep_create(void) {
    MarkSweepGC* self = MJC_ALLOC_OBJ(MarkSweepGC);
    MJC_ASSERT(self != NULL);
    memset(self, 0, sizeof(MarkSweepGC));
    self->base.type = GcType_MarkSweepGC;

    // Register GC functions
    self->base._destroy = marksweep_destroy;
    self->base._collect = marksweep_collect;
    self->base._stack_push = marksweep_stack_push;
    self->base._stack_pop = marksweep_stack_pop;
    self->base._ref_incr = NULL;
    self->base._ref_decr = NULL;

    // Handle to basic GC
    return &self->base;
}

/**
 * @brief Destroy this GC
 *
 * @param gc Mark-sweep GC
 */
void marksweep_destroy(GC* gc) {
    MarkSweepGC* self = GC_DYNAMIC_CAST(gc, MarkSweepGC);
    MJC_ASSERT(self != NULL);

    MJC_FREE(self);
}

/**
 * @brief Perform a mark-sweep GC cycle
 *
 * @param gc Mark-sweep GC
 */
void marksweep_collect(GC* gc) {
    MarkSweepGC* self = GC_DYNAMIC_CAST(gc, MarkSweepGC);
    MJC_ASSERT(self != NULL);

    stackframe_traverse(__marksweep_mark_obj);
    __marksweep_sweep(gc);
}

/**
 * @brief Push a new active stack frame
 *
 * @param gc Mark-sweep GC
 * @param frame Stack frame
 * @param size Stack frame size (before alignment)
 */
void marksweep_stack_push(GC* gc, void* frame, u32 size) {
    MarkSweepGC* self = GC_DYNAMIC_CAST(gc, MarkSweepGC);
    MJC_ASSERT(self != NULL);
    MJC_ASSERT(frame != NULL);

    stackframe_push(frame, size);
}

/**
 * @brief Pop the current active stack frame
 *
 * @param gc Mark-sweep GC
 */
void marksweep_stack_pop(GC* gc) {
    MarkSweepGC* self = GC_DYNAMIC_CAST(gc, MarkSweepGC);
    MJC_ASSERT(self != NULL);

    stackframe_pop();
}

/**
 * @brief Mark object as reachable (stack traversal function)
 *
 * @param obj Heap object that was found
 * @param pp_obj Address of the pointer to the object
 */
static void __marksweep_mark_obj(Object* obj, u32* pp_obj) {
    MJC_ASSERT(obj != NULL);
    MJC_ASSERT(pp_obj != NULL);

    obj->marked = TRUE;
    MJC_LOG("marksweep mark %p\n", obj);
}

/**
 * @brief Sweep all unreachable objects
 *
 * @param gc Mark-sweep GC
 */
static void __marksweep_sweep(GC* gc) {
    MarkSweepGC* self = GC_DYNAMIC_CAST(gc, MarkSweepGC);
    MJC_ASSERT(self != NULL);

    // Manual list iteration due to deletion while iterating
    LinkNode* node = curr_heap->objects.head;
    Object* elem = NULL;

    while (node != NULL) {
        // Preserve next pointer before potential node deletion
        LinkNode* next = node->next;

        // Access object in current node
        elem = (Object*)node->object;

        // Free unmarked objects
        if (!elem->marked) {
            MJC_LOG("sweep %p\n", elem);
            heap_free(curr_heap, elem);
        } else {
            // Unmark for next gc cycle
            elem->marked = FALSE;
        }

        node = next;
    }
}
