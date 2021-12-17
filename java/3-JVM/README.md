#### JVM

配置例子

```shell
-XX:MetaspaceSize=384m -XX:MaxMetaspaceSize=384m -Xms4096m -Xmx6144m -Xmn3584m -Xss512k -XX:SurvivorRatio=4 -XX:CompressedClassSpaceSize=256m -XX:CMSInitiatingOccupancyFraction=60 -XX:+UseCMSInitiatingOccupancyOnly -XX:+UseConcMarkSweepGC -XX:+PrintGCDateStamps -XX:+PrintGCDetails -Xloggc:/export/Logs/xxx/gc.log

```

java8永久代被元空间取代

- 堆

  > 新生代：Eden区、2个Servivor区(防止碎片化带来的性能问题，Eden区gc时会同时把s0复制到s1，保证了内存连续)
  >
  > 老年代：

- 非堆

  > code cache(默认240M)：JIT、JNI编译成的native code

  > Matespace：包括Klass Metaspace、NoKlass Metaspce
  >
  > 
  >
  > Klass Metaspace：class文件在jvm里的运行时数据结构(compressed class space)--64bit机器上使用32bit的原始对象指针，默认1G。在没有开启压缩指针不会有这块内存。比如Xmx>=32G，内存会关闭压缩指针，此时class存到NoKlass Metaspace里面
  >
  > 平均一个Klass大小1k，1G可以打开1oow的Kclass
  >
  > 
  >
  > NoKlass Metaspce：存klass相关的其他内存，比如：method、constantPool、注解
  >
  >  
  
  内存可通过jcmd命令查看

> -Xms：初始化堆内存
>
> -Xmx：最大堆内存
>
> -Xmn：新生代内存
>
> -Xss：线程堆栈内存
>
> -XX:MetaspaceSize、-XX:MaxMetaspaceSize：元数据内存
>
> -XX:CompressedClassSpaceSize：compressed class space内存
>
> -XX:SurvivorRatio：年轻代中Eden区和Servivor区的比例。4表示Servivor:Eden=2:4

使用CMS gc的三个参数

> -XX:+UseConcMarkSweepGC：使用CMS收集器
>
> -XX:CMSInitiatingOccupancyFraction：CMS触发GC的百分比。配合-XX:+UseCMSInitiatingOccupancyOnly配置

#### 垃圾回收

##### 标记算法

引用计数算法:引用+1,引用为0的可以回收.会存在循环引用的问题

可达性分析算法:判断对象引用是否可达,进行垃圾对象的识别

GCROOT

> 从线程栈帧的局部变量
>
> 方法区静态属性
>
> 方法区常量
>
> JNI引用对象

##### 回收算法

> 标记清除:分标记清除2个阶段(不会真正清理,只是标记为空闲)
>
> 复制算法:将内存按比例分为对象和空闲2部分,对象部分用完后吧存活的对象复制到空闲部分,吧原来对象部分清空--适用于对象成活率低,比如新生代
>
> 标记整理算法:标记、整理:移动所有的存活对象,按照内存地址次序依次排列,最后将末端内存地址以后的内存回收
>
> 分代回收:GC:Minor GC(年轻代)、Major GC(老年代)、Full GC
>
> JVM无法为新对象分配内存时会出发Minor GC,比如Eden区满时.老年代指向年轻代也被认为是GC ROOT.Minor GC会stop the world

##### 收集器

![垃圾收集器](https://github.com/mirindalover/java-day/tree/master/java/3-JVM/resource/gc.png)

新生代收集器:Serial(串行)、ParNew、Parallel Scavenge.都采用复制算法

老年代收集器:Serial(标记整理)、Parallel(标记整理)、CMS(标记清除)

G1:适用于新生代和老年代(复制+标记整理)

![回收过程](https://github.com/mirindalover/java-day/tree/master/java/3-JVM/resource/gc_2.png)

串行并行,回收线程工作时都会stop the world

###### 新生代gc

新生代一般都是在满的时候进行gc

- 担保机制

  > 只要老年代的连续空间大于新生代对象的总大小或者历次晋升到老年代的对象的平均大小就进行`MinorGC`，否则`FullGC`

###### CMS

> 针对老年代回收的GC.目的影响用户时间最短.回收的某个阶段,回收线程和用户线程可以并发运行

回收过程

> 初始标记:标识GC ROOT直接关联的对象.stop the world
>
> 并发标记:用户线程并发,追溯标记,前阶段对象出发,标记所有可达的对象
>
> 并发预处理:查找并发标记阶段修改的对象:从新生代晋升、分配到老年代、修改了的对象
>
> 重新标记:stop the world,扫描CMS堆中的剩余对象
>
> 并发清理:清理垃圾对象,程序不会停顿
>
> 并发重置:重置CMS收集器的数据结构

问题

> 1、并发清理用户线程继续运行,需要预留内存空间给用户线程
>
> 解决:CMSInitiatingOccupancyFraction设置触发百分比
>
> 由于空间小到只用户线程空间不够,虚拟机会使用Serial收集器对老年代回收

> 2、标记-清除会造成大量空间碎片
>
> 解决:UseCMSCompactAtFullCollection在full gc时开启内存碎片整理
>
> CMSFullGCsBeforeCompaction,设置执行不压缩的full gc进行多少次后,跟着来一次带压缩的

###### G1

> 将堆空间分成多个Regin,每个区域独立进行垃圾回收.回收线程和用户线程并发

