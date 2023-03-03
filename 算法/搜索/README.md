
### 搜索

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

