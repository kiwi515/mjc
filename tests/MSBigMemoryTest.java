class Main {
    public static void main(String[] a) {
        System.out.println(new MSBigMemoryTest().execute());
    }
}

class Object {
    int dummyvar;
    char[] hugeArray; // A field to hold a large character array

    public Object() {
        // Attempt to allocate a massive character array
        // Note: The size specified here is just an example and may need to be adjusted
        // based on the memory capabilities of the system where this is run.
        hugeArray = new char[Integer.MAX_VALUE]; // Integer.MAX_VALUE is the largest size for an array in Java
    }

    public int dummymtd() {
        return 0;
    }
}

class MSBigMemoryTest {
    public int execute() {
        try {
            Object o;
            o = new Object(); // This allocation should fail if the system cannot handle such a large array

            // If the above line does not throw an OutOfMemoryError, the allocation somehow succeeded
            return 1; // Indicate test failure
        } catch (OutOfMemoryError e) {
            // Expected outcome if the allocation fails
            return 0; // Indicate test success
        }
    }
}
