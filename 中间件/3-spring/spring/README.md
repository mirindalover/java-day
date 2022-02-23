#### Spring bean 生命周期

几个比较重要的扩展接口说明

- BeanFactoryPostProcessor bean的声明处理类

> 与BeanPostProcessor区别就是：一个是对bean容器中bean的定义，如：bean name修改。BeanPostProcessor是对bean属性的修改

- InstantiationAwareBeanPostProcessor：自定义实例化bean

> postProcessBeforeInstantiation如果返回不为空，相当于自己实例化了bean，直接调用BeanPostProcessor.postProcessAfterInitialization()

- BeanPostProcessor：对bean属性进行再加工

1、Bean创建前准备
	a、实例化BeanFactoryPostProcessor ，执行postProcessBeanFactory

​	b、实例化BeanPostProcessor

​	c、实例化InstantiationAwareBeanPostProcessor

​	d、调用InstantiationAwareBeanPostProcessor.postProcessBeforeInstantiation()。自定义实例化逻辑

> 找到Bean的定义和配置
>
> 回调相关的后置处理器：BeanFactoryPostProcessor 、BeanPostProcessor、InstantiationAwareBeanPostProcessor

2、创建实例

​	a、如果没有自定义实例，使用BeanFactory创建bean对象

​	b、bean的构造器

​	c、调用InstantiationAwareBeanPostProcessor.postProcessAfterInstantiation()和postProcessPropertyValues()。postProcessAfterInstantiation如果是false不会调用下个方法设置属性

> spring容器使用java反射创建bean实例
>
> 扫描Bean声明的属性并解析

3、依赖注入

​	a、为Bean注入属性

​	b、调用BeanNameAware.setBeanName()。让bean获取自身的id属性

​	c、调用BeanFactoryAware.setBeanFactory()。让bean获取spring容器

​	d、BeanPostProcessor.postProcessBeforeInitialization()

​	e、InitializingBean接口afterPropertiesSet()

> 注入Bean的属性

4、容器缓存

​	a、配置文件中bean的init-method方法

​	b、BeanPostProcessor.postProcessAfterInitialization()

​	c、InstantiationAwareBeanPostProcessor.postProcessAfterInitialization()

> 配置文件中的init-method
>
> 回调相关的后置处理器

5、销毁实例

​	a、DisposableBean.destroy()

​	b、配置文件中指定的destroy-method

> 销毁的相关方法