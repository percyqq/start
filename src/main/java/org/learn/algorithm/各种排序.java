package org.learn.algorithm;

import java.util.Arrays;

/**
 * @create: 2020-08-05 19:18
 * @jdk 13
 */
public class 各种排序 {

    public static void main(String[] args) {
        int[] wtf = new int[]{3, 11, 7, 5, 32, 1, 8, 2, 43, 6, 4,};

        //bubbleSort(wtf);
        selectionSort(wtf);

        System.out.println(Arrays.toString(wtf));
    }


    //冒泡
    private static void bubbleSort(int[] arr) {
        int len = arr.length;
        for (var i = 0; i < len - 1; i++) {
            for (var j = 0; j < len - 1 - i; j++) {
                if (arr[j] > arr[j + 1]) {        // 相邻元素两两对比
                    swap(arr, j, j + 1);
                }
            }
        }
    }

    /**
     * 选择排序
     * n个记录的直接选择排序可经过n-1趟直接选择排序得到有序结果。具体算法描述如下：
     * 1. 初始状态：无序区为R[1..n]，有序区为空；
     * 2. 第i趟排序(i=1,2,3…n-1)开始时，当前有序区和无序区分别为R[1..i-1]和R(i..n）。
     * ______该趟排序从当前无序区中-选出关键字最小的记录 R[k]，将它与无序区的第1个记录R交换，
     * ______使R[1..i]和R[i+1..n)分别变为记录个数增加1个的新有序区和记录个数减少1个的新无序区；
     * 3. n-1趟结束，数组有序化了。
     * <p>
     * 表现最稳定的排序算法之一，因为无论什么数据进去都是O(n2)的时间复杂度，所以用到它的时候，数据规模越小越好。
     * ==> 唯一的好处可能就是不占用额外的内存空间了吧。理论上讲，选择排序可能也是平时排序一般人想到的最多的排序方法了吧。
     */
    private static void selectionSort(int[] arr) {
        int len = arr.length, minIndex;
        for (int i = 0; i < len - 1; i++) {
            minIndex = i;
            for (var j = i + 1; j < len; j++) {
                if (arr[j] < arr[minIndex]) {     // 寻找最小的数
                    minIndex = j;                 // 将最小数的索引保存
                }
            }
            swap(arr, i, minIndex);
        }
    }


    /**
     * 插入排序（Insertion-Sort）的算法描述是一种简单直观的排序算法。
     * 它的工作原理是通过构建有序序列，对于未排序数据，在已排序序列中从后向前扫描，找到相应位置并插入。
     * <p>
     * 3.1 算法描述
     * 一般来说，插入排序都采用in-place在数组上实现。具体算法描述如下：
     * 1. 从第一个元素开始，该元素可以认为已经被排序；
     * 2. 取出下一个元素，在已经排序的元素序列中从后向前扫描；
     * 3. 如果该元素（已排序）大于新元素，将该元素移到下一位置；
     * 4. 重复步骤3，直到找到已排序的元素小于或者等于新元素的位置；
     * 5. 将新元素插入到该位置后；
     * 6. 重复步骤2~5。
     * <p>
     * 通常采用in-place排序（即只需用到O(1)的额外空间的排序），因而在从后向前扫描过程中，需要反复把已排序元素逐步向后挪位，为最新元素提供插入空间。
     */
    private static void insertionSort(int[] arr) {
        int len = arr.length;
        int preIndex, current;
        for (int i = 1; i < len; i++) {
            preIndex = i - 1;
            current = arr[i];
            while (preIndex >= 0 && arr[preIndex] > current) {
                arr[preIndex + 1] = arr[preIndex];
                preIndex--;
            }
            arr[preIndex + 1] = current;
        }
    }

    /**
     * 归并排序是建立在归并操作上的一种有效的排序算法。该算法是采用分治法（Divide and Conquer）的一个非常典型的应用。
     *  将已有序的子序列合并，得到完全有序的序列；即先使每个子序列有序，再使子序列段间有序。若将两个有序表合并成一个有序表，称为2-路归并。
     * */


    private static void swap(int[] arr, int i, int j) {
        var temp = arr[i];        // 元素交换
        arr[i] = arr[j];
        arr[j] = temp;
    }

}
