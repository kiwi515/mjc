/*
 * Author:  Tyler Gutowski, tgutowski2020@my.fit.edu
 * Course:  CSE 4251, Section 01, Spring 2023
 * Project: MiniJava Compiler Project
 * Charset: US-ASCII
 */

class Main {
    public static void main(String[] args) {
        System.out.println(new FragmentedHeapTest().execute());
    }
}

class Node {
    Node next;

    public int setNext(Node node) {
        next = node;
        return 0;
    }
}

class FragmentedHeapTest {
    int[] block1;
    int[] block2;
    int[] block3;
    int[] block4;
    int[] block5;
    int[] block6;
    int[] heaptest;
    public int execute() {
        // Our heap for this test should be 1024 bytes.
        // We create blocks of memory of varying sizes
        block1 = new int[50];
        block2 = new int[50];
        block3 = new int[50];
        block4 = new int[50];
        block5 = new int[50];
        block6 = new int[50];
        // We delete specific blocks of memory to fragment it
        block2 = new int [1];
        block4 = new int [1];
        block5 = new int [1];
        // We try to allocate something big. 
        // It should fail to allocate because of the fragmentation
        heaptest = new int [50];

        return 0;
    }
}
