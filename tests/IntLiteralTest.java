/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Course:  CSE 4251, Section 01, Spring 2023
 * Project: MiniJava Compiler Project
 * Charset: US-ASCII
 */

class Main {
    public static void main(String[] a) {
        System.out.println(new IntLiteralTest().execute());
    }
}

class IntLiteralTest {
    public int execute() {
        int i;

        i = 0;
        i = 100;
        i = 0xABCDEF;

        return 0;
    }
}
