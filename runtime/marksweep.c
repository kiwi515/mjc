/*
 * Author:  Tyler Gutowski
 */

#include "marksweep.h"
#include "heap.h"
#include <stddef.h>
#include <assert.h>
#include <stdbool.h>

static void mark(HeapHeader* header);
static void sweep();

void gc_mark() {
    void* current_block = heap_list_head; 

    while (current_block != NULL) {
        // valid pointer
        if (heap_is_header(current_block)) {
            HeapHeader* header = heap_get_header(current_block);
            // if its being used
            if (header->ref > 0) {
                mark(header);
            }
            current_block = (void*)(header->next);
        // if its not valid
        } else {
            break;
        }
    }
}

static void mark(HeapHeader* header) {
    assert(header != NULL);
    // if already marked
    if (header->marked) {
        return;
    }
    // mark object
    header->marked = true;
}

void gc_sweep() {
    sweep();
}

static void sweep() {
    HeapHeader* current = heap_get_header(heap_list_head);
    HeapHeader* temp;
    // for each object in the pointer linked list
    while (current != NULL) {
        // if the object is not marked
        if (!current->marked) {
            // free the object
            temp = heap_get_header(current->next);
            heap_free(current->data);
            current = temp;
        // if the object is marked
        } else {
            // dont free
            current = heap_get_header(current->next);
        }
    }
}

void gc_collect() {
    gc_mark();
    gc_sweep();
}
