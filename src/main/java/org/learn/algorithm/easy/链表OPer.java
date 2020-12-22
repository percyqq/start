package org.learn.algorithm.easy;

import lombok.ToString;

/**
 * @description:
 * @create: 2020-12-22 11:04
 */
public class 链表OPer {

    public static void main(String[] args) {
        Node<Integer> node1 = new Node().init(1, 3, 5);
        Node<Integer> node2 = new Node().init(2, 4, 6, 8, 0);

        Node ret = merge(node1, node2);
        System.out.println(ret);
    }

    private static Node<Integer> merge(Node<Integer> p1, Node<Integer> p2) {
        if (p1 == null) {
            return p2;
        }
        if (p2 == null) {
            return p1;
        }

        Node c1 = p1, c2 = p2;
        Node cursor = c1;
        while (c1 != null && c2 != null) {
            cursor = c1;        // 开始于p1
            c1 = c1.next;       // 获取p1 next
            cursor.next = c2;   // 修改p1 next

            c2 = c2.next;
            cursor.next.next = c1;
        }

        if (c1 != null) {
            cursor.next.next = c1;
        }

        if (c2 != null) {
            cursor.next.next = c2;
        }

        return p1;
    }

    @ToString
    private static class Node<T> {
        private T data;
        private Node next;

        private Node() {
        }

        private Node(T val) {
            this.data = val;
        }

        private Node init(T... vals) {
            if (vals == null) {
                throw new RuntimeException("node is null");
            }

            if (vals.length == 1) {
                return new Node(vals[0]);
            } else {
                Node head = new Node(vals[0]);
                Node cursor = head;
                for (int i = 1; i < vals.length; i++) {
                    Node node = new Node(vals[i]);
                    cursor.next = node;
                    cursor = cursor.next;
                }
                return head;
            }
        }

        private void print() {
            Node next = this;
            do {
                System.out.print(" -> " + next);
            } while ((next = next.next) != null);
        }

        @Override
        public String toString() {
            return "Node(data = " + data + ", next = " + next + ")";
        }
    }


}
