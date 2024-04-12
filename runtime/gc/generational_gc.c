/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Author:  Tyler Gutowski, tgutowski2020@my.fit.edu
 * Course:  CSE 4101, Fall 2023
 * Project: Heap Heap Hooray
 * Charset: US-ASCII
 */

#include "gc/generational_gc.h"
#include "config.h"
#include "heap/chunk_heap.h"
#include "heap/heap.h"
#include "runtime.h"
#include "stackframe.h"
#include <string.h>

// Forward declarations
static void __generational_mark_obj(void* arg, Object* obj, void** pp_obj);
static void __generational_sweep(GC* gc);

/**
 * @brief Create a generational GC
 */
GC* generational_create(void) {
    GenerationalGC* self = MJC_ALLOC_OBJ(GenerationalGC);
    MJC_ASSERT(self != NULL);
    memset(self, 0, sizeof(GenerationalGC));
    self->base.type = GcType_GenerationalGC;

    // Register GC functions
    self->base._destroy = generational_destroy;
    self->base._collect = generational_collect;
    self->base._stack_push = generational_stack_push;
    self->base._stack_pop = generational_stack_pop;
    self->base._ref_incr = NULL;
    self->base._ref_decr = NULL;

    u32 heap_size = config_get_heap_size();
    self->gen_one = chunkheap_create(heap_size);

    // Handle to basic GC
    return &self->base;
}

/**
 * @brief Destroy this GC
 *
 * @param gc Generational GC
 */
void generational_destroy(GC* gc) {
    GenerationalGC* self = GC_DYNAMIC_CAST(gc, GenerationalGC);
    MJC_ASSERT(self != NULL);

    MJC_ASSERT(self->gen_one != NULL);
    heap_destroy(self->gen_one);

    MJC_FREE(self);
}

/**
 * @brief Perform a generational GC cycle
 *
 * @param gc Generational GC
 */
void generational_collect(GC* gc) {
    GenerationalGC* self = GC_DYNAMIC_CAST(gc, GenerationalGC);
    MJC_ASSERT(self != NULL);
    MJC_ASSERT(self->gen_one != NULL);

    // Live objects move up to next generation
    stackframe_traverse(__generational_mark_obj, NULL);
    BOOL success = chunkheap_purify(curr_heap, self->gen_one);

    // If copying worked, everything in gen zero is garbage
    if (success) {
        heap_destroy(curr_heap);
        curr_heap = chunkheap_create(config_get_heap_size());
        return;
    }

    // Make new room in generation one
    Heap* old_curr_heap = curr_heap;
    curr_heap = self->gen_one;
    stackframe_traverse(__generational_mark_obj, NULL);
    __generational_sweep(gc);
    curr_heap = old_curr_heap;
}

/**
 * @brief Push a new active stack frame
 *
 * @param gc Generational GC
 * @param frame Stack frame
 * @param size Stack frame size (before alignment)
 */
void generational_stack_push(GC* gc, void* frame, u32 size) {
    GenerationalGC* self = GC_DYNAMIC_CAST(gc, GenerationalGC);
    MJC_ASSERT(self != NULL);
    MJC_ASSERT(frame != NULL);

    stackframe_push(frame, size);
}

/**
 * @brief Pop the current active stack frame
 *
 * @param gc Generational GC
 */
void generational_stack_pop(GC* gc) {
    GenerationalGC* self = GC_DYNAMIC_CAST(gc, GenerationalGC);
    MJC_ASSERT(self != NULL);

    stackframe_pop();
}

/**
 * @brief Mark object as reachable (stack traversal function)
 *
 * @param arg User argument (optional)
 * @param obj Heap object that was found
 * @param pp_obj Address of the pointer to the object
 */
static void __generational_mark_obj(void* arg, Object* obj, void** pp_obj) {
    MJC_ASSERT(obj != NULL);
    MJC_ASSERT(pp_obj != NULL);

    obj->marked = TRUE;
    MJC_LOG("generational mark %p\n", obj);
}

/**
 * @brief Sweep all unreachable objects
 *
 * @param gc Generational GC
 */
static void __generational_sweep(GC* gc) {
    GenerationalGC* self = GC_DYNAMIC_CAST(gc, GenerationalGC);
    MJC_ASSERT(self != NULL);

    for (LinkNode* node = curr_heap->objects.head; node != NULL;) {
        // Preserve next pointer before potential node deletion
        LinkNode* next = node->next;
        // Access object in current node
        Object* elem = (Object*)node->object;

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
