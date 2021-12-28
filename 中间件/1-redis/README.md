### Redis

#### Redis支持的数据结构

String、List、Set、Sorted Set、Hash、BitMap

#### Zset 是如何实现的

Object的encoding可以是：ziplist、skiplist

1、ziplist：元素个数小于128个；所有元素长度小于64字节

2、skiplist：字典+跳表。字典的键保存元素的值，字典的值则保存元素的分值；跳跃表节点的 object 属性保存元素的值，跳跃表节点的 score 属性保存元素的分值。

#### AOF、RDB的优缺点

> AOF优点：更好保护数据不丢失、磁盘性能开销小、适合灾难性误删恢复
>
> AOF缺点：恢复慢、占用硬盘大、需要重写

> RDB优点：适合做冷备、恢复时间快
>
> RDB缺点：全量备份时间长，丢失数据多、fork子进程时，如果文件特别大，可能会导致暂停服务几毫秒、甚至几秒

#### Redis的缓存雪崩、缓存击穿、缓存穿透

##### 缓存雪崩

> 大量的key设置了相同的过期时间，导致缓存在同一时间失效，造成瞬时DB请求量大，压力骤增，引起雪崩

> 解决：
>
> 1、设置过期时间加上随机值，使key过期时间分布开
>
> 2、设置队列、熔断等减少瞬时db查询太多的情况

##### 缓存击穿

> 一个热点key，访问量很大，失效的瞬间大量请求数据库，像凿开了一个洞

> 解决：
>
> 1、热点缓存永不过期
>
> 2、加上互斥锁(去db查询数据时，先使用redis的setnx设置去db取数的操作，能设置成功才取，否则重试get缓存方法)

##### 缓存穿透

> 访问不存在的key时，请求会穿透到DB，流量大时DB就打挂了

> 解决：
>
> 1、接口层增加校验，过滤一些不合理的参数
>
> 2、key未在DB查询到值，空值也写进缓存，需要设置一个较短的过期时间
>
> 3、采用布隆过滤器，不存在的key直接过滤

引擎项目思考：

1、采用队列来访问引擎减少雪崩问题

2、空数据和报错查询缓存5min的报错时长，防止缓存穿透

3、开始设置了0点缓存过期，改为懒过期，通过mq来保存是否有新数据，访问相应的库时根据是否有新数据来查询还是使用缓存

#### Redis的单线程模型

> Redis对网络IO和数据读写采用一个线程，为了避免多线程带来的并发问题
>
> 同时使用多路复用的IO模型(select/epoll机制)，单线程监听多个套接字，收到请求后放到事件队列排队处理

Redis6.0可以选择使用多线程处理客户端的读写数据，但是命令的操作还是单线程的

#### Redis单线程为什么这么快

单线程指的是他的请求处理使用单现场

1. 纯内存操作

2. Redis是KV数据库,内存通过哈希表存放key,查找是O(1),且数据类型比较丰富且高效

3. 采用IO多路复用

4. 非密集型任务,不需要很多计算:Redis的瓶颈时内存和网络带宽

5. 可以使用集群来解决利用多核CPU

6. 多线程优化

   > 4.0引入来解决释放大内存数据导致redis阻塞
   >
   > 6.0引入多线程来解析数据协议,数据请求处理仍然是单线程

#### Redis的耗时操作(性能瓶颈)

1、操作bigkey：分配内存时消耗更多事件，删除时也消耗长(4.0推出azyfree-lazy-expire 放到异步线程中执行删除操作)，主动删除可以使用UNLINK命令

2、大量key集中过期：过期机制是在主线程执行，大量key同时过期会导致耗时长

3、淘汰机制：淘汰机制也是在主线程执行，redis内存上限后，每次写入都会进行淘汰key，导致耗时

4、AOF开启always：每次写入都刷到磁盘

5、主从全量同步生成RDB：fork一瞬间会阻塞整个线程

- Redis怎么做分布式锁

#### Redis的过期删除策略

> 惰性删除：主键被访问时，发现失效，删除

> 定期删除：定期在设置了失效时间的主键中取出一定数量随机键进行检查，删除过期键

> 从库不会过期key，只当主库删除、过期key时同步给从库删除命令。对于从库提供读功能时，Redis3.2以下会返回值，3.2以后会判断键是否过期，但是仍不会删除

#### Redis实现分布式锁

三要素：安全(互斥)、死锁释放、故障容错

1. 使用 SET xxx NX PX 30000,NX只有没有锁才设置,PN设置超时时间

   > 设置超时时间需要考虑,需要保证锁真正使用完毕了.
   >
   > 可以使用守护线程,循环检查失效时间,自动续期

2. 主动释放.释放时，不能直接del。而要先比较再del(防止超时主动释放导致删除其他进程正在用的锁).

   使用lua脚本实现,lua是原子性

   ```lua
   if redis.call('get',KEYS[1]) == VAL[1] 
   then
   return redis.call('del',KEYS[1]) 
   else
   return 0 
   end
   ```

3. 获取锁失败重试时等待一段时间，且大于正常获取锁的时间。可以防止竞争资源时发生死锁

3. 宕机恢复时,锁超时时间内不能使用,保证不让其他客户端获取锁

官网方案：RedLock 方案：使用多个redis实例来获取锁，当有n/2+1个实例获取锁成功，才表示获取锁成功

带来的不同操作：

1. 释放锁时：需要给所有的节点发送释放请求
2. 故障恢复：宕机后重启的实例在锁超时范围内不可用即可，保证了不会有其他客户端获取到锁

#### Redis做消息队列

使用5.0后版本的Streams类型

1. 支持阻塞式拉消息
2. 支持发布订阅
3. 支持消息id拉取,保证消息不丢失
4. 数据会写入RDB、AOF,保证宕机恢复

缺点:

1. redis是内存操作,stream堆积时,会有内存压力(可以通过设置队列长度,但同时不保证消息不丢失)







#### 数据库

```c
typedef struct redisDb {
    dict *dict;                 /* The keyspace for this DB */
    dict *expires;              /* Timeout of keys with a timeout set */
    dict *blocking_keys;        /* Keys with clients waiting for data (BLPOP)*/
    dict *ready_keys;           /* Blocked keys that received a PUSH */
    dict *watched_keys;         /* WATCHED keys for MULTI/EXEC CAS */
    int id;                     /* Database ID */
    long long avg_ttl;          /* Average TTL, just for stats */
    unsigned long expires_cursor; /* Cursor of the active expire cycle. */
    list *defrag_later;         /* List of key names to attempt to defrag one by one, gradually. */
} redisDb;
```

dict保存键值对，expires保存键和过期时间，键使用同一个对象，不会造成内存消耗

##### 过期键的删除策略

> 惰性删除：主键被访问时，发现失效，删除

> 定期删除：定期在设置了失效时间的主键中取出一定数量随机键进行检查，删除过期键

##### 持久化

###### RDB

快照方式。fork子进程。把某个时间点的全量Redis数据保存到一个压缩的二进制文件中

​	SAVE、BGSAVE可以制动触发保存

###### AOF

写命令追加到AOF文本日志末尾

只有在关闭 AOF 功能的情况下，才会使用 RDB 还原数据，否则优先使用 AOF 文件来还原数据

AOF重写：一拷贝、两处日志——重写是主线程fork一个子进程，fork会吧内存也拷贝一份；此时新的redis操作日志会同时追加到原来的AOF和重写的AOF中，重写完成后吧日志追加到后面。

恢复时，虚拟一个客户端根据AOF文件执行命令来恢复

> AOF采用是写后日志，而mysql数据库采用写前日志。因为mysql等有语法分析等模块，redis执行成功后写日志，保证了命令的正确性

4.0可以混合使用RDB和AOF方法，即内存快照使用一点频率执行，2次快照之间使用AOF记录命令操作



#### 集群-Redis sentinel

##### 部署方式

单节点部署；master-slave部署：主库写，slave读取，master宕机后需要手动吧slave提升成master；master-slave+哨兵部署，master宕机后自动切换主从

本质还是单机redis,Redis sentinel只是个HA架构(高可用)

##### 主从同步

> replication_buffer：有多个，每个客户端一个，用于数据交互。在主从增量同步时，从库的buffer叫replication_buffer，用于同步主库的写命令

> repl_backlog_buffer：只有一个，用于从库断开后找到主从差异数据而设计的环形缓冲区(尽量大一些,避免覆盖后需要全量同步),通过offset寻找增量命令

1、当从库启动时，会发送命令给主库，开始全量数据同步。主库通过bgsave生成RDB文件同步给从库，同时主库的写操作会加到replication_buffer缓存区。RDB传递给从库，吧replication_buffer数据给到从库，从库再执行这些操作

2、同步完后主从库开始常规同步阶段：基于长链接的命令复制

3、长链接断开后重连时会进行增量更新：使用repl_backlog_buffer寻找增量命令后同步。如果断开时间太长，导致offset被覆盖则需要重新进行全量数据同步

##### 哨兵机制

> 哨兵作用：监控、选主、通知
>
> 哨兵集群通过pub/sub模式来 发现其他哨兵实例、客户端订阅哨兵消息
>
> 哨兵集群通过info命令在主库获取从库的信息

- 主库客观下线

  哨兵发现主库主观下线后，给其他哨兵发送命令获取其他哨兵与主库的连接情况，当达到quorum配置设定阈值，可判断为主库客观下线

- 哨兵投票机制

  1、哨兵实例在自己判定主库下线时，才会给自己投票。否则投票只会给第一个来要票的请求，其后都投N

  2、如果多个哨兵同时发现主库下线，导致选举失败，会触发新一轮投票(触发是超时后触发)

  3、成为Leader必要条件：获得半数以上票、票数达到配置的quorum阈值

  tips：哨兵主从库检查是用一个定时器来完成，一般100ms执行一次，且周期会加上随机时间偏移，很少会多个哨兵发现主库下线同时选举Leader

#### 集群-Redis Cluster

解决单点数据量大，备份、写入产生的性能瓶颈

Redis Cluster属于无中心化的集群，每个客户端直接请求节点(每个服务端节点保存了所有节点的槽信息)

##### 虚拟槽分区

槽一共16384个.key通过hash取余后分配到对应的槽上。

优点：解耦数据和节点的关系，节点扩容、缩容时方便

##### 槽迁移重定向

ASK：集群正在进行slot数据迁移，只是临时重定向，此时可能会请求2次才能知道结果

MOVED：槽已经迁移完成，客户端可以更新slots缓存

##### 故障

https://www.cnblogs.com/crazymakercircle/p/14282108.html



#### 数据结构

| API数据结构 | 限制              | 底层数据结构                          |
| ----------- | ----------------- | ------------------------------------- |
| string      | 512 MB            | SDS                                   |
| list        | 最大长度 2^32−1   | quicklist                             |
| set         | 最大容量 2^32−1   | - intset（小整数集） - dict           |
| sort set    | 最大容量 2^32−1   | - ziplist（小集合） - dict + skiplist |
| hash        | 最大KV容量 2^32−1 | - ziplist（小集合） - dict            |
| bitmap      | 512 MB            | SDS                                   |

####  SDS(simple dynamic string)

 Redis 3.0源码

 ```c
struct sdshdr {
    unsigned int len;
    unsigned int free;
    char buf[];
};
 ```

Redis 3.2源码

```c
struct __attribute__ ((__packed__)) sdshdr8 {
    uint8_t len; /* used */
    uint8_t alloc; /* 总共可用的字符空间 */
    unsigned char flags; /* 3 lsb of type, 5 unused bits */
    char buf[];
};
```

3.0后使用sdshdr5 、sdshdr8 、sdshdr16 、sdshdr32 、sdshdr64 多个string来表示。根据string大小使用uint8_t这种固定长度的的数据类型来节省内存

##### SDS减少动态分配内存

1、空间预分配：不超过1MB(1024*1024)时，多申请100%的空间，超过1MB，每次扩容多申请1MB空间

2、惰性空间释放：缩短字符时，不改变释放多余的空间

##### SDS的优势

1、redis的STRLEN函数直接取len变量即可

2、沿用c的字符结构(以'\0'空结尾)，可以使用c中string.h库中的函数

3、可以扩容。杜绝了缓冲区溢问题

4、可以保存文本或二进制等数据(len变量的存在，取值时不根据'\0'空结尾。二进制有'\0'所以不能用c保存)

#### Hash

```c
typedef struct dictht {
    dictEntry **table;
    unsigned long size;
    unsigned long sizemask;
    unsigned long used;
} dictht;

typedef struct dict {
    dictType *type;
    void *privdata;
    dictht ht[2];/*2个table，主要原因是方便rehash*/
    long rehashidx; /* -1时表示没进行rehash */
    int16_t pauserehash; /* If >0 rehashing is paused (<0 indicates coding error) */
} dict;
typedef struct dictEntry {
    void *key;
    union {
        void *val;
        uint64_t u64;
        int64_t s64;
        double d;
    } v;
    struct dictEntry *next;/*链表*/
} dictEntry;
```

和java的hashmap类似，使用拉链发(数组+链表)存储数据和解决hash冲突

##### 扩容、缩容机制

```c
static int _dictExpandIfNeeded(dict *d)
{
    if (d->ht[0].used >= d->ht[0].size &&
        (dict_can_resize ||
         d->ht[0].used/d->ht[0].size > dict_force_resize_ratio))
    {
        return dictExpand(d, d->ht[0].used*2);
    }
    return DICT_OK;
}
```

```c
int htNeedsResize(dict *dict) {/*代码3.0前在redis.c。后在server.c*/
    long long size, used;

    size = dictSlots(dict);
    used = dictSize(dict);
    return (size > DICT_HT_INITIAL_SIZE &&
            (used*100/size < HASHTABLE_MIN_FILL));
}
```

没有执行BGSAVE和BGREWRITEAOF指令(持久化的时候，需要fork操作，这个时候不会分配内存)的情况下，hash表的负载因子大于等于1的时候进行扩容。
正在执行BGSAVE和BGREWRITEAOF指令的情况下，hash表的负载因子大于等于5的时候进行扩容。
负载因子小于0.1的时候，Redis自动开始对Hash表进行缩容操作。

##### 渐进式rehash

避免一次对所有key进行重hash。而分散到正删改查中，对小部分重hash(规定时间循环，每次100;或者每次执行一次rehash节点)

```c
int dictRehashMilliseconds(dict *d, int ms) {
    long long start = timeInMilliseconds();
    int rehashes = 0;

    while(dictRehash(d,100)) {
        rehashes += 100;
        if (timeInMilliseconds()-start > ms) break;
    }
    return rehashes;
}
static void _dictRehashStep(dict *d) {
    if (d->iterators == 0) dictRehash(d,1);
}
```

- rehashidx记录对应需要重hash的index
- rehash中时，取值先找ht[0],再找ht[1]
- put时直接放到ht[1]
- rehash结束，交换ht[1]赋值给ht[0]，释放原来ht[0]的空间

#### quickList

quicklist 是由 ziplist 为节点组成的双向链表

能维持数据项先后顺序的列表

#### 跳跃表

一种有序数据结构，维持多个指向其他节点的指针

```c
typedef struct zskiplistNode {
    sds ele;
    double score;
    struct zskiplistNode *backward;
    struct zskiplistLevel {
        struct zskiplistNode *forward;
        unsigned long span;
    } level[];
} zskiplistNode;

typedef struct zskiplist {
    struct zskiplistNode *header, *tail;
    unsigned long length;
    int level;
} zskiplist;
```

score用于排序；level[]是跳跃表的精髓:程序根据幂次定律(越大的数出现的概率越小)随机生成一个介于1和32之间的值作为level数组的大小

支持平均O(logN)、最坏O(N)复杂度的节点查找

![跳跃表结构](https://github.com/mirindalover/java-day/blob/master/%E4%B8%AD%E9%97%B4%E4%BB%B6/1-redis/resource/skipList.png)

#### 压缩列表

压缩列表(ziplist)本质上就是一个字节数组，是Redis为了节约内存而设计的一种线性数据结构，可以包含任意多个元素，每个元素可以是一个字节数组或一个整数

| 0f 00 00 00 | 0c 00 00 00 | 02 00 | 00 f3 | 02 f6 | ff   |
| ----------- | :---------- | ----- | ----- | ----- | ---- |
| zlbytes     | zltail      | zllen | "2"   | "5"   | end  |

- 1、**zlbytes**：压缩列表的字节长度，占4个字节，因此压缩列表最长(2^32)-1字节；
- 2、**zltail**：压缩列表尾元素相对于压缩列表起始地址的偏移量，占4个字节；
- 3、**zllen**：压缩列表的元素数目，占两个字节；那么当压缩列表的元素数目超过(2^16)-1需要遍历整个压缩列表才能获取到元素数目；
- 4、**entryX**：压缩列表存储的若干个元素，可以为字节数组或者整数
- 5、**zlend**：压缩列表的结尾，占一个字节，恒为0xFF

##### entry

组成

- previous_entry_length：前元素的字节长度。占1个或者5个字节
- encoding：content的元素的编码
- content

[encoding参考](https://segmentfault.com/a/1190000017328042)

例子中使用的是最后一种 0001之间的数1101。真正结果需要-1

- 缺点

> ziplist很大时，每次插入修改需要realloc(重新分配空间),可能会导致内存拷贝
>
> ziplist数据大时，查找指定的数据性能很低，因为ziplist是遍历查找
>
> 连锁更新：由于previous_entry_length使用1或者5个字节，导致插入或者删除元素时，同时元素长度都在临界点(254--一个字节的长度)。会导致所有的元素进行连锁的重分配空间

#### 整数集合

内存连续的整数，且有序

```c
typedef struct intset {
    uint32_t encoding;
    uint32_t length;
    int8_t contents[];
} intset;
```

encoding：决定了contents数组中所有整数的类型，值可为INTSET_ENC_INT16、INTSET_ENC_INT32、INTSET_ENC_INT64

##### 升级

插入新元素时，类型需要升级。会吧所有的元素进行类型转换、时间复杂度为O(n)

#### 对象

```c
typedef struct redisObject {
    unsigned type:4;
    unsigned encoding:4;
    unsigned lru:LRU_BITS;
    int refcount;
    void *ptr;
} robj;
```

1、type类型：字符串、列表、哈希、集合、有序集合对象(ZSet)

2、encoding：ptr对应的数据结构编码

3、refcount：引用计数。引用为0时，回收改对象

> 对象没有循环引用的问题，，因为变量中不会再引用对象

- 字符串：int、raw、embstr
- 哈希：ziplist、hashtable
- 列表：ziplist、linkedlist
- 集合：intset、hashtable
- 有序集合：ziplist、skiplist

