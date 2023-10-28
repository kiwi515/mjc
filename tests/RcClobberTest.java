/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Course:  CSE 4251, Section 01, Spring 2023
 * Project: MiniJava Compiler Project
 * Charset: US-ASCII
 */

class Main {
    public static void main(String[] a) {
        System.out.println(new RcClobberTest().execute());
    }
}

/**
 * Test object
 */
class Object {
    int dummyvar;

    public int dummymtd() {
        return 0;
    }
}

class RcClobberTest {
    public int execute() {
        Object o;
        o = new Object();

        // Clobber reference
        o = new Object();

        return 0;
    }
}
