### HashMap

> 理想下，HashMap的存取都是O(1),由于数组长度有限，所以不同的值会放到同一个桶，即hash冲突

>  JDK1.8前，由数组+链表组成，链表主要解决hash冲突

> JDK1.8后，由数组+链表组成。当链表长度大余阈值(8)时，并且数组长度大于64(小于时会有限扩展数组长度)，链表转换成红黑树，减少搜索时间

#### HashMap源码分析

##### HashMap重要的参数变量

```java
//默认初始容量16
static final int DEFAULT_INITIAL_CAPACITY = 1 << 4; // aka 16
//默认的填充因子
static final float DEFAULT_LOAD_FACTOR = 0.75f;
//加载因子
final float loadFactor;
// 临界值 当实际大小(容量*填充因子)超过临界值时，会进行扩容
int threshold;
// 当桶(bucket)上的结点数大于这个值时会转成红黑树
static final int TREEIFY_THRESHOLD = 8;
// 当桶(bucket)上的结点数小于这个值时树转链表
static final int UNTREEIFY_THRESHOLD = 6;
// 桶中结构转化为红黑树对应的table的最小大小
static final int MIN_TREEIFY_CAPACITY = 64;
// 每次扩容和更改map结构的计数器
transient int modCount;
```

- **loadFactor** 为什么是0.75

  > 加载因子过高，提高了空间利用率，增加了查询时间的成本；加载因子过低，减少查询时间成本，但是空间利用率很低。0.75是一个折中的选择

- 当链表长度为8时转为红黑树,为什么是8

  > 链表转红黑树很消耗性能，官方根据泊松分布，链表被放满8个节点概率比较小，且链表在8个节点查询时间和红黑树相差无几

##### 扰动函数

即hash函数

```java
static final int hash(Object key) {
    int h;
    return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
}
```

数组存放时tab位置(n-1)&hash()，因为HashMap大小一般不会特别大，所以取低几位hash值，高位不同低位相同的容易产生hash碰撞

即为了得到更散列的值，所以使用了高位和低位异或运算。来减少hash碰撞

##### 数组长度为什么使用2的幂

> 数据存放在数组的位置是0~n-1。2的幂-1正好是000..1..11;与运算可以根据hash得到一个0~n-1的值

##### put方法

```java
final V putVal(int hash, K key, V value, boolean onlyIfAbsent,
                   boolean evict) {
    Node<K,V>[] tab; Node<K,V> p; int n, i;
    // table未初始化或者长度为0，进行扩容
    if ((tab = table) == null || (n = tab.length) == 0)
        n = (tab = resize()).length;
    if ((p = tab[i = (n - 1) & hash]) == null)
        //桶位置是空，直接放
        tab[i] = newNode(hash, key, value, null);
    else {
        //桶位置有值
        Node<K,V> e; K k;
        if (p.hash == hash &&
            ((k = p.key) == key || (key != null && key.equals(k))))
            //key值相同,先暂存，后续根据配置替换或者不操作==
            e = p;
        else if (p instanceof TreeNode)
            //放到树中
            e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
        else {
            //链表
            for (int binCount = 0; ; ++binCount) {
                //放到链表最末
                if ((e = p.next) == null) {
                    p.next = newNode(hash, key, value, null);
                    //链表的长度达到阈值(默认8)
                    //1.数组长度小于64，扩容数组。大于等于则把链表转成红黑树
                    if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st
                        treeifyBin(tab, hash);
                    break;
                }
                if (e.hash == hash &&
                    ((k = e.key) == key || (key != null && key.equals(k))))
                    //如果链表存在key值相同的，先暂存，后续根据配置替换或者不操作==
                    break;
                p = e;
            }
        }
        if (e != null) { // existing mapping for key
            V oldValue = e.value;
            if (!onlyIfAbsent || oldValue == null)
                //使用新值替换
                e.value = value;
            //回调
            afterNodeAccess(e);
            return oldValue;
        }
    }
    //记录修改次数
    ++modCount;
    if (++size > threshold)
        resize();
    afterNodeInsertion(evict);
    return null;
}
```

##### get方法

```java
final Node<K,V> getNode(int hash, Object key) {
    Node<K,V>[] tab; Node<K,V> first, e; int n; K k;
    if ((tab = table) != null && (n = tab.length) > 0 &&
        (first = tab[(n - 1) & hash]) != null) {
        //取第一个
        if (first.hash == hash && // always check first node
            ((k = first.key) == key || (key != null && key.equals(k))))
            //key相同，直接返回value
            return first;
        //key不同，且还有其他节点
        if ((e = first.next) != null) {
            if (first instanceof TreeNode)
                //查找树
                return ((TreeNode<K,V>)first).getTreeNode(hash, key);
            do {
                if (e.hash == hash &&
                    ((k = e.key) == key || (key != null && key.equals(k))))
                    //遍历链表查找值
                    return e;
            } while ((e = e.next) != null);
        }
    }
    return null;
}
```

##### resize方法

resize方法会重新hash分配，并且会遍历hashmap所有的元素，非常耗时。应尽量避免resize

```java
final Node<K,V>[] resize() {
    Node<K,V>[] oldTab = table;
    int oldCap = (oldTab == null) ? 0 : oldTab.length;
    int oldThr = threshold;
    int newCap, newThr = 0;
    if (oldCap > 0) {
        //超过最大容量，直接设置阈值最大，不再进行扩容，直接让其hash碰撞
        if (oldCap >= MAXIMUM_CAPACITY) {
            threshold = Integer.MAX_VALUE;
            return oldTab;
        }
        //阈值扩大2倍
        else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY &&
                 oldCap >= DEFAULT_INITIAL_CAPACITY)
            newThr = oldThr << 1; // double threshold
    }
    else if (oldThr > 0) // initial capacity was placed in threshold
        newCap = oldThr;
    else {               // zero initial threshold signifies using defaults
        newCap = DEFAULT_INITIAL_CAPACITY;
        newThr = (int)(DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);
    }
    if (newThr == 0) {
        //阈值为0重新计算
        float ft = (float)newCap * loadFactor;
        newThr = (newCap < MAXIMUM_CAPACITY && ft < (float)MAXIMUM_CAPACITY ?
                  (int)ft : Integer.MAX_VALUE);
    }
    threshold = newThr;
    @SuppressWarnings({"rawtypes","unchecked"})
    Node<K,V>[] newTab = (Node<K,V>[])new Node[newCap];
    table = newTab;
    if (oldTab != null) {
        //循环遍历所有的元素，重新放置
        for (int j = 0; j < oldCap; ++j) {
            Node<K,V> e;
            if ((e = oldTab[j]) != null) {
                oldTab[j] = null;
                if (e.next == null)
                    //原来只有一个，直接放置。新位置也肯定只有一个
                    newTab[e.hash & (newCap - 1)] = e;
                else if (e instanceof TreeNode)
                    ((TreeNode<K,V>)e).split(this, newTab, j, oldCap);
                else { // preserve order
                    Node<K,V> loHead = null, loTail = null;
                    Node<K,V> hiHead = null, hiTail = null;
                    Node<K,V> next;
                    do {
                        next = e.next;
                        if ((e.hash & oldCap) == 0) {
                            //位置不变的元素
                            if (loTail == null)
                                loHead = e;
                            else
                                loTail.next = e;
                            loTail = e;
                        }
                        else {
                            //位置变化的元素
                            if (hiTail == null)
                                hiHead = e;
                            else
                                hiTail.next = e;
                            hiTail = e;
                        }
                    } while ((e = next) != null);
                    if (loTail != null) {
                        loTail.next = null;
                        //还是老位置
                        newTab[j] = loHead;
                    }
                    if (hiTail != null) {
                        hiTail.next = null;
                        //新数组的位置
                        newTab[j + oldCap] = hiHead;
                    }
                }
            }
        }
    }
    return newTab;
}
```

举例说明下链表扩容时为什么这样放置

00100100 10100101 11000100 00100101 hash

00000000 00000000 00000000 00001111 16-1=15

------

​																  101 =5

扩容到32
00100100 10100101 11000100 00100101 hash

00000000 00000000 00000000 00010000	16

------

0																			新数组位置不变

不为0，那一定数组位置一定在5+16

#### HashMap的问题

##### JDK1.7链表采用头插法，造成死循环

```java
void transfer(Entry[] newTable, boolean rehash) {
    int newCapacity = newTable.length;
    for (Entry<K,V> e : table) {
        while(null != e) {
            //持有链表的next，为了下次循环使用--问题所在
            Entry<K,V> next = e.next;
            if (rehash) {
                e.hash = null == e.key ? 0 : hash(e.key);
            }
            int i = indexFor(e.hash, newCapacity);
            e.next = newTable[i];
            newTable[i] = e;
            e = next;
        }
    }
}
```

1.7采用头插法，，在多线程时会导致死循环。下面按步骤说明如何出现的

假设数组中某个链表 A->B

1、线程1、2同时put，发现需要扩容；线程1走到代码处暂停，cpu去执行线程2==此时线程1:e=A,next=B

2、线程2执行完扩容操作==由于采用的头插法，此时的链表 B->A

3、继续执行线程1,第一次循环，e=A,next=B	==新链表A

4、第二次循环,e=B,由于线程1的操作导致B的next是A,next=A	==新链表B->A

5、第三次循环,e=A,next=null	==新链表	A->B(next=A)

多线程造成了循环链表，当get、put、或者resize的时候都会导致死循环

##### 哈希碰撞拒绝服务攻击 

> 通过精心构造数据，使得所有数据全部碰撞，人为将哈希表变成一个退化的单链表，此时哈希表各种操作的时间均提升了一个数量级，因此会消耗大量CPU资源，导致系统无法快速响应请求，从而达到拒绝服务攻击（DoS）的目的

