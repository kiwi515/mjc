/*
 * Author:  Tyler Gutowski, tgutowski2020@my.fit.edu
 * Course:  CSE 4101, Fall 2023
 * Project: Heap Heap Hooray
 * Charset: US-ASCII
 */

#include "marksweep.h"
#include "heap.h"
#include <stdlib.h>

/**
 * @brief SPARC stack frame
 */
typedef struct SparcFrame {
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
} SparcFrame;

/**
 * @brief Stack frame descriptor
 */
typedef struct StackDesc {
    // Stack pointer
    SparcFrame* sp;
    // Frame size
    u32 size;
} StackDesc;

// Active stack frames
static LinkList frame_list;

static void search_block(void* block, u32 size);

/**
 * @brief Push a new stack frame
 *
 * @param frame Stack pointer
 * @param size Frame size (unaligned)
 */
void marksweep_push_stack(void* frame, u32 size) {
    StackDesc* f;

    DEBUG_LOG("[marksweep] push_stack %p (size:%d)\n", frame, size);

    // Allocate and fill out stack frame structure
    f = malloc(sizeof(StackDesc));
    assert(f != NULL);
    f->sp = (SparcFrame*)frame;
    f->size = size;

    linklist_append(&frame_list, f);
}

/**
 * @brief Pop the current stack frame
 */
void marksweep_pop_stack(void) {
    LinkNode* popped;
    StackDesc* frame;

    DEBUG_LOG("[marksweep] pop_stack\n");

    // List should never be empty when popping
    popped = linklist_pop(&frame_list);
    assert(popped != NULL);

    // Release memory
    frame = (StackDesc*)popped->object;
    free(frame);
}

/**
 * @brief Search (and maybe recurse) through a word (4byte) in memory, looking
 * for roots.
 *
 * @param word Word of data (any 32-bit value)
 */
static void search_word(u32 word) {
    HeapHeader* maybe_header;

    // Don't bother with null values.
    if (word == 0) {
        return;
    }

    // Maybe this possible pointer is specifically a heap header?
    maybe_header = heap_get_header((void*)word);
    if (maybe_header == NULL) {
        return;
    }

    if (heap_is_header(maybe_header) && !maybe_header->marked) {
        // If we really found a heap header pointer, mark it.
        DEBUG_LOG("[marksweep] mark %p\n", maybe_header);
        maybe_header->marked = TRUE;

        // Also, recurse to continue through the object graph
        DEBUG_LOG("[marksweep] search alloced block %p\n", maybe_header->data);
        search_block(maybe_header->data, maybe_header->size);
    }
}

/**
 * @brief Search (and maybe recurse) through a memory block, looking for roots.
 * Used in the marksweep algorithmz
 * @param block Memory block pointer
 * @param size Memory block size
 */
static void search_block(void* block, u32 size) {
    int i;

    assert(block != NULL);
    assert(size % sizeof(u32) == 0);

    // Search the block for references
    for (i = 0; i < size / sizeof(u32); i++) {
        // Intepret the current word of the block as a possible pointer.
        search_word(((u32*)block)[i]);
    }
}

/**
 * @brief Mark all reachable objects
 */
void marksweep_mark(void) {
    StackDesc* desc;
    LinkNode* iter;
    u32 local_first;
    u32 local_idx;
    u32 local_num;
    int i;

    // Search stack frames for roots
    for (iter = frame_list.tail; iter != NULL; iter = iter->prev) {
        assert(iter->object != NULL);

        // List node contains stack frame descriptor
        desc = (StackDesc*)iter->object;
        assert(desc->sp != NULL && desc->size >= sizeof(SparcFrame));
        DEBUG_LOG("[marksweep] search stack frame: %p (size:%d)\n", desc->sp,
                  desc->size);

        // Search CPU local registers (compiler temporaries)
        for (i = 0; i < ARRAY_LENGTH(desc->sp->lreg); i++) {
            DEBUG_LOG("  sp->lreg[%d]=%08X\n", i, desc->sp->lreg[i]);
            search_word(desc->sp->lreg[i]);
        }

        // Search CPU input/output registers
        // Ignore %i6/%i7 (SP/FP, RA)
        for (i = 0; i < 6; i++) {
            DEBUG_LOG("  sp->ioreg[%d]=%08X\n", i, desc->sp->lreg[i]);
            search_word(desc->sp->ioreg[i]);
        }

        // # of locals = frame space occupied by locals / size of a local.
        // (Every data type in MiniJava takes up one word (u32).)
        local_num = (desc->size - sizeof(SparcFrame)) / sizeof(u32);
        DEBUG_LOG("  local_num=%d\n", local_num);

        /**
         * Stack frame is aligned to 8-bytes.
         *
         * Because locals are addressed from %fp (end of the stack frame),
         * rather than from %sp (beginning of the stack frame), the local
         * variables do not always start at sp->locals[0].
         *
         * This is because the stack must be padded for alignment.
         *
         * To get around this, we calculate the size of the locals while
         * accounting for alignment (round UP desc->size to nearest 8).
         *
         * Divide this by the size of a local variable (u32), and we get the
         * offset into sp->locals which we must begin from.
         */
        local_first = ROUND_UP(desc->size, 8) - sizeof(SparcFrame);
        local_first /= sizeof(u32);

        // Convert to offset (zero-indexed)
        if (local_first > 0) {
            local_first--;
        }

        // Search stack locals
        for (i = 0; i < local_num; i++) {
            // Use offset of first local to get the real index
            local_idx = local_first + i;

            DEBUG_LOG("  sp->locals[%d (align:%d)] = %08X\n", i, local_idx,
                      desc->sp->locals[local_idx]);

            search_word(desc->sp->locals[local_idx]);
        }
    }
}

/**
 * @brief Sweep all unreachable objects
 */
void marksweep_sweep() {
    LinkNode* iter = heap_list.head;
    HeapHeader* current = NULL;

    while (iter != NULL) {
        LinkNode* next = iter->next;
        current = (HeapHeader*)iter->object;

        // free unmarked objects, but don't touch any RC
        if (!current->marked) {
            DEBUG_LOG("[marksweep] sweep %p\n", current);
            heap_free(current->data, FALSE);
        } else {
            // unmark for next gc cycle
            current->marked = FALSE;
        }

        iter = next;
    }
}

/**
 * @brief Perform mark-and-sweep garbage collection
 */
void marksweep_collect(void) {
    marksweep_mark();
    marksweep_sweep();
}
