/*
 * Author:  Tyler Gutowski
 */

#ifndef MINI_JAVA_COMPILER_MARKSWEEP_H
#define MINI_JAVA_COMPILER_MARKSWEEP_H
#include "types.h"

void marksweep_mark(void);
void marksweep_sweep(void);
void marksweep_collect(void);

#endif
