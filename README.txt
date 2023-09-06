/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Course:  CSE 4251, Section 01, Spring 2023
 * Project: MiniJava Compiler Project
 * Charset: US-ASCII
 */

==========================================================================
Status of Appel's testcases
==========================================================================
BinarySearch.java: Compiles OK, executes OK, and gives the correct output
BinaryTree.java:   Compiles OK, executes OK, and gives the correct output
BubbleSort.java:   Compiles OK, executes OK, and gives the correct output
Factorial.java:    Compiles OK, executes OK, and gives the correct output
LinearSearch.java: Compiles OK, executes OK, and gives the correct output
LinkedList.java:   Compiles OK, executes OK, and gives the correct output
QuickSort.java:    Compiles OK, executes OK, and gives the correct output
TreeVisitor.java:  Compiles OK, executes OK, but gives the WRONG output 
                   (due to no dynamic dispatch implementation)

Total:
      - Compiled OK:    8/8
      - Executed OK:    8/8
      - Correct output: 7/8

==========================================================================
Status of my testcases
==========================================================================
##########################################################################
# NOTE: Test.java exists in the same directory as the compiler           #
# (as specified by the Assignment 8 instruction).                        #
#                                                                        #
#     All other test cases mentioned here are inside the "tests"         #
# directory. I'm not even sure if I am allowed to submit multiple,       #
# because the asssignment description says "You must also submit your    #
# one best test case"; however, the README section of the assignment     #
# mentions optionally supplying other files (such as MyTestCase1.java).  #
#                                                                        #
#    If you would like to run these extra cases, they exist inside       #
# the "tests" directory. The main case (Test.java) and all other cases   #
# are explained below in detail.                                         #
##########################################################################

Test.java: My "one best testcase". Demonstrates that my compiler supports
           the following concepts:
           - Printing
           - Integer/boolean expressions
           - Local variables
           - If-else statements
           - Else-if statements
           - While statements
           - Function calls (with formal parameters)
           - Object creation
           - Class field/method access (including ones inherited from
             base class FizzBuzzImpl)
           - Recursion

IROptimizerTest.java: Another test case. Demonstrates that my compiler's
                      front-end supports the following optimizations:
    [NOTE: These are not to be confused with constant *propagation*]
    [NOTE 2: This test case can be executed, but it is meant for
             observing the assembly code output]
                      - Compile-time constant folding in logical AND
                        expressions
                          - (x && false) -> (false)
                          - (x && true)  -> (x)

                      - Compile-time constant folding in LessThan
                        expressions
                          - (1 < 5) -> (true)
                          - (5 < 1) -> (false)

                      - Compile-time constant folding in BINOP
                        arithmetic (Plus/Minus/Times)

                      - Compile-time constant folding in logical NOT
                        expressions
                          - (!true)    -> (false)
                          - (!(1 < 5)) -> (false) 

                      - Compile-time constant folding when computing
                        array subscript offset, where the index is
                        a constant literal
                          - myArray[10] -> *(&myArray + (10 + 1) * 4)
                            -> *(&myArray + 44)

ArrayTest.java: Another test case (albeit small). Demonstrates that my
                compiler supports the following concepts not present
                in Test.java:
                - Array creation
                - Array lookup
                - Array assignment
                - Array length field

DefUseTest.java: Another much smaller test case. Demonstrates that my
                 compiler supports detecting uninitialized local variables
                 during the semantic analysis phase.
    [NOTE: This test case is meant to be compiled, but it is meant to
           throw errors (as there are uninitialized variables detected).]

==========================================================================
Optional features
==========================================================================
    The only optional feature I implemented was the ability to detect
uninitialized local variables. (See check.DefUseVisitor for implementation)

==========================================================================
Re: Zero-size object creation
==========================================================================
    When invoking the new operator to create a class instance, if the size
of the class is zero it will be substituted with a null pointer (CONST 0).
This is because there's no point in calling runtime_alloc_object to get a
buffer of zero bytes.

    It's not obvious from the assembly that this is happening, so I wanted
to mention it here.

==========================================================================
Re: Register allocation / back-end optimization
==========================================================================
    I believe my method of register allocation would be considered
"stupid" register allocation. Registers are assigned on a first-come,
first-serve basis, although they become re-available after their owner's
(temp's) lifetime ends.

    My "lifetime" data is a bit naive, it doesn't take into account things
like the order of basic blocks/control flow: it simply has a start and end
point in the code fragment.

    Despite this, it seems to have worked out nicely. This is in part due
to the optimizations I've written in (see optimize.Phase and/or
codegen.arch.sparc.SparcOptimizer). The optimization heuiristics are also
somewhat naive, but I am so happy with how they've managed to clean up
the assembly code. Because the optimization phase comes before the
register allocation phase, the optimizations also help me save registers.
