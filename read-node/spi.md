## dubbo spi实现
在dubbo中并没有选择使用传统的Java里的SPI实现，而是选择对其进行扩展，虽然和Java的SPI比较类似
但是有几点需要注意：

1.Dubbo的SPI支持自定义名称
```properties
adaptive=org.apache.dubbo.common.compiler.support.AdaptiveCompiler
jdk=org.apache.dubbo.common.compiler.support.JdkCompiler
javassist=org.apache.dubbo.common.compiler.support.JavassistCompiler
```
比如说我们可以通过KEY=VALUE的形式同一个文件定义多个实现，如果没有指定名称（key值），则使用默认的首字母方法小写作为名称
扫描的路径为：
```properties
META-INF/dubbo/internal/
META-INF/dubbo/
META-INF/services/
```

那么你可能很奇怪为什么要定义多个呢?
```java
@SPI("javassist")
public interface Compiler {
    Class<?> compile(String code, ClassLoader classLoader);
}
```
像这种@SPI("javassist")都已经写死了使用哪个实现了，为什么要需要指定多个呢？
这个可能要根据Dubbo的SPI另一种扩展机制进行说明，Dubbo还提供了扩展的注解`Activate`
具体请参考文章：[Dubbo SPI之Adaptive详解](https://www.jianshu.com/p/dc616814ce98)
简而言之：
如果我们使用了扩展的方式尝试获取SPI实现
```java
ExtensionLoader<AdaptiveExt2> loader = ExtensionLoader.getExtensionLoader(AdaptiveExt2.class);
AdaptiveExt2 adaptiveExtension = loader.getAdaptiveExtension();
```
那么此时`Activate`注解的功能就会生效，且他们的优先级关系如下：

1. 在类上加上@Adaptive注解的类，是最为明确的创建对应类型Adaptive类。所以他优先级最高。
2. 可以再方法上增加@Adaptive注解，注解中的value与链接中的参数的key一致，链接中的key对应的value就是spi中的name,获取相应的实现类。这里我说明一下，如果我们的注解里没有申明变量的情况下，则使用转换后的类名（AbcDef => abc.def）
3. @SPI注解中的value是默认值，如果通过URL获取不到关于取哪个类作为Adaptive类的话，就使用这个默认值，


当然如果我们使用的不是这种方式
```java
ExtensionLoader.getExtensionLoader(type).getExtension(name);
```
那就没有什么操作的空间了，就是按名称获取不会有`Activate`的概念而言了。


