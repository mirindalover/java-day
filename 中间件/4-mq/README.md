

#### RabbitMQ、Kafka区别

主要区别是架构模型上：

RabbitMQ基于队列，Kafka基于Topic的。RabbtiMQ适合解耦、可靠性较高的场景，Kafka适合数据流处理和高吞吐量场景

RabbitMQ基于队列存储消息，所以每个队列保留了全部内容，不能进行拆分；Kafka基于Topic，一个Topic分布在不同的broker节点，每个节点又进行了Partition

RabbitMQ:Producer发送给Exchange，由Exchanger路由给队列中，队列中的一条消息只能消费一次

Kafka:Producer发送到Topic，每个topic会有多个partation,每个topic使用 offset维护消费的消费情况
