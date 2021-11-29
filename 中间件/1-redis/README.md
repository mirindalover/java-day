### Redis

#### Redis支持的数据结构

String、List、Set、Sorted Set、Hash、BitMap

#### Zset 是如何实现的

Object的encoding可以是：ziplist、skiplist

1、ziplist：元素个数小于128个；所有元素长度小于64字节

2、skiplist：字典+跳表。字典的键保存元素的值，字典的值则保存元素的分值；跳跃表节点的 object 属性保存元素的值，跳跃表节点的 score 属性保存元素的分值。

- Redis为啥快

#### Redis的缓存雪崩、缓存击穿、缓存穿透

##### 缓存雪崩

> 大量的key设置了相同的过期时间，导致缓存在同一时间失效，造成瞬时DB请求量大，压力骤增，引起雪崩

> 解决：设置过期时间加上随机值，使key过期时间分布开

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

- Redis怎么做分布式锁
- Redis的单线程模型
- Redis的过期删除策略







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

> 惰性删除

> 定期删除：取出一定数量随机键进行检查，删除过期键

##### 持久化

RDB：快照方式。把某个时间点的所有Redis数据保存到一个压缩的二进制文件中

​	SAVE、BGSAVE可以制动触发保存

AOF：

> 只有在关闭 AOF 功能的情况下，才会使用 RDB 还原数据，否则优先使用 AOF 文件来还原数据





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

