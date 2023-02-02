##### 如何实现

```java
offset = UNSAFE.objectFieldOffset
    (tk.getDeclaredField("threadLocalRandomProbe"));
```

使用UNSAFE获取对象属性在对象中的偏移量

通过UNSAFE的方法可以实现CAS操作

```java
boolean cas = UNSAFE.compareAndSwapLong(this, offset, cmp, val);
```

> 并发包下 AtomicInteger、AtomicLong都是采用CAS的方式解决多线程问题

##### @sun.misc.Contended

1、(ConcurrentHashMap中有使用)

防止伪共享(系统加载数据时加载整个缓存行，无法充分使用缓存行特性的现象叫伪共享)(cpu和RAM交互的最小单位，64byte)
如果使用了volatile保持可见，缓存行中任意一个变量改变都会导致重新相关线程停止然后重新加载数据)

Contended可以让JVM把注解的字段和其他字段放到不同的缓存行中

java中long为8byte。那8个long组成一个缓存行

2、Disruptor(一个高性能java队列)

采用padding的方式：使用其他无用值填充字段

```java
class LhsPadding
{
    protected long p1, p2, p3, p4, p5, p6, p7;
}

class Value extends LhsPadding
{
    protected volatile long value;
}
```