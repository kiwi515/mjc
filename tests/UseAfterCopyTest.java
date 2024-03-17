/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Author:  Tyler Gutowski, tgutowski2020@my.fit.edu
 * Course:  CSE 4101, Fall 2023
 * Project: Heap Heap Hooray
 * Charset: US-ASCII
 */

class Main {
    public static void main(String[] a) {
        System.out.println(new UseAfterCopyTest().execute());
    }
}

class MyClass {
    int value;

    public int setValue(int x) {
        value = x;
        return 0;
    }

    public int getValue() {
        return value;
    }
}

class UseAfterCopyTest {
    public int execute() {
        MyClass dummy;

        // Dummy root variable. Should be marked by the GC.
        dummy = new MyClass();
        dummy.setValue(12345);

        // Force garbage collection cycle.
        System.gc();

        return dummy.getValue();
    }
}
