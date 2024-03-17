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
    static char msg_buf[512];
    static char all_buf[1024];

    // Format message
    va_list list;
    va_start(list, msg);
    vsnprintf(msg_buf, sizeof(msg_buf), msg, list);
    va_end(list);

    // Format file/line information
    snprintf(all_buf, sizeof(all_buf), "[%s:%04d] %s", file, line, msg_buf);

    // Write to stderr
    fprintf(stderr, all_buf);
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
    static char msg_buf[512];
    static char all_buf[1024];

    // Format message
    va_list list;
    va_start(list, msg);
    vsnprintf(msg_buf, sizeof(msg_buf), msg, list);
    va_end(list);

    // Format file/line information
    snprintf(all_buf, sizeof(all_buf),
             "ASSERTION FAILED!\n%s\nFile: %s, line %d.\n", msg_buf, file,
             line);

    // Write to stderr
    fprintf(stderr, all_buf);

    // Terminate program
    exit(EXIT_FAILURE);
}
