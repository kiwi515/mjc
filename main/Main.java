/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Course:  CSE 4251, Section 01, Spring 2023
 * Project: MiniJava Compiler Project
 * Charset: US-ASCII
 */

package main;

public final class Main {
    public static void main(final String[] args) {
        // Check argument count
        if (args.length < 1) {
            return;
        }

        // Get system properties
        Config.initialize();
        Arch.initialize();

        // Run test cases if specified to do so
        if (Config.isTest()) {
            test();
            return;
        }
        System.out.println(args[0]);
        // Begin logging errors
        Logger.begin(args[0]);
        // Compile source file
        compile(args[0]);
        // Display error log
        Logger.end();

        if (args.length < 2) {
            return;
        }
        System.out.println(args[1]);
    }

    /**
     * Compile source file
     * 
     * @param fileName Source file name
     */
    private static void compile(final String fileName) {
        /**
         * Convert source file path to assembly file path.
         */
        final String asmFileName = fileName.replace(".java", ".s");

        /**
         * I like to think my error handling is pretty robust, but in the case
         * that some unknown exception occurs during testing, we silently fail
         */
        try {
            // Compiler parse phase
            if (!parse.Phase.execute(fileName)) {
                return;
            }

            // Compiler check phase
            if (!check.Phase.execute()) {
                return;
            }

            // Compiler translate phase
            if (!translate.Phase.execute()) {
                return;
            }

            // Compiler codegen phase
            if (!codegen.Phase.execute()) {
                return;
            }

            // Compiler optimization phase
            if (!optimize.Phase.execute()) {
                return;
            }

            // Compiler register allocation phase
            if (!regalloc.Phase.execute()) {
                return;
            }

            // Compiler write phase
            if (!write.Phase.execute(asmFileName)) {
                return;
            }
        } catch (final Exception e) {
            Logger.logVerboseLn(e.toString());
        }
    }

    /**
     * Test the compiler against Appel's testcases (and my own)
     */
    private static void test() {
        final String[] tests = {
                // Appel
                "tests/BinarySearch.java",
                "tests/BinaryTree.java",
                "tests/BubbleSort.java",
                "tests/Factorial.java",
                "tests/LinearSearch.java",
                "tests/LinkedList.java",
                "tests/QuickSort.java",
                "tests/TreeVisitor.java",
                // Custom
                "tests/Test.java",
                "tests/IROptimizerTest.java",
                "tests/ArrayTest.java",
                "tests/DefUseTest.java",
                "tests/RefCountTest.java"
        };

        for (final String test : tests) {
            Logger.begin(test);
            compile(test);
            Logger.end();
        }
    }
}