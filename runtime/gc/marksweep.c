/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Author:  Tyler Gutowski, tgutowski2020@my.fit.edu
 * Course:  CSE 4101, Fall 2023
 * Project: Heap Heap Hooray
 * Charset: US-ASCII
 */

#include "gc/marksweep.h"
#include "heap/heap.h"
#include "runtime.h"
#include <stdlib.h>

/**
 * @brief SPARC register context
 */
typedef struct SparcContext {
    // Local registers (%l0 - %l8)
    u32 lreg[8]; // at 0x0
    // Input/output registers (%i0/%o0 - %i8/%o8)
    u32 ioreg[8]; // at 0x20
    // Space for aggregate return value
    u32 aggregate; // at 0x40
    // Callee register arguments
    u32 args[6]; // at 0x44
    // Local variables
    u32 locals[]; // at 0x5C
} SparcContext;

/**
 * @brief Stack frame descriptor
 */
typedef struct StackFrame {
    // Register context
    const SparcContext* ctx;
    // Context size
    u32 size;
} StackFrame;

// Forward declarations
static void __marksweep_sweep(GC* gc);
static void __marksweep_search_word(GC* gc, u32 word);
static void __marksweep_search_block(GC* gc, const void* block, u32 size);

/**
 * @brief Create a mark-sweep GC
 */
GC* marksweep_create(void) {
    MarkSweepGC* self = MJC_ALLOC_OBJ(MarkSweepGC);
    MJC_ASSERT(self != NULL);
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

    linklist_destroy(&self->frames);
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

    __marksweep_mark(gc);
    __marksweep_sweep(gc);
}

/**
 * @brief Push a new active stack frame
 *
 * @param gc Mark-sweep GC
 * @param frame Stack frame
 * @param size Stack frame size (before alignment)
 */
void marksweep_stack_push(GC* gc, const void* frame, u32 size) {
    MarkSweepGC* self = GC_DYNAMIC_CAST(gc, MarkSweepGC);
    MJC_ASSERT(self != NULL);
    MJC_ASSERT(frame != NULL);
    MJC_ASSERT(size >= sizeof(SparcContext));

    MJC_LOG("push_stack %p (size:%d)\n", frame, size);

    // Allocate and fill out stack frame structure
    StackFrame* f = MJC_ALLOC_OBJ(StackFrame);
    MJC_ASSERT(f != NULL);
    f->ctx = (const SparcContext*)frame;
    f->size = size;

    linklist_append(&self->frames, f);
}

/**
 * @brief Pop the current active stack frame
 *
 * @param gc Mark-sweep GC
 */
void marksweep_stack_pop(GC* gc) {
    MarkSweepGC* self = GC_DYNAMIC_CAST(gc, MarkSweepGC);
    MJC_ASSERT(self != NULL);

    MJC_LOG("pop_stack\n");

    // List should never be empty when popping
    LinkNode* popped = linklist_pop(&self->frames);
    MJC_ASSERT(popped != NULL);

    // Release memory
    StackFrame* frame = (StackFrame*)popped->object;
    MJC_ASSERT(frame != NULL);
    MJC_FREE(frame);
}

/**
 * @brief Mark all reachable objects
 *
 * @param gc Mark-sweep GC
 */
void __marksweep_mark(GC* gc) {
    MarkSweepGC* self = GC_DYNAMIC_CAST(gc, MarkSweepGC);
    MJC_ASSERT(self != NULL);

    // clang-format off
    LINKLIST_FOREACH_REV(&self->frames, StackFrame*,
        // List node contains stack frame descriptor
        MJC_ASSERT(ELEM->ctx != NULL && ELEM->size >= sizeof(SparcContext));
        MJC_LOG("search stack frame: %p (size:%d)\n", ELEM->ctx, ELEM->size);

        // Search CPU local registers (compiler temporaries)
        for (int i = 0; i < ARRAY_LENGTH(ELEM->ctx->lreg); i++) {
            MJC_LOG("  ctx->lreg[%d]=%08X\n", i, ELEM->ctx->lreg[i]);
            __marksweep_search_word(gc, ELEM->ctx->lreg[i]);
        }

        // Search CPU input/output registers
        // Ignore %i6/%i7 (SP/FP, RA)
        for (int i = 0; i < 6; i++) {
            MJC_LOG("  ctx->ioreg[%d]=%08X\n", i, ELEM->ctx->lreg[i]);
            __marksweep_search_word(gc, ELEM->ctx->ioreg[i]);
        }

        // # of locals = frame space occupied by locals / size of a local.
        // (Every data type in MiniJava takes up one word (u32).)
        u32 local_num = (ELEM->size - sizeof(SparcContext)) / sizeof(u32);
        MJC_LOG("  local_num=%d\n", local_num);

        /**
         * Stack frame is aligned to 8-bytes.
         *
         * Because locals are addressed from %fp (end of the stack frame),
         * rather than from %sp (beginning of the stack frame), the local
         * variables do not always start at ctx->locals[0].
         *
         * This is because the stack must be padded for alignment.
         *
         * To get around this, we calculate the size of the locals while
         * accounting for alignment (round UP ELEM->size to nearest 8).
         *
         * Divide this by the size of a local variable (u32), and we get the
         * offset into ctx->locals which we must begin from.
         */
        u32 local_first = ROUND_UP(ELEM->size, 8) - sizeof(SparcContext);
        local_first /= sizeof(u32);

        // Convert to offset (zero-indexed)
        if (local_first > 0) {
            local_first--;
        }

        // Search stack locals
        for (int i = 0; i < local_num; i++) {
            // Use offset of first local to get the real index
            u32 local_idx = local_first + i;

            MJC_LOG("  ctx->locals[%d (align:%d)] = %08X\n", i, local_idx,
                    ELEM->ctx->locals[local_idx]);

            __marksweep_search_word(gc, ELEM->ctx->locals[local_idx]);
        }
    );
    // clang-format on
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

/**
 * @brief Search a word (immediate value) for roots
 *
 * @param gc Mark-sweep GC
 * @param word Word of data (any 32-bit value)
 */
static void __marksweep_search_word(GC* gc, u32 word) {
    MarkSweepGC* self = GC_DYNAMIC_CAST(gc, MarkSweepGC);
    MJC_ASSERT(self != NULL);

    // Don't bother with null values.
    if (word == 0) {
        return;
    }

    // Maybe this possible pointer is specifically a heap header?
    Object* maybe_obj = heap_get_object((void*)word);
    if (maybe_obj == NULL) {
        return;
    }

    if (heap_is_object(curr_heap, maybe_obj) && !maybe_obj->marked) {
        // If we really found a heap header pointer, mark it.
        MJC_LOG("mark %p\n", maybe_obj);
        maybe_obj->marked = TRUE;

        // Also, recurse to continue through the object graph
        MJC_LOG("search alloced block %p\n", maybe_obj->data);
        __marksweep_search_block(gc, maybe_obj->data, maybe_obj->size);
    }
}

/**
 * @brief Search a block of memory for roots
 *
 * @param gc Mark-sweep GC
 * @param block Memory block pointer
 * @param size Memory block size
 */
static void __marksweep_search_block(GC* gc, const void* block, u32 size) {
    MarkSweepGC* self = GC_DYNAMIC_CAST(gc, MarkSweepGC);
    MJC_ASSERT(self != NULL);

    MJC_ASSERT(block != NULL);
    MJC_ASSERT_MSG(size % sizeof(u32) == 0, "Block unaligned");

    // Search the block for references
    for (int i = 0; i < size / sizeof(u32); i++) {
        // Intepret the current word of the block as a possible pointer.
        __marksweep_search_word(gc, ((u32*)block)[i]);
    }
}
