/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.dubbo.common.extension;

import org.apache.dubbo.common.URL;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Provide helpful information for {@link ExtensionLoader} to inject dependency extension instance.
 *
 * 所有包含当前{@link Adaptive}注解的bean或者说方法，在我们通过dubbo SPI扩展获取的时候
 * {@link ExtensionLoader#getExtensionLoader#getAdaptiveExtension()}方法的时候，实际上会通过我们
 * {@link ExtensionLoader#createAdaptiveExtensionClass()} 获取被代理生成的类和编译成字节码
 *
 * 这里以我们的{@link ThreadPool}为例子看看生成的字节码是什么样子
 *
 * public class ThreadPool$Adaptive implements org.apache.dubbo.common.threadpool.ThreadPool {
 * 	public java.util.concurrent.Executor getExecutor(org.apache.dubbo.common.URL arg0) {
 * 		if (arg0 == null) throw new IllegalArgumentException("url == null");
 * 		org.apache.dubbo.common.URL url = arg0;
 * 		String extName = url.getParameter("threadpool", "fixed");
 * 		if (extName == null)
 * 			throw new IllegalStateException("Failed to get extension (org.apache.dubbo.common.threadpool.ThreadPool) name from url (" + url.toString() + ") use keys([threadpool])");
 * 		org.apache.dubbo.common.threadpool.ThreadPool extension = (org.apache.dubbo.common.threadpool.ThreadPool) ExtensionLoader.getExtensionLoader(org.apache.dubbo.common.threadpool.ThreadPool.class).getExtension(extName);
 * 		return extension.getExecutor(arg0);
 *        }
 * }
 *
 * 首先会保留原始的参数不变，然后通过参数不为空的校验（这里的URL是dubbo自己的，非java的URL）
 * URL里有非常重要的参数，也就是说我们注解{@link Adaptive#value()}会决定我们getParameter的Key，而{@link SPI#value()}则会表示为默认值
 * 如果我们的{@link Adaptive#value()}有多个key(key1,key2,key3)则会将当前的getParameter改写为如下形式
 *  url.getParameter("key1",  url.getParameter("key2",  url.getParameter("key2", "fixed")));
 *  也就是优先取K1，取不到取K2，还取不到去K3，最后默认为SPI的value
 *
 *
 *
 * @see ExtensionLoader
 * @see URL
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Adaptive {
    /**
     * Decide which target extension to be injected. The name of the target extension is decided by the parameter passed
     * in the URL, and the parameter names are given by this method.
     * <p>
     * If the specified parameters are not found from {@link URL}, then the default extension will be used for
     * dependency injection (specified in its interface's {@link SPI}).
     * <p>
     * For example, given <code>String[] {"key1", "key2"}</code>:
     * <ol>
     * <li>find parameter 'key1' in URL, use its value as the extension's name</li>
     * <li>try 'key2' for extension's name if 'key1' is not found (or its value is empty) in URL</li>
     * <li>use default extension if 'key2' doesn't exist either</li>
     * <li>otherwise, throw {@link IllegalStateException}</li>
     * </ol>
     * If the parameter names are empty, then a default parameter name is generated from interface's
     * class name with the rule: divide classname from capital char into several parts, and separate the parts with
     * dot '.', for example, for {@code org.apache.dubbo.xxx.YyyInvokerWrapper}, the generated name is
     * <code>String[] {"yyy.invoker.wrapper"}</code>.
     *
     * @return parameter names in URL
     */
    String[] value() default {};

}
