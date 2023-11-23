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

// marks all reachable objects
void gc_mark() {
    HeapHeader* current = (HeapHeader*)heap_list_head; 
    while (current != NULL) {
        // ignore invalid heap headers
        if (heap_is_header(current)) {
            // mark the block if it's in use
            if (current->ref > 0) {
                mark(current);
            }
        }
        current = (HeapHeader*)(current->next);
    }
}

// marks a single heap header
static void mark(HeapHeader* header) {
    assert(header != NULL);
    // only mark the header if it's not already marked
    if (!header->marked) {
        header->marked = true;
    }
}

void gc_sweep() {
    sweep();
}

// sweep phase
static void sweep() {
    HeapHeader* current = (HeapHeader*)heap_list_head;
    HeapHeader* prev = NULL;

    while (current != NULL) {
        HeapHeader* next = (HeapHeader*)(current->next);

        // free unmarked objects
        if (!current->marked) {
            if (prev) {
                prev->next = current->next;
            }
            heap_free(current->data);
        } else {
            // unmark for next gc cycle and update previous pointer
            current->marked = false;
            prev = current;
        }

        current = next;
    }
}

void gc_collect() {
    gc_mark();
    gc_sweep();
}
