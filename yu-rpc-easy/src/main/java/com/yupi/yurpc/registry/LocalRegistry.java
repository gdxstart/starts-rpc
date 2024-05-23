package com.yupi.yurpc.registry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

    /**
     * 本地注册中心：存服务提供者信息
     */
    public  class LocalRegistry {

        // ConcurrentHashMap:线程安全 以key：服务名称 value：服务实现类 存储服务注册信息
        /**
         * 注册信息的存储
         */
        private static final Map<String,Class<?>> map = new ConcurrentHashMap<>();

        /**
         * 注册服务
         * @param serviceName 服务名称
         * @param implClass 服务的所有实现类型
         */
        public static void register(String serviceName,Class<?> implClass){

            //将服务信息放入map中
            map.put(serviceName,implClass);
        }

        /**
         * 根据服务名称获取所有服务实现类型
         * @param serviceName 服务名
         * @return 服务实现类类型
         */
        public static Class<?> get(String serviceName){
            //直接从map中获取
            return map.get(serviceName);
        }

        /**
         * 删除服务
         * @param serviceName 服务名
         */
        public static void remove(String serviceName){
            map.remove(serviceName);
        }

    }
