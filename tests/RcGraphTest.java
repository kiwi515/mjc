/*
 * Author:  Tyler Gutowski, tgutowski2020@my.fit.edu
 * Course:  CSE 4251, Section 01, Spring 2023
 * Project: MiniJava Compiler Project
 * Charset: US-ASCII
 */

class Main {
    public static void main(String[] args) {
        System.out.println(new RcGraphTest().execute());
    }
}

class Node {
    Node next;

    public int setNext(Node node) {
        next = node;
        return 0;
    }
}

class RcGraphTest {
    public int execute() {
        int dummy;
        Node none;
        Node node1;
        Node node2;
        Node node3;
        Node node4;

        // Create a simple graph
        none = new Node();
        node1 = new Node();   
        node2 = new Node();
        node3 = new Node();
        node4 = new Node();

        // Create references
        dummy = this.linkNodes(node1, node2);
        dummy = this.linkNodes(node2, node3);
        dummy = this.linkNodes(node3, node4);
        dummy = this.linkNodes(node4, node1);

        // All pointers now go to "none" node. (NO NULL OPERATOR AAHAHAHAHA)
        node1 = none;
        node2 = none;
        node3 = none;
        node4 = none;

        return 0;
    }

    public int linkNodes(Node nodeA, Node nodeB) {
        int dummy;
        dummy = nodeA.setNext(nodeB);
        return 0;
    }
}
