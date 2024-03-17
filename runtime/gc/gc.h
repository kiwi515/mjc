/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Author:  Tyler Gutowski, tgutowski2020@my.fit.edu
 * Course:  CSE 4101, Fall 2023
 * Project: Heap Heap Hooray
 * Charset: US-ASCII
 */

#ifndef MINI_JAVA_COMPILER_GC_GC_H
#define MINI_JAVA_COMPILER_GC_GC_H
#include "types.h"

// Forward declarations
typedef struct Object;

/**
 * @brief Garbage collector types
 */
typedef enum {
    GcType_None,           // no gc
    GcType_RefCountGC,     // reference counting
    GcType_MarkSweepGC,    // mark-sweep
    GcType_CopyingGC,      // copying
    GcType_GenerationalGC, // generational

    GcType_Max
} GcType;

/**
 * @brief Base garbage collector structure
 */
typedef struct GC {
    // Type of this GC
    GcType type;

    /**
     * Use gc_* family of functions! Don't call these directly!!!
     */

    // Perform GC cycle
    void (*_collect)(struct GC* gc);

    // Stack frames
    void (*_stack_push)(struct GC* gc, void* frame, u32 size);
    void (*_stack_pop)(struct GC* gc);

    // Reference counting
    void (*_ref_incr)(struct GC* gc, struct Object* obj);
    void (*_ref_decr)(struct GC* gc, struct Object* obj);

    // Destroy this GC
    void (*_destroy)(struct GC* gc);
} GC;

/**
 * @brief "Dynamic" cast from Heap to subtypes by checking the 'type' field.
 * @details Returns NULL if cast is not possible.
 */
#define GC_DYNAMIC_CAST(gc, T)                                                 \
    (gc != NULL && gc->type == GcType_##T ? (T*)gc : NULL)

void gc_collect(GC* gc);

void gc_stack_push(GC* gc, void* frame, u32 size);
void gc_stack_pop(GC* gc);

void gc_ref_incr(GC* gc, struct Object* obj);
void gc_ref_decr(GC* gc, struct Object* obj);

void gc_destroy(GC* gc);

#endif
