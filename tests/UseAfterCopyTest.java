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

class Node {
    Node next;
    int dummy;

    public int setNext(Node n) {
        next = n;
        return 0;
    }

    public int setDummy(int x) {
        dummy = x;
        return 0;
    }

    public int getDummy() {
        return dummy;
    }
}

class UseAfterCopyTest {
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
        markme.setDummy(12345);

        // Leak memory via cyclic reference.
        this.leak();

        // Attempt to reclaim memory.
        System.gc();

        // Use 'markme' after it has been copied to the new heap.
        // If pointer correction is OK, this will return 12345.
        return markme.getDummy();
    }
}
