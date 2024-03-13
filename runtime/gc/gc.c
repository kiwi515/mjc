/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Author:  Tyler Gutowski, tgutowski2020@my.fit.edu
 * Course:  CSE 4101, Fall 2023
 * Project: Heap Heap Hooray
 * Charset: US-ASCII
 */

#include "gc/gc.h"
#include "heap/heap.h"

/**
 * @brief Perform a GC cycle
 *
 * @param gc Garbage collector
 */
void gc_collect(GC* gc) {
    MJC_ASSERT(gc != NULL);

    // Functionality is optional
    if (gc->_collect != NULL) {
        gc->_collect(gc);
    }
}

/**
 * @brief Push a new active stack frame
 *
 * @param gc Garbage collector
 * @param frame Stack frame
 * @param size Stack frame size (before alignment)
 */
void gc_stack_push(GC* gc, const void* frame, u32 size) {
    MJC_ASSERT(gc != NULL);
    MJC_ASSERT(frame != NULL);
    MJC_ASSERT(size > 0);

    // Functionality is optional
    if (gc->_stack_push != NULL) {
        gc->_stack_push(gc, frame, size);
    }
}

/**
 * @brief Pop the current active stack frame
 *
 * @param gc Garbage collector
 */
void gc_stack_pop(GC* gc) {
    MJC_ASSERT(gc != NULL);

    // Functionality is optional
    if (gc->_stack_pop != NULL) {
        gc->_stack_pop(gc);
    }
}

/**
 * @brief Increment an object's reference count
 *
 * @param gc Garbage collector
 * @param obj Object
 */
void gc_ref_incr(GC* gc, Object* obj) {
    MJC_ASSERT(gc != NULL);
    MJC_ASSERT(obj != NULL);

    // Functionality is optional
    if (gc->_ref_incr != NULL) {
        gc->_ref_incr(gc, obj);
    }
}

/**
 * @brief Decrement an object's reference count
 *
 * @param gc Garbage collector
 * @param obj Object
 */
void gc_ref_decr(GC* gc, Object* obj) {
    MJC_ASSERT(gc != NULL);
    MJC_ASSERT(obj != NULL);

    // Functionality is optional
    if (gc->_ref_decr != NULL) {
        gc->_ref_decr(gc, obj);
    }
}

/**
 * @brief Destroy this GC
 *
 * @param gc Garbage collector
 */
void gc_destroy(GC* gc) {
    MJC_ASSERT(gc != NULL);

    // Functionality is optional
    if (gc->_destroy != NULL) {
        gc->_destroy(gc);
    }
}
