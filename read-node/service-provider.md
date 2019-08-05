## dubbo服务提供者
dubbo的实现，核心两大要素就是服务提供者和服务消费者，我们的消费者通过reference将其包装成代理对象，底层通过netty或者其他的网络框架进行数据远程调用
常用的就是netty，而我们的提供者则需要通过provider将其的功能暴露给底层的网络框架，使其在netty server在处理的适合能够按需选择合适的处理者。

本文将从服务提供的启动过程来看看内部是如何运作的。

## 标签解析
首席按我们必须要看的就是我们的xml配置是如何被spring解析的，这就要回到spring的命名空间解析的逻辑的，之前已经写过相关的笔记进行描述了，我们现在需要做的就是定位dubbo的命名空间解析器了。
而毫无以为就是`DubboNamespaceHandler`,这里我们就直接定位`service`的标签的解析器。
其实整个dubbo的命名空间解析器都是DubboBeanDefinitionParser,主要是注意他的两个参数
```java
public DubboBeanDefinitionParser(Class<?> beanClass, boolean required) {
    this.beanClass = beanClass;
    this.required = required;
}
```
一个是bean对应的class，一个是表示bean是否为必须的，默认都是必须的。
既然我们知道都是使用同一个DubboBeanDefinitionParser。那么其出现个性化逻辑的可能性就会很少了，因为这里也仅仅只是定义了beanDefinition,所以后续的注册过程还是需要看spring的，那么这里我们就要想dubbo是否有什么自定义的beanPostProcessor进行个性化的拦截和处理呢？
但是我们可以从注册的过程中看到，其没有特殊类似spring的`annotation-driven`这样的注解驱动注解，所以
我们换种思路，他是否自己实现Aware接口或者InitialBean这样的接口，
而从我们的`ServiceBean`中可以看出他确实是这样操作的

## serviceBean解析
首先我们需要根据spring的加载顺序来决定分析的顺序，不用多想首先优先分析`afterPropertiesSet`方法
而不出所料这个方法首先就是初始化或者说加载他必要的依赖信息如：
1.provider(ProviderConfig)
2.application(ApplicationConfig)
3.module(ModuleConfig)
4.metadataReportConfig(MetadataReportConfig)
5.configCenter(ConfigCenterConfig)
6.monitor(MonitorConfig)
7.metric(MetricsConfig)
8.判断是否有applicationListener如果没有则直接export方法进行暴露

首先说一下前面的七点，这些都是每个Dubbo暴露接口必须拥有的，但是Dubbo提供了两种加载的方式
1.使用公用的如application,register信息，这时候我们不需要在参数指明
2.在service的标签里指明所依赖的参数信息，具体的参数列表可以在`dubbo.xsd`中看出

当然这里并不想介绍每个参数是如何被载入和配置的，我们还是继续看如果暴露我们的service，
我们刚刚说到了通过export方法进行暴露，如果我们存在applicationLister的时候就会等待springd的事件触发。


