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
typedef struct StackFrame {
    // Stack pointer
    u32* sp;
    // Frame size
    u32 size;
} StackFrame;

// Active stack frames
static LinkList frame_list;

/**
 * @brief Push a new stack frame
 *
 * @param frame Stack pointer
 * @param size Frame size
 */
void marksweep_push_stack(void* frame, u32 size) {
    StackFrame* f;

    // Allocate and fill out stack frame structure
    f = malloc(sizeof(StackFrame));
    assert(f != NULL);
    f->sp = (u32*)frame;
    f->size = size;

    linklist_append(&frame_list, f);

    DEBUG_LOG("[marksweep] push_stack %p (size:%d)\n", frame, size);
}

/**
 * @brief Pop the current stack frame
 */
void marksweep_pop_stack(void) {
    LinkNode* popped;

    popped = linklist_pop(&frame_list);
    // List should never be empty when popping
    assert(popped != NULL);

    DEBUG_LOG("[marksweep] pop_stack\n");
}

/**
 * @brief Search (and maybe recurse) through a memory block, looking for roots.
 *
 * @param block Memory block pointer
 * @param size Memory block size
 */
static void search_block(void* block, u32 size) {
    HeapHeader* maybe_header;
    void* ptr;
    int i;

    assert(block != NULL);

    // Search the block for references
    for (i = 0; i < size / sizeof(u32); i++) {
        // Intepret the current word of the frame as a possible pointer.
        ptr = *((void**)block + i);

        // Don't bother with null values.
        if (ptr == NULL) {
            continue;
        }

        // Maybe this possible pointer is specifically a heap header?
        maybe_header = heap_get_header(ptr);

        if (heap_is_header(maybe_header) && !maybe_header->marked) {
            // If we really found a heap header pointer, mark it.
            DEBUG_LOG("[marksweep] mark %p\n", maybe_header);
            maybe_header->marked = TRUE;

            // Also, recurse to continue through the object graph
            DEBUG_LOG("[marksweep] search_block %p (alloced)\n",
                      maybe_header->data);
            search_block(maybe_header->data, maybe_header->size);
        }
    }
}

/**
 * @brief Mark all reachable objects
 */
void marksweep_mark(void) {
    LinkNode* iter;
    StackFrame* frame;

    for (iter = frame_list.tail; iter != NULL; iter = iter->prev) {
        assert(iter->object != NULL);

        // Search the stack frame for roots
        frame = (StackFrame*)iter->object;
        DEBUG_LOG("[marksweep] search_block %p (stack frame)\n", frame->sp);
        search_block(frame->sp, frame->size);
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
