### java锁

#### synchronized

无锁-偏向锁-轻量级锁-重量级锁

对象头保存内容：标示(锁类型),mark word

##### 偏向锁

mark word区保存了锁对应的线程id，其他线程获取锁时使用cas设置失败后开始自旋

##### 轻量级锁

自选一定时间后，仍然获取不到。开始升级

吧mark down区域复制到线程栈中，对象头对应区域存栈对应的地址


#### AQS(AbstractQueuedSynchronizer)


重要的属性:state(是否加锁状态)、exclusiveOwnerThread(当前持有锁的线程)、head(队头)

##### ReentrantLock

可重入锁

步骤：
1、lock:非公平锁直接使用CAS进行state设置;公平锁先判断队列是否有等待的，再进行设置

2、设置成功表示获得锁。设置失败再比较当前锁线程是否是自己(可重入)

3、不是自己，创建node，添加到队列中(使用tail通过cas来设置)

4、如果前一个节点是head，阻塞前会再次尝试lock

5、阻塞：通过unsafe.park

##### ReentrantReadWriteLock

读写锁

> 获取写锁时，不能有任何线程持有读锁
>
> 获取读锁时，除了自己线程，不能有线程持有写锁
>
> 读锁是共享锁

原理:

1、读锁和写锁

2、通过state变量来设置读锁和写锁(高位为读状态，低位为写状态)
