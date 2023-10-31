/*
 * Author:  Tyler Gutowski
 */

class Main {
    public static void main(String[] args) {
        System.out.println(new RcValidPointerTest().execute());
    }
}

/**
 * Test object
 */
class Object {
	
    int heapBlockTag;
	int fillervar1; 
	int fillervar2;
	int fillervar3; 
	int fillervar4;
	int fakePointer;
	
	public int Object() {
		// Match the HEAP_BLOCK bytes
		// "HBLK" -> 48424c4b -> 1212304459
		heapBlockTag = 1212304459;
		fillervar1 = 0; 
		fillervar2 = 0;
		fillervar3 = 0; 
		fillervar4 = 0;
		fakePointer = 3735928559;
		return 0;
	}
}

class RcValidPointerTest {
    public int execute() {
        Object o;
        o = new Object();
        return 0;
    }
}
