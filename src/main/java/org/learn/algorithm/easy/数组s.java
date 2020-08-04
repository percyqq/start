package org.learn.algorithm.easy;


import java.util.Arrays;

/**
 * @description:
 * @create: 2020-07-29 11:16
 */
public class 数组s {

    public static void main(String[] args) {
        数组s w = new 数组s();


        //w.pivotIndex(new int[]{1, 3, 5, 2, 4, 6, 7, 8});
        //w.pivotIndex(new int[]{-1, -1, 0, 1, 1, 0});

        //w.searchInsert(new int[]{1, 3, 5, 6}, 0);

        int[][] wtf = {{1, 3}, {2, 6}, {8, 10}, {15, 18}};
        for (int[] xx : w.merge(wtf)) {
            System.out.println(Arrays.toString(xx));
        }

    }

    //1.寻找数组的中心索引

    /**
     * 给定一个整数类型的数组 nums，请编写一个能够返回数组 “中心索引” 的方法。
     * 我们是这样定义数组 中心索引 的：数组中心索引的左侧所有元素相加的和等于右侧所有元素相加的和。
     * 如果数组不存在中心索引，那么我们应该返回 -1。如果数组有多个中心索引，那么我们应该返回最靠近左边的那一个。
     */
    public int pivotIndex(int[] nums) {
        int totalSum = 0;
        for (int i : nums) {
            totalSum += i;
        }

        int leftSum = 0;
        int length = nums.length;

        // 可以认为左边/右边没有...
        for (int i = 0; i < length; i++) {
            // 左/右，至少从第二个
            int rightSum = totalSum - nums[i] - leftSum;
            System.out.println("middle [" + i + "]: " + nums[i] + " , leftSum : " + leftSum + ", rightSum : " + rightSum);
            if (rightSum == leftSum) {
                System.out.println("find i: " + i);
                return i;
            }

            leftSum += nums[i];
        }
        return -1;
    }


    //2.搜索插入位置

    /**
     * 给定一个排序数组和一个目标值，在数组中找到目标值，并返回其索引。如果目标值不存在于数组中，返回它将会被按顺序插入的位置。
     * 你可以假设数组中无重复元素。
     */
    public int searchInsert(int[] nums, int target) {
        int left = 0, right = nums.length - 1;
        int pos = right + 1;//最后一位

        //注意：注意边界值 [1, 3, 5, 6], 0
        while (left <= right) {
            int middle = (left + right) / 2;
            if (target <= nums[middle]) {
                pos = middle;
                right = middle - 1;
            } else {
                left = middle + 1;
            }
        }
        return pos;
    }


    //3. 合并区间

    /**
     * 给出一个区间的集合，请合并所有重叠的区间。
     * 示例 1:
     * 输入: [[1,3],[2,6],[8,10],[15,18]]
     * 输出: [[1,6],[8,10],[15,18]]
     * 解释: 区间 [1,3] 和 [2,6] 重叠, 将它们合并为 [1,6].
     * <p>
     * 示例 2:
     * 输入: [[1,4],[4,5]]
     * 输出: [[1,5]]
     * 解释: 区间 [1,4] 和 [4,5] 可被视为重叠区间。
     * <p>
     * int[][] wtf = {{1, 3}, {2, 6}, {8, 10}, {15, 18}};
     */
    public int[][] merge(int[][] intervals) {
        //按照区间的左端点排序
        java.util.Arrays.sort(intervals, java.util.Comparator.comparingInt(a -> a[0]));

        // 对于需要解得的集合，可以认为是分散的组合在一起的，
        //  比如 [start, ...] start是从排序好的值开始， 那么 [..., end] end就是交集集合中最大的end。
        int index = 0;
        int[][] answer = new int[intervals.length][2];
        for (int[] arr : intervals) {
            int start = arr[0], end = arr[1];

            // 如果[结果数组]是空的，或者当前区间的起始位置 > 结果数组中最后区间的终止位置，
            // 则不合并，直接将当前区间加入结果数组。
            if (index == 0 || start > answer[index - 1][1]) {
                // start > 上一次集合的end， 无交集
                answer[index] = arr;
                index++;
            } else {
                int lastEnd = answer[index - 1][1];
                int newEnd = Math.max(lastEnd, end);
                answer[index - 1][1] = newEnd;
            }
        }
        return Arrays.copyOf(answer, index);
    }

    public int[][] merge2(int[][] intervals) {
        // 先按照区间起始位置排序
        Arrays.sort(intervals, (v1, v2) -> v1[0] - v2[0]);
        // 遍历区间
        int[][] res = new int[intervals.length][2];
        int idx = -1;
        for (int[] interval : intervals) {
            // 如果结果数组是空的，或者当前区间的起始位置 > 结果数组中最后区间的终止位置，
            // 则不合并，直接将当前区间加入结果数组。
            if (idx == -1 || interval[0] > res[idx][1]) {
                res[++idx] = interval;
            } else {
                // 反之将当前区间合并至结果数组的最后区间
                res[idx][1] = Math.max(res[idx][1], interval[1]);
            }
        }
        return Arrays.copyOf(res, idx + 1);
    }

}