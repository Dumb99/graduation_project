//package com.baiyun.asstorage.config;
//
//import com.alibaba.fastjson.JSON;
//import lombok.Data;
//import lombok.extern.slf4j.Slf4j;
//import org.aopalliance.intercept.MethodInterceptor;
//import org.aopalliance.intercept.MethodInvocation;
//import org.springframework.aop.framework.ProxyFactory;
//import org.springframework.beans.BeansException;
//import org.springframework.beans.factory.config.BeanPostProcessor;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.redis.core.RedisTemplate;
//
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.io.ObjectOutputStream;
//import java.lang.reflect.Method;
//
///**
// * 本类的作用是输出 RedisTemplate 命令执行日志。包括：命令名称、参数、返回值等
// * 原理是使用动态代理拦截类 redisConnectionFactory 的 getConnection() 方法，监控Redis命令
// * 目前主要输出 get/set/pExpire/pSetEx 等命令日志，可根据需要扩展
// */
//@Configuration
//@Slf4j
//public class RedisCommandLogBean implements BeanPostProcessor {
//
//    @Override
//    public Object postProcessBeforeInitialization(Object o, String s) throws BeansException {
//
//        return o;
//    }
//
//    @Override
//    public Object postProcessAfterInitialization(Object bean, String beanName) {
//
//        if (beanName.equals("redisConnectionFactory")) {
//
//            // 使用代理，覆盖原Redis工厂类
//            return getProxy(bean, invocation -> this.interceptorRedisFactory(invocation));
//
//        } else if (bean instanceof RedisTemplate) {
//
//        }
//        return bean;
//    }
//
//    // 使用 ProxyFactory 类生成动态代理方法
//    public static Object getProxy(Object obj, MethodInterceptor interceptor) {
//
//        ProxyFactory proxy = new ProxyFactory(obj);
//        proxy.setProxyTargetClass(true);
//        proxy.addAdvice(interceptor);
//        return proxy.getProxy();
//    }
//
//    // 如果是getConnection方法，则对该方法进行代理
//    public Object interceptorRedisFactory(MethodInvocation invocation) throws Throwable {
//
//        Object ret = invocation.proceed();
//        String methodName = invocation.getMethod().getName();
//        if (methodName.equals("getConnection")) {
//            return getProxy(ret, this::interceptorGetConnection);
//        }
//        return ret;
//    }
//
//    private Object interceptorGetConnection(MethodInvocation invocation) throws Throwable {
//
//        Method method = invocation.getMethod();
//        String name = method.getName();
//        if (name.equals("isPipelined") || name.equals("close"))
//            return invocation.proceed();
//
//        RedisDto redisDto = new RedisDto();
//        redisDto.setMethodName(name);
//
//        Object target = invocation.getThis();
//        redisDto.setClassName(target.getClass().getName());
//
//        Object[] args = invocation.getArguments();
//
//        // 方法名(命令参数）
//        String commandName = invocation.getMethod().getName();
//
//        if (this.containMethod(commandName)) {
//
//            String[] argsStr = new String[args.length];
//            for (int i = 0; i < args.length; i++) {
//
//                // 字节数组类型
//                if (args[i] instanceof byte[]) {
//
//                    String s = new String(toByteArray(args[i]));
//
//                    // 有乱码，乱码中包含字符串"xp"，原因不明。这里删除乱码
//                    if (s.indexOf("xp") > 0) {
//
//                        s = s.substring(s.indexOf("xp") + 2).trim();
//
//                    }
//
//                    argsStr[i] = s;
//                } else {
//                    argsStr[i] = args[i].toString();
//                }
//
//            }
//            redisDto.setParams(JSON.toJSONString(argsStr));
//        }
//
//        Object ret = null;
//        long start = System.currentTimeMillis();
//
//        try {
//
//            ret = invocation.proceed();
//
//            String result = new String(toByteArray(ret));
//
//            // 有乱码，乱码中包含字符串"xp"，原因不明。这里删除乱码
//            if (result.indexOf("xp") > 0) {
//                result = result.substring(result.indexOf("xp") + 2).trim();
//            }
//
//            redisDto.setResult(result);
//
//            return ret;
//        } catch (Exception ex) {
//
//            redisDto.setEx(ex);
//            throw ex;
//
//        } finally {
//
//            redisDto.setExecuteTime(System.currentTimeMillis() - start);
//
//            if (this.containMethod(commandName))
//                log.info("Redis Log: method:{} params:{} result:{}",
//                        redisDto.getMethodName(), redisDto.getParams(), redisDto.getResult());
//        }
//    }
//
//    // 监控的命令
//    private boolean containMethod(String commandName) {
//
//        if (commandName.equalsIgnoreCase("get")
//                || commandName.equalsIgnoreCase("set")
//                || commandName.equalsIgnoreCase("pExpire")
//                || commandName.equalsIgnoreCase("pSetEx")) {  // 对应方法：ValueOperations --> set(K key, V value, long timeout, TimeUnit unit)
//
//            return true;
//        }
//
//        return false;
//    }
//
//    public byte[] toByteArray(Object obj) {
//
//        byte[] bytes = null;
//        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//        try {
//            ObjectOutputStream oos = new ObjectOutputStream(bos);
//            oos.writeObject(obj);
//            oos.flush();
//            bytes = bos.toByteArray();
//            oos.close();
//            bos.close();
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }
//        return bytes;
//    }
//
//
//    @Data
//    public class RedisDto {
//
//        private String className;
//
//        private String methodName;
//
//        private String params;
//
//        private Object result;
//
//        private long executeTime;
//
//        private String remark;
//
//        private Exception ex;
//    }
//}
