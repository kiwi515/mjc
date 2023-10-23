/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Course:  CSE 4251, Section 01, Spring 2023
 * Project: MiniJava Compiler Project
 * Charset: US-ASCII
 */

class Main {
    public static void main(String[] a) {
        System.out.println(new RefCountTest().execute());
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

/**
 * Test object (circular link)
 */
class CircularObject {
    CircularObject next;
    CircularObject prev;

    public int dummymtd() {
        return 0;
    }
}

class RefCountTest {
    public int execute() {
        int dummy;
        dummy = this.createRef();
        dummy = this.initRef();
        return 0;
    }

    /**
     * Test creating a reference
     */
    public int createRef() {
        Object o;
        return 0;
    }

    /**
     * Test initializing a reference
     */
    public int initRef() {
        Object o;
        o = new Object();
        o = new Object();
        return 0;
    }
}
