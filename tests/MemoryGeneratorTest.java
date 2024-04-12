/*
 * Author:  Tyler Gutowski, tgutowski2020@my.fit.edu
 * Course:  CSE 4251, Section 01, Spring 2023
 * Project: MiniJava Compiler Project
 * Charset: US-ASCII
 */

class Main {
    public static void main(String[] args) {
        System.out.println(new MemoryGeneratorTest().execute());
    }
}

class Node {
    Node next;

    public int setNext(Node node) {
        next = node;
        return 0;
    }
}

class MemoryGeneratorTest {
    boolean cont;
    int[] array;
    public int execute() {
        cont = true;
        while (cont) {
            array = new int[50];
        }
        return 0;
    }
}
