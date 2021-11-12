### 一致性Hash

> 一致性哈希算法是分布式系统中常用的算法。一致性哈希算法解决了普通余数Hash算法伸缩性差的问题，可以保证在上线、下线服务器的情况下尽量有多的请求命中原来路由到的服务器

#### 一致性Hash环

环的起点是0，终点是2^32 - 1,整数按逆时针分布

对象选择机器时，顺时针找第一个hash对应的机器

java中可以使用TreeMap来表示，利用tailMap和firstKey函数来取对应的机器

```java
// 得到大于该Hash值的所有Map
SortedMap<Integer, String> subMap = virtualNodes.tailMap(hash);
String virtualNode;
if(subMap.isEmpty()){
    //如果没有比该key的hash值大的，则从第一个node开始
    Integer i = virtualNodes.firstKey();
    //返回对应的服务器
    virtualNode = virtualNodes.get(i);
}else{
    //第一个Key就是顺时针过去离node最近的那个结点
    Integer i = subMap.firstKey();
    //返回对应的服务器
    virtualNode = subMap.get(i);
}
```

##### 虚拟节点

当增加一个节点时，只会减轻一个节点的压力。为了实现负载均衡，把机器分成多个虚拟节点，均匀分布在hash环

```java
virtualNodeName = str + "&&VN" + String.valueOf(i)
```

机器的的name使用for循环创建，添加多个虚拟节点

#### Hash函数

hash算法如CRC32_HASH、FNV1_32_HASH、KETAMA_HASH等，其中KETAMA_HASH是默认的MemCache推荐的一致性Hash算法，用别的Hash算法也可以，比如FNV1_32_HASH算法的计算效率就会高一些



参考

[一致性hash java实现](https://blog.csdn.net/suifeng629/article/details/81567777)