[题目](https://leetcode-cn.com/problems/cong-shang-dao-xia-da-yin-er-cha-shu-ii-lcof/)

从上到下按层打印二叉树，同一层的节点按从左到右的顺序打印，每一层打印到一行。

 

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
  [9,20],
  [15,7]
]
 

提示：

节点总数 <= 1000

思路:中序遍历,使用int值记录当前节点的层级,当如到linkedHashMap中,再使用values获取最终结果

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
        list.add(root.val);
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
            temp.add(poll.val);
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