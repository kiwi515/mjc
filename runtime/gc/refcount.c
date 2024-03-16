/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Author:  Tyler Gutowski, tgutowski2020@my.fit.edu
 * Course:  CSE 4101, Fall 2023
 * Project: Heap Heap Hooray
 * Charset: US-ASCII
 */

#include "gc/refcount.h"
#include "heap/heap.h"
#include "runtime.h"

// Forward declarations
static void __refcount_ref_decr_recurse(GC* gc, Object* obj);

/**
 * @brief Create a reference counting GC
 */
GC* refcount_create(void) {
    RefCountGC* self = MJC_ALLOC_OBJ(RefCountGC);
    MJC_ASSERT(self != NULL);
    self->base.type = GcType_RefCountGC;

    // Register GC functions
    self->base._destroy = NULL;
    self->base._collect = NULL;
    self->base._stack_push = NULL;
    self->base._stack_pop = NULL;
    self->base._ref_incr = refcount_ref_incr;
    self->base._ref_decr = refcount_ref_decr;

    // Handle to basic GC
    return &self->base;
}

/**
 * @brief Destroy this GC
 *
 * @param gc Reference counting GC
 */
void refcount_destroy(GC* gc) {
    RefCountGC* self = GC_DYNAMIC_CAST(gc, RefCountGC);
    MJC_ASSERT(self != NULL);

    MJC_FREE(self);
}

/**
 * @brief Increment an object's reference count
 *
 * @param gc Reference counting GC
 * @param obj Object
 */
void refcount_ref_incr(GC* gc, Object* obj) {
    RefCountGC* self = GC_DYNAMIC_CAST(gc, RefCountGC);
    MJC_ASSERT(self != NULL);

    MJC_ASSERT(obj != NULL);
    obj->ref++;

    MJC_LOG("increment %p, now %d\n", obj, obj->ref);
}

/**
 * @brief Decrement an object's reference count
 *
 * @param gc Reference counting GC
 * @param obj Object
 */
void refcount_ref_decr(GC* gc, Object* obj) {
    RefCountGC* self = GC_DYNAMIC_CAST(gc, RefCountGC);
    MJC_ASSERT(self != NULL);

    MJC_ASSERT(obj != NULL);
    MJC_ASSERT_MSG(obj->ref > 0, "WHY?????");
    obj->ref--;

    MJC_LOG("decrement %p, now %d\n", obj, obj->ref);

    if (obj->ref == 0) {
        MJC_LOG("free %p\n", obj);

        // Decrement refcount of children
        __refcount_ref_decr_recurse(gc, obj);
        // Safely free if unreferenced
        heap_free(curr_heap, obj);
    }
}

/**
 * @brief Recursively decrement the reference count of an object's children.
 *
 * @param gc Reference counting GC
 * @param obj Parent/root object
 */
static void __refcount_ref_decr_recurse(GC* gc, Object* obj) {
    RefCountGC* self = GC_DYNAMIC_CAST(gc, RefCountGC);
    MJC_ASSERT(self != NULL);

    MJC_ASSERT(obj != NULL);

    // Search block data for pointers
    for (int i = 0; i < obj->size; i += sizeof(void*)) {
        // Intepret the current word of the block as a possible pointer.
        // (Specifically, 'ptr' is the ADDRESS of this current word.)
        void** ptr = (void**)(obj->data + i);

        // Don't bother with null values
        if (*ptr == NULL) {
            continue;
        }

        // Maybe this value is an object pointer?
        Object* maybe_obj = heap_get_object(*ptr);

        // If we really found a object pointer, decrement its refcount.
        if (heap_is_object(curr_heap, maybe_obj)) {
            MJC_LOG("decr child %p of %p\n", maybe_obj, obj);
            refcount_ref_decr(gc, maybe_obj);
        }
    }
}
