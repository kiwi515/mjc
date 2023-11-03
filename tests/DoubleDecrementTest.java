/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Course:  CSE 4251, Section 01, Spring 2023
 * Project: MiniJava Compiler Project
 * Charset: US-ASCII
 */

class Main {
    public static void main(String[] a) {
        System.out.println(new DoubleDecrementTest().execute());
    }
}

class Pointer {
    int value;

    public int set(int x) {
        value = x;
        return 0;
    }
}

class DoubleDecrementTest {
    public int execute() {
        Pointer p;
        Pointer p2;

        p = new Pointer();
        p2 = new Pointer();

        p.set(0x23030);
        p2.set(0x23018);
        return 0;

        // p goes out of scope:
        // dec(p) -> dec(p2) through child
        // p2 goes out of scope:
        // dec(p2) -> error
    }
}
