[题目](https://leetcode-cn.com/problems/cong-shang-dao-xia-da-yin-er-cha-shu-iii-lcof/)

请实现一个函数按照之字形顺序打印二叉树，即第一行按照从左到右的顺序打印，第二层按照从右到左的顺序打印，第三行再按照从左到右的顺序打印，其他行以此类推。

 

例如:
给定二叉树: [3,9,20,null,null,15,7],

    3
   / \
  9  20
    /  \
   15   7
返回其层次遍历结果：

[
  [3],
  [20,9],
  [15,7]
]
 

提示：

节点总数 <= 1000


思路:于上题相同,根据num的奇偶来决定list添加到头部还是尾部

```java
class Solution {
    Map<Integer, List<Integer>> result;

    public List<List<Integer>> levelOrder(TreeNode root) {
        result = new LinkedHashMap<>();
        addNode(root, 0);
        return new ArrayList<>(result.values());
    }

    /**
     * @param num 当前树的层数
     */
    private void addNode(TreeNode root, int num) {
        if (root == null) {
            return;
        }
        List<Integer> list = result.computeIfAbsent(num, k -> new ArrayList<>());
        //中序遍历
        if ((num & 1) == 1) {
            list.add(0, root.val);
        } else {
            list.add(root.val);
        }
        num++;
        addNode(root.left, num);
        addNode(root.right, num);
    }
}
```

```java
 public List<List<Integer>> levelOrder(TreeNode root) {
    Queue<TreeNode> queue = new LinkedList<>();
    List<List<Integer>> result = new ArrayList<>();
    if (root != null) {
        queue.add(root);
    }
    List<Integer> temp;
    while (!queue.isEmpty()) {
        temp = new ArrayList<>();
        int i = queue.size();
        for (int j = 0; j < i; j++) {
            TreeNode poll = queue.poll();
            if ((result.size() & 1) == 0) {
                temp.add(poll.val);
            } else {
                temp.add(0, poll.val);
            }
            if (poll.left != null) {
                queue.add(poll.left);
            }
            if (poll.right != null) {
                queue.add(poll.right);
            }
        }
        result.add(temp);
    }
    return result;
}
```