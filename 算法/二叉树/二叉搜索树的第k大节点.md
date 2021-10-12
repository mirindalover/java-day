(题目)[https://leetcode-cn.com/problems/er-cha-sou-suo-shu-de-di-kda-jie-dian-lcof/]

 

示例 1:

输入: root = [3,1,4,null,2], k = 1
   3
  / \
 1   4
  \
   2
输出: 4
示例 2:

输入: root = [5,3,6,2,4,null,null,1], k = 3
       5
      / \
     3   6
    / \
   2   4
  /
 1
输出: 4
 

限制：

1 ≤ k ≤ 二叉搜索树元素个数

二叉搜素树：任何节点中的值都会大于或者等于其左子树中存储的值，小于或者等于其右子树中存储的值


```java
class Solution {
    
    int num;

    public int kthLargest(TreeNode root, int k) {
        //后序遍历，找到第k个node
        TreeNode result = loopTree(root, k);
        if (result == null) {
            return -1;
        }
        return result.val;
    }

    private TreeNode loopTree(TreeNode root, int k) {
        if (root == null) {
            return null;
        }
        TreeNode right = loopTree(root.right, k);
        if (right != null) {
            return right;
        }
        num++;
        if (num == k) {
            return root;
        }
        return loopTree(root.left, k);
    }
}
```