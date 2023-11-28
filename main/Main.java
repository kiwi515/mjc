/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Course:  CSE 4251, Section 01, Spring 2023
 * Project: MiniJava Compiler Project
 * Charset: US-ASCII
 */

package main;

public final class Main {
    public static void main(final String[] args) {
        int gcType = 0;
        // Check argument count
        if (args.length < 1) {
            return;
        }
        if (args.length >= 2) {
            if (args[1].equals("NONE")) {
                gcType = 0;
            }
            if (args[1].equals("REF_COUNT")) {
                gcType = 1;
            }
            if (args[1].equals("MARK_SWEEP")) {
                gcType = 2;
            }
            if (args[1].equals("COPYING")) {
                gcType = 3;
            }
        }
        // Get system properties
        Config.initialize();
        Arch.initialize();

        // Run test cases if specified to do so
        if (Config.isTest()) {
            test(gcType);
            return;
        }
        System.out.println(args[0]);
        // Begin logging errors
        Logger.begin(args[0]);
        // Compile source file
        compile(args[0], gcType);
        // Display error log
        Logger.end();
    }

    /**
     * Compile source file
     * 
     * @param fileName Source file name
     */
    private static void compile(final String fileName, final int gcType) {
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
            if (!translate.Phase.execute(gcType)) {
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
    private static void test(final int gcType) {
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
            compile(test, gcType);
            Logger.end();
        }
    }
}