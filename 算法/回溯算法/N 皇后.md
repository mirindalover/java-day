#### [N 皇后](https://leetcode-cn.com/problems/n-queens/)

> n 皇后问题 研究的是如何将 n 个皇后放置在 n×n 的棋盘上，并且使皇后彼此之间不能相互攻击。
>
> 给你一个整数 n ，返回所有不同的 n 皇后问题 的解决方案。
>
> 每一种解法包含一个不同的 n 皇后问题 的棋子放置方案，该方案中 'Q' 和 '.' 分别代表了皇后和空位。
>
>  
>
> 示例 1：
>
>
> 输入：n = 4
> 输出：[[".Q..","...Q","Q...","..Q."],["..Q.","Q...","...Q",".Q.."]]
> 解释：如上图所示，4 皇后问题存在两个不同的解法。
> 示例 2：
>
> 输入：n = 1
> 输出：[["Q"]]
>
>
> 提示：
>
> 1 <= n <= 9
> 皇后彼此不能相互攻击，也就是说：任何两个皇后都不能处于同一条横行、纵行或斜线上。

思路：N皇后问题是典型的回溯算法，使用暴力穷举

注意：放置时需要把行列和斜线位置都置为不可用

```java
class Solution {

    char[] word;
    List<List<String>> result;

    public List<List<String>> solveNQueens(int n) {
        //N皇后问题是典型的回溯算法，使用暴力穷举
        //由于每一行每一列都只能放1个，所以使用1个二位数组表示行列使用情况，按照列递增放置
        //放置Q后应该行、列、对角。可能会互相影响，所以需要使用int来表示是否使用
        int[][] row = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                row[i][j] = 0;
            }
        }
        result = new ArrayList<>();
        word = new char[n];
        for (int i = 0; i < n; i++) {
            word[i] = '.';
        }
        String[] temp = new String[n];
        solveQueen(row, temp, 0);
        return result;
    }

    /**
     * @param row    行数组，true表示此行使用
     * @param temp   放置结果的临时变量
     * @param colNum 当前列
     */
    private void solveQueen(int[][] row, String[] temp, int colNum) {
        if (colNum == row.length) {
            //前面列已经填充完毕
            result.add(new ArrayList<>(Arrays.asList(temp)));
            return;
        }
        for (int i = 0; i < row.length; i++) {
            if (row[i][colNum] > 0) {
                //i行已经不能使用了
                continue;
            }
            //第colNum列放置Q
            temp[i] = createQueen(colNum);
            setUseQ(row, i, colNum);
            solveQueen(row, temp, colNum + 1);
            resetUseQ(row, i, colNum);
        }
    }

    private void resetUseQ(int[][] row, int rowNum, int colNum) {
        //行
        int[] temp = row[rowNum];
        for (int i = 0; i < temp.length; i++) {
            temp[i]--;
        }
        //列
        for (int i = 0; i < row.length; i++) {
            row[i][colNum]--;
        }
        //左上
        for (int i = 1; i < temp.length; i++) {
            if (rowNum - i < 0 || colNum - i < 0) {
                //边界
                break;
            }
            row[rowNum - i][colNum - i]--;
        }
        //左下
        for (int i = 1; i < temp.length; i++) {
            if (rowNum + i >= temp.length || colNum - i < 0) {
                //边界
                break;
            }
            row[rowNum + i][colNum - i]--;
        }
        //右上
        for (int i = 1; i < temp.length; i++) {
            if (rowNum - i < 0 || colNum + i >= temp.length) {
                //边界
                break;
            }
            row[rowNum - i][colNum + i]--;
        }
        //右下
        for (int i = 1; i < temp.length; i++) {
            if (rowNum + i >= temp.length || colNum + i >= temp.length) {
                //边界
                break;
            }
            row[rowNum + i][colNum + i]--;
        }
    }

    /**
     * 设置影响的行列斜线
     *
     * @param row
     * @param rowNum
     * @param colNum
     */
    private void setUseQ(int[][] row, int rowNum, int colNum) {
        //行
        int[] temp = row[rowNum];
        for (int i = 0; i < temp.length; i++) {
            temp[i]++;
        }
        //列
        for (int i = 0; i < row.length; i++) {
            row[i][colNum]++;
        }
        //左上
        for (int i = 1; i < temp.length; i++) {
            if (rowNum - i < 0 || colNum - i < 0) {
                //边界
                break;
            }
            row[rowNum - i][colNum - i]++;
        }
        //左下
        for (int i = 1; i < temp.length; i++) {
            if (rowNum + i >= temp.length || colNum - i < 0) {
                //边界
                break;
            }
            row[rowNum + i][colNum - i]++;
        }
        //右上
        for (int i = 1; i < temp.length; i++) {
            if (rowNum - i < 0 || colNum + i >= temp.length) {
                //边界
                break;
            }
            row[rowNum - i][colNum + i]++;
        }
        //右下
        for (int i = 1; i < temp.length; i++) {
            if (rowNum + i >= temp.length || colNum + i >= temp.length) {
                //边界
                break;
            }
            row[rowNum + i][colNum + i]++;
        }
    }

    private String createQueen(int i) {
        word[i] = 'Q';
        String result = new String(word);
        word[i] = '.';
        return result;
    }
}
```

