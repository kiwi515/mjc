/*
 * Author:  Tyler Gutowski
 */

class Main {
    public static void main(String[] a) {
        System.out.println(new RcValidPointerTest().execute());
    }
}

class Parent { //0x23008
    Child child; // 0x23012
    int heapBlockTag; // 0x23016
    int fillervar1; // 0x23020
    int fillervar2; // 0x23024
    int fillervar3; // 0x23028
    int fillervar4; // 0x23032
    int fillervar5; // 0x23036

    public int setValues() {
        // Match the HEAP_BLOCK bytes
	// "HBLK" -> 48424c4b -> 1212304459
	heapBlockTag = 1212304459;
	fillervar1 = 0; 
	fillervar2 = 0;
	fillervar3 = 0; 
	fillervar4 = 0;
	fillervar5 = 0; // 0x23008 (Start of the heap)
	return 0;
    }

    public int printMemory() {
        System.out.println(143414);
        return 0;
    }
 
    public int setChild(Child o) {
	child = o;
        return 0;
    }	
}

class Child {
    int memoryAddress;

    public int setValues() {
	memoryAddress = 143414;
	return 0;
    }
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
	dummy = c.setValues();
	dummy = p.printMemory();
	return 0;
    }
}
