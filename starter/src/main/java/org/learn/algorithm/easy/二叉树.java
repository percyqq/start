package org.learn.algorithm.easy;

import com.alibaba.google.common.collect.Lists;
import lombok.Data;

import java.util.LinkedList;
import java.util.Stack;

public class 二叉树 {

    @Data
    private static class TreeNode {
        int data;
        private TreeNode leftChild;
        private TreeNode rightChild;

        public TreeNode(int data) {
            this.data = data;
        }
    }

    private static TreeNode createBinaryTree(LinkedList<Integer> inputList) {
        TreeNode node = null;
        if (inputList == null || inputList.isEmpty()) {
            return null;
        }

        Integer data = inputList.removeFirst();
        // data 为空，则不再进一步递归
        if (data != null) {
            node = new TreeNode(data);
            node.leftChild = createBinaryTree(inputList);
            node.rightChild = createBinaryTree(inputList);
        }
        return node;
    }

    /**
     * * * * * * 3
     * * * *  /    \
     * * *  2        8
     * *  /  \     /   \
     * * 9   10   null  4
     */
    public static void main(String[] args) {
        LinkedList<Integer> inputList = Lists.newLinkedList(Lists.newArrayList(3, 2, 9, null, null, 10, null, null, 8, null, 4));
        TreeNode treeNode = createBinaryTree(inputList);

        System.out.println("前序遍历");
        //preOrderTraverse(treeNode);
        //preOrderTraverseWithStack(treeNode);
        //inOrderTraverse(treeNode);
        //inOrderTraverseWithStack(treeNode);
        System.out.println();
        postOrderTraverse(treeNode);    //9  10  2  4  8  3
        postOrderTraverseWithStack2(treeNode);
    }

    // 后续。 节点 - 右 - 左， 依次入栈
    // stack : 3, 8, 4 , 4.2, 4, 4.1, 8, 8.1, 3, 2, 10
    // head  : 3                                 10,
    private static void postOrderTraverseWithStack1(TreeNode treeNode) {
        System.out.println();
        Stack<TreeNode> stack = new Stack<>();
        Stack<TreeNode> head_right_left = new Stack<>(); //依次存入，头节点 - 右 - 左，出栈时即为所得顺序
        while (treeNode != null || !stack.isEmpty()) {
            while (treeNode != null) {
                stack.push(treeNode);
                head_right_left.push(treeNode);
                treeNode = treeNode.rightChild;
            }
            if (!stack.isEmpty()) {
                treeNode = stack.pop();
                treeNode = treeNode.leftChild;
            }
        }
        while (!head_right_left.isEmpty()) {
            treeNode = head_right_left.pop();
            System.out.print(treeNode.data + "  ");
        }
    }


    //3 2 9 9.1  9 9.2  2 10 10.1 3 8 8.1 8
    //9  9
    //2     2
    //3     3
    private static void postOrderTraverseWithStack2(TreeNode treeNode) {
        System.out.println();
        Stack<TreeNode> head_right_left = new Stack<>(); //依次存入，头节点 - 右 - 左，出栈时即为所得顺序
        Stack<TreeNode> head_left_right = new Stack<>(); //依次存入，头节点 - 左 - 右
        if (treeNode != null) {
            head_left_right.push(treeNode);
        }

        while (!head_left_right.isEmpty()) {
            TreeNode head = head_left_right.pop();
            head_right_left.push(head);
            if (head.leftChild != null) {
                head_left_right.add(head.leftChild);
            }
            if (head.rightChild != null) {
                head_left_right.add(head.rightChild);
            }
        }

        while (!head_right_left.isEmpty()) {
            treeNode = head_right_left.pop();
            System.out.print(treeNode.data + "  ");
        }
    }

    //前序： 根在前。 3  2  9  10  8  4
    private static void preOrderTraverseWithStack(TreeNode treeNode) {
        System.out.println();
        Stack<TreeNode> stack = new Stack<>();
        while (treeNode != null || !stack.isEmpty()) {
            while (treeNode != null) {
                System.out.print(treeNode.data + "  ");
                stack.push(treeNode);
                treeNode = treeNode.leftChild;
            }
            if (!stack.isEmpty()) {
                treeNode = stack.pop();
                treeNode = treeNode.rightChild;
            }
        }
    }

    /**
     * * * * * * 3
     * * * *  /    \
     * * *  2        8
     * *  /  \     /   \
     * * 9   10   null  4
     */
    // 中序，根在中间。 9, 2 , 10, 3, 4, 8
    private static void inOrderTraverseWithStack(TreeNode treeNode) {
        System.out.println();
        Stack<TreeNode> stack = new Stack<>();
        while (treeNode != null || !stack.isEmpty()) {
            while (treeNode != null) {
                stack.push(treeNode);
                treeNode = treeNode.leftChild;
            }
            if (!stack.isEmpty()) {
                treeNode = stack.pop();
                System.out.print(treeNode.data + "  ");
                treeNode = treeNode.rightChild;
            }
        }
    }


    // 前序
    private static void preOrderTraverse(TreeNode treeNode) {
        if (treeNode == null) {
            return;
        }
        System.out.print(treeNode.data + "  ");
        preOrderTraverse(treeNode.leftChild);
        preOrderTraverse(treeNode.rightChild);
    }

    //中序
    private static void inOrderTraverse(TreeNode treeNode) {
        if (treeNode == null) {
            return;
        }
        inOrderTraverse(treeNode.leftChild);
        System.out.print(treeNode.data + "  ");
        inOrderTraverse(treeNode.rightChild);
    }

    //后续
    private static void postOrderTraverse(TreeNode treeNode) {
        if (treeNode == null) {
            return;
        }
        postOrderTraverse(treeNode.leftChild);
        postOrderTraverse(treeNode.rightChild);
        System.out.print(treeNode.data + "  ");
    }

}
