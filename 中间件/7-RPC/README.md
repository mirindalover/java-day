

### RPC


RPC即远程服务调用：常见的如 Dubbo、Grpc、OpenFeign、JSF(京东)

步骤涉及：

1、动态代理:让调用方与调用本地方法一样，屏蔽rpc的细节

2、序列化:网络传输必须的步骤(把传输内容转换成二进制)

3、网络通信:一般是在TCP层进行，但是像OpenFeign是走的http请求

对应消费端,也是通信、反序列化、动态代理

> RPC中还一个比较重要的点--注册中心：服务端如何服务注册、消费端获取服务可用节点获取

#### 动态代理

AOP面向切面编程

apt:生成java文件
aspectJ:java->class编译过程
javassist:class文件修改

在java->class文件过程中,改变代码逻辑

实践:AspectJ、APT(注解处理器)、jdk的动态代理(在运行期生成对应的代理类)、cglib

> aspectJ、APT、cglib等都是在编译期,生成对应代码

> jdk代理是在运行期间创建对象

spring中通过注解进行aop,在创建bean时,根据情况选择jdk还是cglib


##### OpenFeign

1、扫描@FeignCline注解的接口

2、通过MVC Contract解析,生成http请求需要的MethodMetadata

3、生成动态代理对象，放到map中

4、请求时通过map中找到对象，生成request,再通过ribbon等负载均衡中间件，给对应的provider发送请求


##### Dubbo

通过javassist在启动时生成wapper类,减少运行时反射的使用

#### 序列化

序列化有多种方式：

1、java原生序列化:缺点性能不好,属性有对象时需要递归写

2、json:额外空间大,对于java这种强类型需要反射来赋值

3、Hessian:dubbo采用的序列化方式。数据更加紧凑

4、ProtoBuf:grpc采用的proto x(proto3),优点 不需要反射,编译时直接生成对应的方法。缺点 不支持object类型

#### 通信

##### Dubbo

通过dubbo协议(在TCP协议上)进行编解码,通过Netty来发送接收请求

消费者通过注册中心获取provider时,与provider建立长链接


##### OpenFeign

直接使用http协议进行通信

默认为jdk的httpClient，可以替换为Apache的httpClient


##### Grpc

基于Http 2协议


#### 注册中心

注册中心一般都是多选择的

1、Dubbo可配置:ZK、Nacos、Redis

2、OpenFeign:Eureka、Nacos

3、Grpc:etcd、Nacos等

注册中心一般使用AP、可以忍耐一定的延迟同步




