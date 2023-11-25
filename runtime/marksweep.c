/*
 * Author:  Tyler Gutowski
 */

#include "marksweep.h"
#include "heap.h"
#include <assert.h>

static void mark(HeapHeader* header);
static void sweep(void);

// marks all reachable objects
void marksweep_mark(void) {
    HeapHeader* current = heap_list_head;
    while (current != NULL) {
        // ignore invalid heap headers
        if (heap_is_header(current)) {
            // mark the block if it's in use
            if (current->ref > 0) {
                mark(current);
            }
        }
        current = current->next;
    }
}

// marks a single heap header
static void mark(HeapHeader* header) {
    assert(header != NULL);
    // only mark the header if it's not already marked
    if (!header->marked) {
        header->marked = TRUE;
    }
}

void marksweep_sweep() { sweep(); }

// sweep phase
static void sweep(void) {
    HeapHeader* current = heap_list_head;
    HeapHeader* prev = NULL;

    while (current != NULL) {
        HeapHeader* next = current->next;

        // free unmarked objects
        if (!current->marked) {
            if (prev) {
                prev->next = current->next;
            }
            heap_free(current->data);
        } else {
            // unmark for next gc cycle and update previous pointer
            current->marked = FALSE;
            prev = current;
        }

        current = next;
    }
}

void marksweep_collect(void) {
    marksweep_mark();
    marksweep_sweep();
}
