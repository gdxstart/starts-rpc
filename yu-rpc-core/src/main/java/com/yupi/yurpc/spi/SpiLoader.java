package com.yupi.yurpc.spi;

import cn.hutool.core.io.resource.ResourceUtil;
import com.yupi.yurpc.serializer.Serializer;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SPI 加载起：支持键值对映射
 */
@Slf4j
public class SpiLoader {

    // 存储已经加载的类：接口名=> (key=> 实现类)
    private static Map<String, Map<String,Class<?>>> loaderMap = new ConcurrentHashMap<>();

    //对象实例缓存(避免重复 new 对象) ,类路径 => 对象实例   单例模式
    private static Map<String,Object> instanceCache = new ConcurrentHashMap<>();

    //系统SPI目录
    private static final String RPC_SYSTEM_SPI_DIR = "META-INF/rpc/system/";

    //用户自定义SPI目录
    private static final String RPC_CUSTOM_SPI_DIR = "META-INF/rpc/custom/";

    //扫描路径
    private static final String[] SCAN_DIRS = new String[]{
            RPC_CUSTOM_SPI_DIR,RPC_SYSTEM_SPI_DIR
    };

    //动态加载的类列表
    private static final List<Class<?>> LOAD_CLASS_LIST = Arrays.asList(Serializer.class);

    //加载所有类型
    public static void loadAll(){
        log.info("加载所有SPI");
        for (Class<?> aClass : LOAD_CLASS_LIST){
            load(aClass);
        }
    }
    //加载某个类型
    public static Map<String,Class<?>> load(Class<?> loadClass) {
        log.info("加载类型为{}的SPI",loadClass.getName());
        // 扫描路径，用户自定义SPI的优先级高于系统SPI
        Map<String,Class<?>> keyClassMap = new HashMap<>();
        for (String scanDir : SCAN_DIRS){
             List<URL> resources = ResourceUtil.getResources(scanDir + loadClass.getName());

             //读取每一个resource
            for (URL resource : resources){
                try {
                    InputStreamReader inputStreamReader = new InputStreamReader(resource.openStream());
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String line;
                    //读取文件Buffer中的内容
                    while((line = bufferedReader.readLine()) != null){

                        //分割开每一对键值对
                        String[] strArray = line.split("=");
                        if (strArray.length > 1){
                            String key = strArray[0];
                            String className = strArray[1];
                            keyClassMap.put(key,Class.forName(className));
                        }
                    }
                }catch (Exception e){
                    log.error("spi resource load error ",e);
                }
            }
        }
        //loaderMap:缓存起来
        loaderMap.put(loadClass.getName(),keyClassMap);
        //返回
        return keyClassMap;
    }

    /**
     * 通过反射机制 获取某个接口的实例
     * @param tClass 原类
     * @param key META-INF/rpc/system下的文件 =左边值
     * @return 接口实现类
     * @param <T> 泛型
     */
    public static <T> T getInstance(Class<?> tClass,String key){

        //无非就是要获取到 loaderMap 的值中的值
        // 首先，反射获取loaderMap的key
        String tClassName = tClass.getName();
        // 接着，通过key获取对应的value =>keyClassMap
        Map<String, Class<?>> keyClassMap = loaderMap.get(tClassName);

        // 判断合法性
        if (keyClassMap == null){
            throw new RuntimeException(String.format("SpiLoader 未加载 %s 类型",tClassName));
        }
        if (!keyClassMap.containsKey(key)){
            throw new RuntimeException(String.format("Spiloader 的 %s 不存在key=%s 的类型",tClassName,key));
        }
        // 接着， 获取到要加载的实现类型
        Class<?> implClass = keyClassMap.get(key);

        // 接着， 从实例缓存中加载指定类型的实例
        String implClassName = implClass.getName();
        // 第一次实例缓存中没有数据
        if (!instanceCache.containsKey(implClassName)){
            try{
                instanceCache.put(implClassName,implClass.newInstance());
            }catch (InstantiationException | IllegalAccessException e){
                String errorMsg = String.format("%s 类实例化失败",implClass.getName());
                throw new RuntimeException(errorMsg,e);
            }
        }
        return (T) instanceCache.get(implClassName);
    }

}
