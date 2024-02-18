/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Author:  Tyler Gutowski, tgutowski2020@my.fit.edu
 * Course:  CSE 4101, Fall 2023
 * Project: Heap Heap Hooray
 * Charset: US-ASCII
 */
#ifndef MINI_JAVA_COMPILER_DEBUG_H
#define MINI_JAVA_COMPILER_DEBUG_H
// #include "types.h" <-- Must NOT include to avoid circular dependency

/*=======================================================*/
/*                       Logging                         */
/*=======================================================*/

/**
 * @brief Log message to the console
 *
 * @param msg Message
 * @param ... Format string arguments (optional)
 */
#define MJC_LOG(msg, ...) debug_log(__FILE__, __LINE__, msg, ##__VA_ARGS__)

void debug_log(const char* file, int line, const char* msg, ...);

/*=======================================================*/
/*                        Assert                         */
/*=======================================================*/

/**
 * @brief Assert conditional expression
 *
 * @param expr Expression
 */
#define MJC_ASSERT(expr)                                                       \
    (expr) ? (void)0 : debug_fail_assert(__FILE__, __LINE__, #expr)

/**
 * @brief Assert conditional expression
 *
 * @param expr Expression
 * @param ... Message (can also be format string + arguments)
 */
#define MJC_ASSERT_MSG(expr, ...)                                              \
    (expr) ? (void)0 : debug_fail_assert(__FILE__, __LINE__, ##__VA_ARGS__)

void debug_fail_assert(const char* file, int line, const char* msg, ...);

/*=======================================================*/
/*                   Memory allocation                   */
/*=======================================================*/

/**
 * @brief Allocate a block of memory (NOT in our heap, but from the STL)
 *
 * @param size Block size
 */
#ifdef VERBOSE
#define MJC_ALLOC(size) (MJC_LOG("MJC_ALLOC: size=%d\n", size), malloc(size))
#else
#define MJC_ALLOC(size) malloc(size)
#endif

/**
 * @brief Free a block of memory
 *
 * @param block Memory block
 */
#ifdef VERBOSE
#define MJC_FREE(block) (MJC_LOG("MJC_FREE: block=%p\n", block), free(block))
#else
#define MJC_FREE(block) free(block)
#endif

#endif
