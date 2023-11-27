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

class CyclicThingy {
    CyclicThingy myfriend;

    public int set(CyclicThingy other) {
        myfriend = other;
        return 0;
    }
}

class Buffer {
    int[] data;

    public int init() {
        data = new int[2000000000];
        return 0;
    }
}

class CyclicGarbageTest {
    public int leak() {
        CyclicThingy one;
        CyclicThingy two;

        one = new CyclicThingy();
        two = new CyclicThingy();

        one.set(two);
        two.set(one);

        return 0;
    }

    public int execute() {
        Buffer b;
        b = new Buffer();

        // Leak memory through cyclic references.
        // When this function returns, the cyclic objects will not be roots.
        // Therefore, mark-sweep GC shall collect them.
        this.leak();

        // This function will allocate enough memory to force GC.
        // 'Buffer b' is a root and shall be marked by the GC.
        b.init();

        return 0;
    }
}
