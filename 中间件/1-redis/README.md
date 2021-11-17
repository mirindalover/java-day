### Redis

#### Redis支持的数据结构

String、List、Set、Sorted Set、Hash、BitMap

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

#### 跳跃表

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

[跳跃表结构](https://github.com/mirindalover/java-day/edit/master/%E4%B8%AD%E9%97%B4%E4%BB%B6/1-redis/resource/skiplist.png)