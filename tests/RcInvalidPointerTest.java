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
	int fakePointer = 0x12345678;
	
    public int dummymtd() {
        return 0;
    }
}

class RcInvalidPointerTest {
    public int execute() {
        Object o;
        o = new Object();
        return 0;
    }
}
