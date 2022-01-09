package org.learn.algorithm.easy;

import java.util.Arrays;

public class 二叉堆 {

    /**
     * * * * * * * 1
     * * * * *  /    \
     * * * *  3        2
     * * *  /  \     /   \
     * * * 6   5    7     8
     * * /  \
     * 9   10
     * <p>
     * 二叉堆：完全二叉树： 根节点要么没有子节点，要么有2个子节点。
     * 一棵深度为k的有n个结点的二叉树，对树中的结点按从上至下、从左到右的顺序进行编号，
     * 如果编号为i（1≤i≤n）的结点与 满二叉树（所有节点都2个子节点）中编号为i的结点在二叉树中的位置相同，则这棵二叉树称为完全二叉树
     * <p>
     * int[] 存储。 index从0开始。 index = 2 * parent + [1,2]
     * . 1     3     2      6       5       7       8       9       10
     * a[0]  a[1]  a[2]   a[3]    a[4]    a[5]    a[6]    a[7]     a[8]
     */

    public static void main(String[] args) {
        int[] array = new int[]{1, 3, 2, 6, 5, 7, 8, 9, 10, 0};
        upAdjust(array);
        System.out.println(Arrays.toString(array));

        array = new int[]{7, 1, 3, 10, 5, 2, 8, 9, 6};
        buildHeap(array);
        System.out.println(Arrays.toString(array));
    }

    private static void buildHeap(int[] array) {
        //从最后一个非叶子节点开始，一次做“下沉”调整
        for (int i = (array.length - 2) / 2; i >= 0; i--) {
            downAdjust(array, i, array.length);
        }
    }

    /**
     * 上浮调整堆
     * * * * * * * * 1
     * * * * * *  /    \
     * * * * *  3        2
     * * * *  /  \     /   \
     * * * * 6    5   7     8
     * * * /  \   /
     * * 9    10 0(insert)
     * <p> 最终调整的结果：
     * * * * * *       0
     * * * * * * *  /    \
     * * * * * *  1        2
     * * * * *  /  \     /   \
     * * * * * 6    3   7     8
     * * * * /  \   /
     * * * 9    10 5
     */
    private static void upAdjust(int[] array) {
        int childIndex = array.length - 1;
        int parentIndex = (childIndex - 1) / 2;
        //temp 保存插入的叶子节点值，用于最后的赋值
        int temp = array[childIndex];

        while (childIndex > 0 && temp < array[parentIndex]) {
            // 无须真正交换，单向赋值即可
            array[childIndex] = array[parentIndex];
            childIndex = parentIndex;
            parentIndex = (parentIndex - 1) / 2;
        }

        array[childIndex] = temp;
    }

    /**
     * 下沉调整堆
     * * * * * *       7
     * * * * * * *  /    \
     * * * * * *  1        3
     * * * * *  /  \     /   \
     * * * * *10    5   2     8
     * * * * /  \   /
     * * * 9    6
     * 从最后一个非叶子开始，也就是节点10开始，如果节点10大于它的左、右子孩子节点中最小的一个，则节点10下沉。
     */
    private static void downAdjust(int[] array, int parentIndex, int length) {
        // temp保存父节点的值，用于最后的赋值
        int temp = array[parentIndex];
        int childIndex = 2 * parentIndex + 1;
        while (childIndex < length) {
            // 如果有右孩子，且右孩子小于做孩子的值，则定位到右孩子
            if (childIndex + 1 < length && array[childIndex] + 1 < array[childIndex]) {
                childIndex++;
            }

            //如果父节点小于任何一个孩子的值，则直接跳出
            if (temp <= array[childIndex]) {
                break;
            }
            //无须真正交换，单向赋值即可
            array[parentIndex] = array[childIndex];
            parentIndex = childIndex;
            childIndex = 2 * childIndex + 1;
        }

        array[parentIndex] = temp;
    }


}
