package org.learn.algorithm.easy;

/*
  @description:  给定一个链表，两两交换其中相邻的节点，并返回交换后的链表。
  @create: 2020-11-09 15:05
 */
public class 交换链表相邻的节点 {

    public static void main(String[] args) {
        ListNode node = ListNode.create(1, 2, 3, 5);
        System.out.println(node);
    }

    // 1 -> 2 -> 3 -> 4
    // 1 ,  2 -> 3 -> 4
    private ListNode reverse(ListNode head) {
        if (head == null || head.next == null) {
            return head;
        }

        ListNode newNode = reverse(head.next);


        return null;
    }

    //Definition for singly-linked list.
    public static class ListNode {
        int val;
        ListNode next;

        ListNode(int val) {
            this.val = val;
        }

        ListNode(int val, ListNode next) {
            this.val = val;
            this.next = next;
        }

        public static ListNode create(int... values) {
            if (values != null && values.length > 0) {
                ListNode head = new ListNode(values[0]);
                ListNode cursor = head;
                for (int i = 1; i < values.length; i++) {
                    ListNode curr = new ListNode(values[i]);
                    cursor.next = curr;
                    cursor = curr;
                }
                return head;
            }

            return null;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder("[");
            ListNode curr = this;
            if (curr == null) {
                return "[]";
            } else {
                sb.append(curr.val);

                while ((curr = curr.next) != null) {
                    sb.append(",").append(curr.val);
                }
                sb.append("]");
                return sb.toString();
            }
        }
    }

    // 1-2-3-4 ==> 2-1-4-3
    class Solution {
        public ListNode swapPairs(ListNode head) {
            int index = 1;
            ListNode current;
            while ((current = head.next) != null) {
                index++;
                if (index % 2 == 0) {
                    ListNode next = current.next;

                }
            }

            return null;
        }
    }

}























