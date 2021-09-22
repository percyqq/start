package org.learn.algorithm.easy;

import com.google.common.collect.Lists;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.LinkedList;

/**
 * @description:
 * @create: 2020-07-28 14:19
 */
public class 合并有序链表 {

    public static class Node<T> implements Comparable<T> {
        T val;
        Node<T> next = null;
        Node<T> prev = null;

        public Node(T val, Node<T> next, Node<T> prev) {
            if (val == null) {
                throw new RuntimeException("null value!");
            }
            this.val = val;
            this.next = next;
            this.prev = prev;
        }

        @Override
        public int compareTo(T o) {
            return this.compareTo(o);
        }
    }

    public static class ListNode<T> {
        Node<T> first;
        Node<T> last;
        int count = 0;

        public ListNode(T val) {
            this.first = this.last = new Node(val, null, null);
            count++;
        }

        public ListNode(T... values) {
            if (values != null && values.length > 0) {
                for (int i = 0; i < values.length; i++) {
                    addNodeAtLast(values[i]);
                }
            }
        }

        public boolean addNodeAtLast(T val) {
            if (count == 0) {
                this.first = this.last = new Node(val, null, null);
                count++;
            } else {
                Node<T> last = this.last;
                this.last = new Node(val, null, last);

                Node<T> prev = this.last.prev;
                prev.next = this.last;
                if (this.first.next == null) {
                    // == 2的时候开始绑定first的next
                    this.first.next = last;
                }
                count++;

                // == 3
            }
            return true;
        }


        public boolean isEmpty() {
            return count == 0;
        }

        public Node<T> pollFirst() {
            return this.first;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder("[");
            if (last == null) {
                return "[]";
            } else {
                Node<T> curr = first;
                sb.append(curr.val).append(",");

                while ((curr = curr.next) != last) {
                    sb.append(curr.val).append(",");
                }
                sb.append("]");
                return sb.toString();
            }
        }
    }

    public static void main(String[] args) {
        LinkedList<Integer> ll1 = Lists.newLinkedList(Lists.newArrayList(1, 3, 5, 7, 9));
        LinkedList<Integer> ll2 = Lists.newLinkedList(Lists.newArrayList(2, 4, 6, 8, 10));

        ListNode<Integer> l1 = new ListNode<>(1, 3, 5, 7, 9);
        ListNode<Integer> l2 = new ListNode<>(2, 4, 6, 8, 10);

        System.out.println(l1);
    }

    private static ListNode<Integer> merge(ListNode<Integer> l1, ListNode<Integer> l2) {
        if (l1.isEmpty()) {
            return l2;
        }

        if (l2.isEmpty()) {
            return l1;
        }

//        Integer i1 = l1.pollFirst();
//        Integer i2 = l2.pollFirst();
//
//        if (i1 < i2) {
//            l1.
//        } else {
//
//        }
        return null;
    }
}
