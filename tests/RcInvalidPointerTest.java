/*
 * Author:  Tyler Gutowski
 */

class Main {
    public static void main(String[] a) {
        System.out.println(new RcInvalidPointerTest().execute());
    }
}

/**
 * Test object
 */
class Object {
	int fakePointer;
	
    public int init() {
        fakePointer = 12345678;
        return 0;
    }
}

class RcInvalidPointerTest {
    public int execute() {
        int dummy;

        Object o;
        o = new Object();
        dummy = o.init();

        return 0;
    }
}
