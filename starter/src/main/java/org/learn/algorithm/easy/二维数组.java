package org.learn.algorithm.easy;

/**
 * @description:
 * @create: 2020-07-29 19:51
 */
public class 二维数组 {

    public static void main(String[] args) {
        二维数组 w = new 二维数组();

        int[][] a4x4 = {
                {5, 1, 9, 11},
                {2, 4, 8, 10},
                {13, 3, 6, 7},
                {15, 14, 12, 16}
        };
        //w.rotate(a4x4);
        //w.rotate(new int[][]{{1, 2, 3}, {4, 5, 6}, {7, 8, 9}});

        int[][] aMxN = {
                {0, 1, 2, 0},
                {3, 4, 5, 2},
                {1, 3, 1, 5}
        };
        //System.out.println(aMxN.length + ", " + aMxN[2].length);

        w.setZeroes(new int[][]{{1, 1, 1}, {1, 0, 1}, {1, 1, 1}});
    }

    //1. 给你一幅由 N × N 矩阵表示的图像，其中每个像素的大小为 4 字节。请你设计一种算法，将图像旋转 90 度。
    //不占用额外内存空间能否做到？
    /**
     * 给定 matrix =
     * [
     *   [ 5, 1, 9,11],
     *   [ 2, 4, 8,10],
     *   [13, 3, 6, 7],
     *   [15,14,12,16]
     * ],
     *
     * 原地旋转输入矩阵，使其变为:
     * [
     *   [15,13, 2, 5],
     *   [14, 3, 4, 1],
     *   [12, 6, 8, 9],
     *   [16, 7,10,11]
     * ]
     *
     *      0,0 -> 0,3  0,1 -> 1,3  0,2 -> 2,3  0,3 -> 3,3
     *      1,0 -> 0,2  1,1 -> 1,2  1,2 -> 2,2  1,3 -> 3,2
     *      2,0 -> 0,1  2,1 -> 1,1  2,2 -> 2,1  2,3 -> 3,1
     *      3,0 -> 0,0  3,1 -> 1,0  3,2 -> 2,0  3,3 -> 3,0
     * */

    /**
     * 5  1  9 11
     * 2  4  8 10
     * 13  3  6  7
     * 15 14 12 16
     * <p>
     * 先将其通过水平轴翻转得到：
     * 5  1  9 11                 15 14 12 16
     * 2  4  8 10                 13  3  6  7
     * ------------   =水平翻转=>   ------------
     * 13  3  6  7                  2  4  8 10
     * 15 14 12 16                  5  1  9 11
     * <p>
     * 再根据主对角线 \backslash\ 翻转得到：
     * 15 14 12 16                   15 13  2  5
     * 13  3  6  7   =主对角线翻转=>   14  3  4  1
     * 2  4  8 10                   12  6  8  9
     * 5  1  9 11                   16  7 10 11
     */
    public void rotate(int[][] matrix) {
        //1. 水平翻转  2.左起始位置对角线翻转

        // 0,0 -> 3,0  0,1 -> 3,1  0,2 -> 3,2 ...
        // 1,0 -> 2,0  1,1 -> 2,1  ...
        int length = matrix.length;
        //  3/2 ==> 1
        int half = length / 2;
        for (int i = 0; i < half; i++) {
            for (int j = 0; j < length; j++) {
                int pos = length - 1 - i;
                int temp = matrix[pos][j];
                matrix[pos][j] = matrix[i][j];
                matrix[i][j] = temp;
            }
        }

        // 1,0 -> 0,1
        // 2,0 -> 0,2  2,1 -> 1,2
        // 3,0 -> 0,3  3,1 -> 1,3  3,2 -> 2,3
        // 左边第1行不用换了
        for (int i = 1; i < length; i++) {
            for (int j = 0; j < i; j++) {
                int temp = matrix[j][i];
                matrix[j][i] = matrix[i][j];
                matrix[i][j] = temp;
            }
        }
    }

    //2.编写一种算法，若M × N矩阵中某个元素为0，则将其所在的行与列清零。
    /**
     * 输入：
     * [
     *   [0,1,2,0],
     *   [3,4,5,2],
     *   [1,3,1,5]
     * ]
     * 输出：
     * [
     *   [0,0,0,0],
     *   [0,4,5,0],
     *   [0,3,1,0]
     * ]
     * */
    public void setZeroes(int[][] matrix) {
        // M x N
        int length = matrix[0].length;//M
        int width = matrix.length;//N
        java.util.Set<Integer> row = new java.util.HashSet<>();
        java.util.Set<Integer> col = new java.util.HashSet<>();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < length; j++) {
                if (matrix[i][j] == 0) {
                    row.add(i);
                    col.add(j);
                }
            }
        }

        for (Integer i : row) {
            for (int j = 0; j < length; j++) {
                matrix[i][j] = 0;
            }
        }

        for (Integer i : col) {
            for (int j = 0; j < width; j++) {
                matrix[j][i] = 0;
            }
        }
    }


    //3.对角线遍历
    //给定一个含有 M x N 个元素的矩阵（M 行，N 列），请以对角线遍历的顺序返回这个矩阵中的所有元素
    /**
     *  ↑ 1   2   3
     *  ↓ 4   5   6
     *  ↑ 7   8   9
     *    10  11  12
     *    13  14  15
     *
     *    0,0               1
     *    0,1  1,0          2,4
     *    2,0  1,1  0,2     7,5,3
     *    1,2  2,1  3,0     6,8,10
     *    4,0  3,1  2,2     13,11,9
     *    3,2  4,1          12,14
     *    4,2               15
     *
     *===================================
     *
     *    1   2   3   4
     *    5   6   7   8
     *    9   10  11  12
     *
     *    0,0               1
     *    0,1  1,0          2,5
     *    2,0  1,1  0,2     9,6,3
     *    0,3  1,2  2,1     4,7,10
     *    2,2  1,3          11,8
     *    2,3               12
     * */
    public int[] findDiagonalOrder(int[][] matrix) {


        return null;
    }
}