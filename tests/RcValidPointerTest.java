/*
 * Author:  Tyler Gutowski
 */

class Main {
    public static void main(String[] a) {
        System.out.println(new RcValidPointerTest().execute());
    }
}

class Parent {
    Child child;
    int heapBlockTag;
    int fillervar1; 
    int fillervar2;
    int fillervar3; 
    int fillervar4;
    int fakePointer;

    public int setValues() {
        // Match the HEAP_BLOCK bytes
	// "HBLK" -> 48424c4b -> 1212304459
	heapBlockTag = 1212304459;
	fillervar1 = 0; 
	fillervar2 = 0;
	fillervar3 = 0; 
	fillervar4 = 0;
	fakePointer = 0 - 889275714;
	return 0;
    }

    public int setChild(Child o) {
	child = o;
        return 0;
    }	
}

class Child {
    int dummy;
}

class RcValidPointerTest {
    public int execute() {
        int dummy;
        
	Parent p;
	Child c;
	
	p = new Parent();
        c = new Child();

	dummy = p.setChild(c);
        dummy = p.setValues();
        return 0;
    }
}
