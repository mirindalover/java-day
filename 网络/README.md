### 网络

4层模型：链接层、网际层、传输层、应用层

7层模型：物理层、数据链路层、网络层、传输层、会话层、表示层、应用层

#### https

对称加密：DES，AES 

非对称加密：RSA，ECC

CA：颁发证书,service发送公钥,私钥自己解谜用

https由http进行通信,但是利用SSL/TSL加密数据

先执行http握手,再执行TLS握手

1、client发送请求,给出协议版本号、客户端支持的加密方式、随机数client_random

2、service确认加密方法,给出数字证书、随机数server_random

3、client确认证书有效,生成随机数pre_random，并使用证书中的公钥加密，发送给service

4、service使用私钥揭秘随机数

5、通讯使用约定的加密方法和上面3个随机数,生成会话密钥,用对称加密+密钥加密下面的通信


#### http

三次握手：共发送3个包
1、client发送syn=j包
2、service发送ack=j+1 syn=k
3、client发送 ack=k+1

4次挥手:

1、client发送fin，seq=p
2、service回复ack=p+1
3、service发送fin，seq=q，ack=p+1(不和2合并发送:因为service可能会有未发完的内容,全都发完后再关闭)
4、client发送ack=q+1(这里client会等待2个MSL--报文最大生存时间,防止service没有收到ack而重发)

##### http 1.0

最初版本

##### http 1.1

支持keep alive，一个tcp连接支持多次请求(连接复用，减少tcp握手)

###### 队头阻塞

请求的资源必须排队依次传输(依次 请求-应答)

临时解决：

1、开启多个socket连接

2、域名碎片

##### http 2

1、头部压缩(请求的head进行压缩，http 1 只能压缩body)

2、多路复用。支持一个连接并发多个请求，

##### TCP的队头阻塞

tcp协议由于是可靠协议，tcp发送包时,如果前面的包没有收到 后面的包会存到缓冲区中 等待丢包重传.造成了阻塞

##### http 3

基于UDP,使用了新的QUIC协议，实现了可靠传输

引入了http 2 的流和多路复用(单个流是有序的,其他流不受影响)


##### http快启

client向service发送syn时携带快启选项,service相应时携带cookie

下次连接时:client发送syn携带cookie,service发送sck和syn后 直接传输数据

#### Netty

