/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Course:  CSE 4251, Section 01, Spring 2023
 * Project: MiniJava Compiler Project
 * Charset: US-ASCII
 */

class Main {
    public static void main(String[] a) {
        System.out.println(new DefUseTest().execute());
    }
}

class DefUseTest {
    public int execute() {
        int dummy;
        int[] arr;
        int i;

        // Random OK code
        i = 0;
        while (i < 32) {
            i = i + 1;
        }

        // Attempt to use uninitialized variable
        System.out.println(dummy);
        // Attempt to use uninitialized variable AGAIN
        arr[0] = 1;

        return 0;
    }
}
