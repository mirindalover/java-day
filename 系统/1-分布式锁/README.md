### 分布式锁

#### Redis



#### Zookeeper

1. 客户端 1 和 2 都尝试创建「临时节点」，例如 /lock
2. 假设客户端 1 先到达，则加锁成功，客户端 2 加锁失败
3. 客户端 1 操作共享资源
4. 客户端 1 删除 /lock 节点，释放锁

- 锁删除

  客户端异常,Zookeeper长时间收不到客户端的心跳会删除临时节点

优点:

> 不需要考虑锁过期
>
> watch机制,是乐观锁

缺点

> 性能不如redis
>
> 部署运维成本高
>
> 客户端与ZK失联,锁被释放



https://mp.weixin.qq.com/s?__biz=MzAwNDA2OTM1Ng==&mid=2453141835&idx=1&sn=ff0867c9f5ecec9ea8187a21ef7edb2c&chksm=8cf2dbc8bb8552def9bb27fc6302e735eccdd68be5344d6e1d51d244b8e0753d56317cfe8bf2&token=1478279203&lang=zh_CN&scene=21#wechat_redirect



http://kaito-kidd.com/2021/06/08/is-redis-distributed-lock-really-safe/#more