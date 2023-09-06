/*
 * Author:  Trevor Schiff, tschiff2020@my.fit.edu
 * Course:  CSE 4251, Section 01, Spring 2023
 * Project: MiniJava Compiler Project
 * Charset: US-ASCII
 */

class Main {
    public static void main(String[] a) {
        System.out.println(new ArrayTest().execute());
    }
}

class ArrayTest {
    public int execute() {
        int[] arr;
        int i;

        // Create array
        arr = new int[32];

        // Initialize array values
        i = 0;
        while (i < arr.length) {
            arr[i] = i;
            i = i + 1;
        }

        // Print array values
        i = 0;
        while (i < arr.length) {
            System.out.println(arr[i]);
            i = i + 1;
        }

        // Return last element
        i = arr.length;
        return arr[i - 1];
    }
}
