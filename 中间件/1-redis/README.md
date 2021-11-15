### Redis

- Redis为啥快
- Redis支持的数据类型
- Redis的缓存雪崩、缓存击穿、缓存穿透具体是什么原因，解决办法
- Redis怎么做分布式锁
- Redis的单线程模型
- Redis的过期删除策略

#####  SDS(simple dynamic string)

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

4、可以保存文本或二进制等数据(len变量的存在，取值时不根据'\0'空结尾)

