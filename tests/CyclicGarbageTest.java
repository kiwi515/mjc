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
        this.leak();
        return 0;
    }
}
