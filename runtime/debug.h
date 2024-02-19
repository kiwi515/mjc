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
#ifndef NDEBUG
#define MJC_LOG(msg, ...) debug_log(__FILE__, __LINE__, msg, ##__VA_ARGS__)
#else
#define MJC_LOG(msg, ...) ((void)0)
#endif

void debug_log(const char* file, int line, const char* msg, ...);

/*=======================================================*/
/*                        Assert                         */
/*=======================================================*/

/**
 * @brief Assert conditional expression
 *
 * @param expr Expression
 */
#ifndef NDEBUG
#define MJC_ASSERT(expr)                                                       \
    (expr) ? (void)0 : debug_fail_assert(__FILE__, __LINE__, #expr)
#else
#define MJC_ASSERT(expr) ((void)0)
#endif

/**
 * @brief Assert conditional expression
 *
 * @param expr Expression
 * @param ... Message (can also be format string + arguments)
 */
#ifndef NDEBUG
#define MJC_ASSERT_MSG(expr, ...)                                              \
    (expr) ? (void)0 : debug_fail_assert(__FILE__, __LINE__, ##__VA_ARGS__)
#else
#define MJC_ASSERT_MSG(expr, ...) ((void)0)
#endif

void debug_fail_assert(const char* file, int line, const char* msg, ...);

/*=======================================================*/
/*                   Memory allocation                   */
/*=======================================================*/

/**
 * @brief Allocate a block of memory (NOT in our heap, but from the STL)
 *
 * @param size Block size
 */
#define MJC_ALLOC(size) malloc(size)

/**
 * @brief Free a block of memory (NOT to our heap, but to the STL)
 *
 * @param block Memory block
 */
#define MJC_FREE(block) free(block)

#endif
