##### 如何实现

```java
PROBE = UNSAFE.objectFieldOffset
    (tk.getDeclaredField("threadLocalRandomProbe"));
```

使用UNSAFE获取对象属性在对象中的偏移量

通过UNSAFE的方法可以实现CAS操作

##### @sun.misc.Contended

防止伪共享(系统加载数据时加载整个缓存行(cpu和RAM交互的最小单位，64byte)，如果使用了volatile保持可见，缓存行中任意一个变量改变都会导致重新相关线程停止然后重新加载数据)

Contended可以让JVM把注解的字段和其他字段放到不同的缓存行中