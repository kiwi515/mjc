/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Author:  Tyler Gutowski, tgutowski2020@my.fit.edu
 * Course:  CSE 4101, Fall 2023
 * Project: Heap Heap Hooray
 * Charset: US-ASCII
 */

class Main {
    public static void main(String[] a) {
        System.out.println(new CyclicGarbageTest().execute());
    }
}

class Node {
    Node next;

    public int setNext(Node n) {
        next = n;
        return 0;
    }
}

class CyclicGarbageTest {
    public int leak() {
        Node one;
        Node two;
        one = new Node();
        two = new Node();

        one.setNext(two);
        two.setNext(one);
        return 0;
    }

    public int execute() {
        Node markme;

        // Dummy root variable. Should be marked by the GC.
        markme = new Node();

        // Leak memory via cyclic reference.
        this.leak();

        // Attempt to reclaim memory.
        System.gc();

        return 0;
    }
}
