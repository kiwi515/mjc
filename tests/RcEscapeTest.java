/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Course:  CSE 4251, Section 01, Spring 2023
 * Project: MiniJava Compiler Project
 * Charset: US-ASCII
 */

class Main {
    public static void main(String[] a) {
        System.out.println(new RcEscapeTest().execute());
    }
}

class Child {
    // Something to make class size > 0
    int dummyvar;
}

class Parent {
    Child child;

    public int setChild(Child o) {
        child = o;
        return 0;
    }
}

class RcEscapeTest {
    public int execute() {
        int dummy;

        // Create parent
        Parent p;
        p = new Parent();

        // Set child
        dummy = this.escape(p);

        return 0;
    }

    public int escape(Parent p) {
        int dummy;

        // Allocate local Child
        Child c;
        c = new Child(); // ref(c) == 1

        // Allocation from this function escapes this function
        dummy = p.setChild(c); // ref(c) == 2

        return 0;
    }
}
