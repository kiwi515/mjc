/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Course:  CSE 4251, Section 01, Spring 2023
 * Project: MiniJava Compiler Project
 * Charset: US-ASCII
 */

class Main {
    public static void main(String[] a) {
        System.out.println(new RcArgumentTest().execute());
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

class RcArgumentTest {
    public int execute() {
        Object a;
        Object b;
        int dummy;

        a = new Object();
        b = new Object();

        dummy = this.simple(a, b);
        dummy = this.clobber(a, b);

        return 0;
    }

    /**
     * Function to test refcount adjustment of function parameters
     */
    public int simple(Object a, Object b) {
        return 0;
    }

    /**
     * Function to test refcount adjustment of overwritten function parameters
     */
    public int clobber(Object a, Object b) {
        a = new Object();
        b = a;
        return 0;
    }
}
