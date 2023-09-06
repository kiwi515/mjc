/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Course:  CSE 4251, Section 01, Spring 2023
 * Project: MiniJava Compiler Project
 * Charset: US-ASCII
 */

class Main {
    public static void main(String[] a) {
        System.out.println(new ConstantFoldingTest().execute());
    }
}

class ConstantFoldingTest {
    public int execute() {
        int dummy_i;
        boolean dummy_b;
        int[] dummy_a;

        // Compile-time constant folding in less-than expressions,
        // and logical AND expressions
        dummy_b = (1 < 5) && (5 < 1); // dummy_b = false
        dummy_b = (1 < 5) && (1 < 10); // dummy_b = true

        // Compile-time constant folding in BINOP arithmetic
        dummy_i = 1 + 2; // dummy_i = 3
        dummy_i = 2 - 1; // dummy_i = 1
        dummy_i = 2 * 2; // dummy_i = 4
        dummy_i = dummy_i * 1; // dummy_i = dummy_i
        dummy_i = 0 * dummy_i; // dummy_i = 0

        // Compile-time constant folding in logical NOT expressions
        dummy_b = (!true); // dummy_b = false
        dummy_b = (!(1 < 5)); // dummy_b = false

        // Compile-time constant folding when computing
        // array subscript offset, where the index is a constant literal
        dummy_a = new int[32];
        // &dummy_a[16] -> (&dummy_a + (16 + 1) * 4) -> (&dummy_a + 68)
        dummy_a[16] = 999;

        return 1 + 2 * 3 + 5; // 17
    }
}
