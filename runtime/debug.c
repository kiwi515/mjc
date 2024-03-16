/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Author:  Tyler Gutowski, tgutowski2020@my.fit.edu
 * Course:  CSE 4101, Fall 2023
 * Project: Heap Heap Hooray
 * Charset: US-ASCII
 */

#include "debug.h"
#include "types.h"
#include <stdio.h>
#include <stdlib.h>

/**
 * @brief Log a message to the console
 * @note Uses stderr in case stdout can't be flushed
 *
 * @param file Source file name where MJC_LOG was written
 * @param line Source file line where MJC_LOG was written
 * @param msg
 */
void debug_log(const char* file, int line, const char* msg, ...) {
    static char msgbuf[512];
    static char allbuf[1024];

    // Format message
    va_list list;
    va_start(list, msg);
    vsnprintf(msgbuf, sizeof(msgbuf), msg, list);
    va_end(list);

    // Format file/line information
    snprintf(allbuf, sizeof(allbuf), "[%s:%d] %s", file, line, msgbuf);

    // Write to stderr
    fprintf(stderr, allbuf);
}

/**
 * @brief Log assertion failure and terminate program
 *
 * @param file Source file name where MJC_ASSERT* was written
 * @param line Source file name where MJC_ASSERT* was written
 * @param msg Assertion message
 * @param ... Format string arguments (optional)
 */
void debug_fail_assert(const char* file, int line, const char* msg, ...) {
    static char msgbuf[512];
    static char allbuf[1024];

    // Format message
    va_list list;
    va_start(list, msg);
    vsnprintf(msgbuf, sizeof(msgbuf), msg, list);
    va_end(list);

    // Format file/line information
    snprintf(allbuf, sizeof(allbuf),
             "ASSERTION FAILED!\n%s\nFile: %s, line %d.\n", msgbuf, file, line);

    // Write to stderr
    fprintf(stderr, allbuf);

    // Terminate program
    exit(EXIT_FAILURE);
}
