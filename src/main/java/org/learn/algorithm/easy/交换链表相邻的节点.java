package org.learn.algorithm.easy;

/*
  @description:  给定一个链表，两两交换其中相邻的节点，并返回交换后的链表。
  @create: 2020-11-09 15:05
 */
public class 交换链表相邻的节点 {

    //Definition for singly-linked list.
    public class ListNode {
        int val;
        ListNode next;

        ListNode() {
        }

        ListNode(int val) {
            this.val = val;
        }

        ListNode(int val, ListNode next) {
            this.val = val;
            this.next = next;
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























