/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Author:  Tyler Gutowski, tgutowski2020@my.fit.edu
 * Course:  CSE 4101, Fall 2023
 * Project: Heap Heap Hooray
 * Charset: US-ASCII
 */

class Main {
    public static void main(String[] a) {
        System.out.println(new GenerationTest().execute());
    }
}

// Designed for heap_size 0x10000
class GenerationTest {
    // Leak most of gen zero memory
    public int dummy0() {
        int[] a00;
        a00 = new int[0x3C00]; // 0xF000 / sizeof(int)
        return 0;
    }

    public int execute() {
        int[] a01;
        int[] a02;
        this.dummy0();

        // 1. This will fail to allocate in gen 0.
        // 2. a00 will get sweeped (gen0 0x1000 free -> gen0 0x10000 free).
        // 3. Allocation will succeed (gen0 0x10000 free -> gen0 0x1000 free).
        a01 = new int[0x3C00]; // 0xF000 / sizeof(int)

        // 1. This will fail to allocate in gen 0.
        // 2. a01 will survive (move from gen 0 -> gen 1).
        // 2a. This moves memory usage from gen 0 to gen 1.
        //     (gen0 0x1000 free -> gen0 0x10000 free, gen1 0x10000 free -> gen1 0x1000 free)
        // 3. Allocation will succeed in gen 0.
        a02 = new int[0x800]; // 0x2000 / sizeof(int)

        // Both generations now have 0x1000 free space.
        return 0;
    }
}
