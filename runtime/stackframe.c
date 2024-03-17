/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Author:  Tyler Gutowski, tgutowski2020@my.fit.edu
 * Course:  CSE 4101, Fall 2023
 * Project: Heap Heap Hooray
 * Charset: US-ASCII
 */

#include "stackframe.h"
#include "heap/heap.h"
#include "runtime.h"
#include <string.h>

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
typedef struct FrameDesc {
    // Register context
    SparcContext* ctx;
    // Context size
    u32 size;
} FrameDesc;

// List of active stack frames
static LinkList frames;

// Forward declarations
static void __stackframe_traverse_word(u32* p_word,
                                       StackFrameTraverseFunc func);
static void __stackframe_traverse_block(void* block, u32 size,
                                        StackFrameTraverseFunc func);

/**
 * @brief Push a new active stack frame
 *
 * @param frame Stack frame
 * @param size Stack frame size (before alignment)
 */
void stackframe_push(void* frame, u32 size) {
    MJC_ASSERT(frame != NULL);
    MJC_ASSERT(size >= sizeof(SparcContext));

    MJC_LOG("stackframe_push %p (size:%d)\n", frame, size);

    // Allocate and fill out stack frame structure
    FrameDesc* f = MJC_ALLOC_OBJ(FrameDesc);
    MJC_ASSERT(f != NULL);
    memset(f, 0, sizeof(FrameDesc));

    f->ctx = (SparcContext*)frame;
    f->size = size;

    linklist_append(&frames, f);
}

/**
 * @brief Pop the current active stack frame
 */
void stackframe_pop(void) {
    MJC_LOG("stackframe_pop\n");

    // List should never be empty when popping
    LinkNode* popped = linklist_pop(&frames);
    MJC_ASSERT(popped != NULL);
    MJC_FREE(popped);

    // Release memory
    MJC_ASSERT(popped->object != NULL);
    MJC_FREE(popped->object);
}

/**
 * @brief Traverse the stack and apply a function to all reachable objects
 *
 * @param func Traversal function
 */
void stackframe_traverse(StackFrameTraverseFunc func) {
    MJC_ASSERT(func != NULL);

    // clang-format off
    LINKLIST_FOREACH_REV(&frames, FrameDesc*,
        // List node contains stack frame descriptor
        MJC_ASSERT(ELEM->ctx != NULL && ELEM->size >= sizeof(SparcContext));
        MJC_LOG("search stack frame: %p (size:%d)\n", ELEM->ctx, ELEM->size);

        // Search CPU local registers (compiler temporaries)
        for (int i = 0; i < ARRAY_LENGTH(ELEM->ctx->lreg); i++) {
            MJC_LOG("  ctx->lreg[%d]=%08X\n", i, ELEM->ctx->lreg[i]);
            __stackframe_traverse_word(&ELEM->ctx->lreg[i], func);
        }

        // Search CPU input/output registers
        // Ignore %i6/%i7 (SP/FP, RA)
        for (int i = 0; i < 6; i++) {
            MJC_LOG("  ctx->ioreg[%d]=%08X\n", i, ELEM->ctx->lreg[i]);
            __stackframe_traverse_word(&ELEM->ctx->ioreg[i], func);
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

            __stackframe_traverse_word(&ELEM->ctx->locals[local_idx], func);
        }
    );
    // clang-format on
}

/**
 * @brief Check if a word of data is an object reference
 *
 * @param p_word Pointer to word of data (any 32-bit value)
 */
static void __stackframe_traverse_word(u32* p_word,
                                       StackFrameTraverseFunc func) {
    MJC_ASSERT(p_word != NULL);
    MJC_ASSERT(func != NULL);

    // The actual word in question
    u32 word = *p_word;

    // Don't bother with null values.
    if (word == 0) {
        return;
    }

    // Backtrack from object contents to object header
    Object* maybe_obj = heap_get_object((void*)word);

    // If this "object" exists in the heap, we found a real reference
    if (heap_is_object(curr_heap, maybe_obj)) {
        // Apply user function
        MJC_LOG("traverse p_obj=%p pp_obj=%p\n", maybe_obj, p_word);
        func(maybe_obj, p_word);

        // Look for child references in this object
        MJC_LOG("traverse alloced block %p\n", maybe_obj->data);
        __stackframe_traverse_block(maybe_obj->data, maybe_obj->size, func);
    }
}

/**
 * @brief Search a block of memory for roots
 *
 * @param block Memory block pointer
 * @param size Memory block size
 */
static void __stackframe_traverse_block(void* block, u32 size,
                                        StackFrameTraverseFunc func) {
    MJC_ASSERT(block != NULL);
    MJC_ASSERT_MSG(size % sizeof(u32) == 0, "Block unaligned");
    MJC_ASSERT(func != NULL);

    // Check each word of the memory block
    for (int i = 0; i < size / sizeof(u32); i++) {
        __stackframe_traverse_word((u32*)block + i, func);
    }
}
