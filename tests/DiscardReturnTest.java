/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Course:  CSE 4251, Section 01, Spring 2023
 * Project: MiniJava Compiler Project
 * Charset: US-ASCII
 */

class Main {
    public static void main(String[] a) {
        System.out.println(new DiscardReturnTest().execute());
    }
}

class DiscardReturnTest {
    public int execute() {
        this.dummy();
        return 0;
    }

    public int dummy() {
        return 0;
    }
}
