package org.learn.algorithm.easy;

/*
  @description:  给定一个链表，两两交换其中相邻的节点，并返回交换后的链表。
  @create: 2020-11-09 15:05
 */
public class 交换链表相邻的节点 {

    public static void main(String[] args) {
        ListNode node = ListNode.create(1, 2, 3, 4);
        System.out.println(node);

        ListNode r = reverse(node);
        System.out.println(r);
    }


    // 1 -> 2 -> 3 -> 4
    // ① newList = 4 进入最内圈递归，此时 head == 3， 递归完就成了： 4 -> 3
    // ② newList = 2 进入上一层递归，此时 head == 2,  2 -> 4 ->3
    private static ListNode reverse(ListNode head) {
        // 1.递归结束条件
        if (head == null || head.next == null) {
            return head;
        }
        // 递归反转 子链表
        ListNode newList = reverse(head.next);
        // 改变 1，2节点的指向。
        // 通过 head.next获取节点2
        ListNode t1 = head.next;
        // 让 2 的 next 指向 2
        t1.next = head;
        // 1 的 next 指向 null.
        head.next = null;
        // 把调整之后的链表返回。
        return newList;
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























