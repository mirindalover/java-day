#### 跳表

基于链表通过建立索引，来实现链表的二分查找。插入、删除、查找时间复杂度都是O(logn)


### 树

#### 叶子结点

树中最底端的节点，叶子节点没有子节点

#### 二叉树

基本可以通过递归去做

如果要求使用非递归方式，可以使用迭代循环的方式去做

> 使用栈(Stack)来维护节点

```java
	Stack<TreeNode> stack = new Stack();
	while(root != null || !stack.isEmpty()){
		//中、后序遍历就把想要的先放进stack
		//前序遍历先value再放到stack
	}
```


#### 动态规划


本质：暴力穷举

重叠子问题、最优子结构、状态转移方程 是动态规划的三要素

重叠子问题：可以用dp table或者备忘录优化

基础结构：

```java
	//初始化存储计算的结果，避免多次重复计算
	int dp[n];
	//循环穷举，计算结果
	for(i<n,i++){
		//for求所有子问题
		for(子问题枚举){
			//转移方程：dp和其他dp的关系
		}		
	}
```

[斐波那契数列](https://leetcode-cn.com/problems/fei-bo-na-qi-shu-lie-lcof/)

#### 回溯算法


本质：暴力穷举

没有重叠子问题.使用DFS(深度优先)

使用for循环递归调用，所以时间复杂度很高

优化点：可以枝剪。例如路径已经使用、当前选择已经不符合

```java
	backTrack(...){
		for 选择 in 选择列表{
			做选择
			backTrack()
			撤销选择
		}	
	}
```

#### 搜索

分为 DFS(深度优先)、BFS(广度优先)

广度优先
空间复杂度高,每次把后续可能添加到数据中

```java
//核心数据结构
Queue<TreeNode> q = new LinkedList<>();
q.offer(root);

//访问过的数据,不回头
Set<TreeNode> visited;

int depth = 1;
while(!q.isEmpty()){
    int size = q.size();
    for(int i = 0;i<size;i++){
        TreeNode current = q.poll();
        //判断是否满足
        if(current is target){
        	return depth;
        }
        //把后续的可能加入到数据中
        for(后续节点){
        	//通过访问过的过滤一部分
        	if(not in visited){
        		q.offer();
        		visited.add();
        	}
        }
    }
    depth++;
}
return depth;
```

深度优先类似回溯算法

[开密码锁](https://leetcode.cn/problems/zlDJc7/)




