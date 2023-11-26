class Main {
    public static void main(String[] a) {
        System.out.println(new MSBigMemoryTest().execute());
    }
}

class Object {
    int[] bigArray;
    int[] hugeArray;
    int[] massiveArray;
    public int initialize() {
        bigArray = new int[5000];
        hugeArray = new int[50000];
        massiveArray = new int[500000];
        return 0;
    }
}

class MSBigMemoryTest {
    public int execute() {
        int dummy;
        Object o;
        o = new Object();
        dummy = o.initialize();
        return 0;
    }
}
