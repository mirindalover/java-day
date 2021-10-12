#### [n个骰子的点数](https://leetcode-cn.com/problems/nge-tou-zi-de-dian-shu-lcof/)

> 把n个骰子扔在地上，所有骰子朝上一面的点数之和为s。输入n，打印出s的所有可能的值出现的概率。
>
>  
>
> 你需要用一个浮点数数组返回答案，其中第 i 个元素代表这 n 个骰子所能掷出的点数集合中第 i 小的那个的概率。
>
>  
>
> 示例 1:
>
> 输入: 1
> 输出: [0.16667,0.16667,0.16667,0.16667,0.16667,0.16667]
> 示例 2:
>
> 输入: 2
> 输出: [0.02778,0.05556,0.08333,0.11111,0.13889,0.16667,0.13889,0.11111,0.08333,0.05556,0.02778]
>
>
> 限制：
>
> 1 <= n <= 11

动态规划

转移方程:f(n,x)=f(n-1,x-1)/6+f(n-1,x-2)/6...，存在数组越界问题

改为转移方程：f(n-1,x-1)影响f(n,x),f(n,x+1)...f(n,x+5)

```java
class Solution {
     public double[] dicesProbability(int n) {
        //n个筛子出现点数个数6n-n+1=5n+1
        //转移方程：f(n,x)=f(n-1,x-1)/6+f(n-1,x-2)/6...，存在数组越界问题
        //转移方程：f(n-1,x-1)影响f(n,x),f(n,x+1)...f(n,x+5)
        double[] dp = new double[6];
        Arrays.fill(dp, 1.0 / 6);
        double[] temp;
        for (int i = 2; i <= n; i++) {
            //从2开始
            temp = new double[5 * i + 1];
            for (int j = 0; j < dp.length; j++) {
                //遍历f(n-1)
                for (int k = 0; k < 6; k++) {
                    //f(n-1)影响的
                    temp[j + k] += dp[j] / 6;
                }
            }
            dp = temp;
        }
        return dp;
    }
}
```

