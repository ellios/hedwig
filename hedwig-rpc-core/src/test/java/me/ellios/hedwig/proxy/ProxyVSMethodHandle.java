package me.ellios.hedwig.proxy;

import com.google.common.base.Stopwatch;
import org.testng.annotations.Test;
import sun.misc.ProxyGenerator;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.lang.invoke.MethodHandles.lookup;
import static org.testng.Assert.assertTrue;

/**
 * jdk proxy vs jdk methodHandle
 *
 * @author gaofeng
 * @since: 13-10-22
 */
public class ProxyVSMethodHandle {

    static class Hello {

        static void hello() {
            System.out.println("hello");
        }

    }

    static interface HelloService {

        void hello();

        int sum(int a, int b);
    }

    static class HelloServiceImpl implements HelloService {

        @Override
        public void hello() {
            //System.out.println("hello");
        }

        @Override
        public int sum(int a, int b) {
            return a + b;
        }
    }

    static class HelloHandler implements InvocationHandler {

        final Object delegate;

        HelloHandler(Object delegate) {
            this.delegate = delegate;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            return method.invoke(delegate, args);
        }
    }

    private void showMsg(String msg, Stopwatch stopwatch) {
        System.out.println(msg + ":" + stopwatch);
    }


    @Test
    public void testStaticMethod() throws Throwable {
        MethodType type = MethodType.methodType(void.class);
        MethodHandle method = lookup()
                .findStatic(Hello.class, "hello", type);
        method.invoke();
    }

    @Test
    public void testProxyVSMethodHandle() throws Throwable {
        HelloService delegate = new HelloServiceImpl();
        Stopwatch stopwatch = Stopwatch.createStarted();
        final int unit = 10000;
        int count = 100 * unit;

        MethodType type = MethodType.methodType(void.class);
        MethodHandle helooMethod = lookup()
                .findVirtual(HelloService.class, "hello", type);

        //System.getProperties().put("sun.misc.ProxyGenerator.saveGeneratedFiles", true);
        //writeProxyClassToHardDisk(HelloService.class);

        HelloService helloProxy = (HelloService) Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(),
                new Class[]{HelloService.class}, new HelloHandler(delegate));

        stopwatch.reset();
        stopwatch.start();
        for (int i = count; i > 0; i--) {
            helloProxy.hello();
        }
        stopwatch.stop();
        long proxyElapsed = stopwatch.elapsed(TimeUnit.MILLISECONDS);
        showMsg("proxyElapsed", stopwatch);

        stopwatch.reset();
        stopwatch.start();
        for (int i = count; i > 0; i--) {
            helooMethod.invokeExact(delegate);
        }
        stopwatch.stop();
        long methoHanleElapsed = stopwatch.elapsed(TimeUnit.MILLISECONDS);
        showMsg("methoHanleElapsed", stopwatch);

        assertTrue(proxyElapsed > methoHanleElapsed); // proxyElapsed > methoHanleElapsed * 10;
    }

    @Test
    public void testProxyVSMethodHandleWithArgs() throws Throwable {
        HelloService delegate = new HelloServiceImpl();
        Stopwatch stopwatch = Stopwatch.createStarted();
        final int unit = 10000;
        int count = 100 * unit;
        int result = 0;

        MethodType type = MethodType.methodType(int.class, int.class, int.class);
        MethodHandle helooMethod = lookup()
                .findVirtual(HelloService.class, "sum", type);

        HelloService helloProxy = (HelloService) Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(),
                new Class[]{HelloService.class}, new HelloHandler(delegate));

        stopwatch.reset();
        stopwatch.start();
        for (int i = count; i > 0; i--) {
            result = helloProxy.sum(100, 200);
        }
        stopwatch.stop();
        long proxyElapsed = stopwatch.elapsed(TimeUnit.MILLISECONDS);
        showMsg("proxyElapsed", stopwatch);

        stopwatch.reset();
        stopwatch.start();
        for (int i = count; i > 0; i--) {
            result = (int) helooMethod.invokeExact(delegate, 100, 200);
        }
        stopwatch.stop();
        long methoHanleElapsed = stopwatch.elapsed(TimeUnit.MILLISECONDS);
        showMsg("methoHanleElapsed", stopwatch);

        assertTrue(proxyElapsed > methoHanleElapsed); // proxyElapsed > methoHanleElapsed * 10;
    }

    @Test
    public void testInvoker() throws Throwable {

        HelloService delegate = new HelloServiceImpl();
        Stopwatch stopwatch = Stopwatch.createStarted();
        int count = 1000000;

        MethodType type = MethodType.methodType(int.class, int.class, int.class);
        MethodHandle helooMethod = lookup()
                .findVirtual(HelloService.class, "sum", type);

        stopwatch.start();
        for (int i = count; i > 0; i--) {
            helooMethod.invoke(delegate, 100, 500);
        }
        stopwatch.stop();
        long v1Cost = stopwatch.elapsed(TimeUnit.MILLISECONDS);
        showMsg("invoke without result, cost", stopwatch);

        stopwatch.reset();
        stopwatch.start();
        for (int i = count; i > 0; i--) {
            int result = (int) helooMethod.invoke(delegate, 100, 500);
        }
        stopwatch.stop();
        long v2Cost = stopwatch.elapsed(TimeUnit.MILLISECONDS);
        showMsg("invoke with result, costt", stopwatch);

        assertTrue(v1Cost > v2Cost); // v1Cost > v2Cost * 4;
    }


    static class HelloHandler4MethodHandle<T> implements InvocationHandler {

        final T delegate;

        HelloHandler4MethodHandle(T delegate) {
            this.delegate = delegate;
        }

        static Map<Method, MethodHandle> map = new HashMap<>();

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            MethodHandle methodHandle = map.get(method);
            if (methodHandle == null) {
                methodHandle = lookup().findVirtual(method.getDeclaringClass(), method.getName(), MethodType.methodType(method.getReturnType(), method.getParameterTypes()));
                map.put(method, methodHandle);
            }
            // 这里怎么处理?
            return methodHandle.invokeExact(delegate, args[0], args[1]);
        }
    }

    @Test
    public void testProxyVSMethodHandleWithArgsWithMap() throws Throwable {
        HelloService delegate = new HelloServiceImpl();
        Stopwatch stopwatch = Stopwatch.createStarted();
        final int unit = 10000;
        int count = 100 * unit;
        int result = 0;

        HelloService helloProxy = (HelloService) Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(),
                new Class[]{HelloService.class}, new HelloHandler(delegate));

        stopwatch.reset();
        stopwatch.start();
        for (int i = count; i > 0; i--) {
            result = helloProxy.sum(100, 200);
        }
        stopwatch.stop();
        long proxyElapsed = stopwatch.elapsed(TimeUnit.MILLISECONDS);
        showMsg("proxyElapsed", stopwatch);


        HelloService helloProxyMethodHandle = (HelloService) Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(),
                new Class[]{HelloService.class}, new HelloHandler4MethodHandle(delegate));
        stopwatch.reset();
        stopwatch.start();
        for (int i = count; i > 0; i--) {
            result = helloProxyMethodHandle.sum(100, 200);
        }
        stopwatch.stop();
        long methoHanleElapsed = stopwatch.elapsed(TimeUnit.MILLISECONDS);
        showMsg("methoHanleElapsed", stopwatch);

        assertTrue(proxyElapsed > methoHanleElapsed); // proxyElapsed > methoHanleElapsed * 10;
    }

    public static void writeProxyClassToHardDisk(Class cz) {
        byte[] classFile = ProxyGenerator.generateProxyClass("$Proxy11", new Class[]{cz});
        FileOutputStream out = null;
        try {
            out = new FileOutputStream("d:/" + cz.getName() + ".class");
            out.write(classFile);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
