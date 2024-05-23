package com.yupi.yurpc.registry;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import cn.hutool.json.JSONUtil;
import com.yupi.yurpc.config.RegistryConfig;
import com.yupi.yurpc.model.ServiceMetaInfo;
import io.etcd.jetcd.*;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;
import io.etcd.jetcd.watch.WatchEvent;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * ETCD:用于注册中心
 */
public class EtcdRegistry implements Registry{

    private Client client;

    private KV kvClient;

    //为了区分不同的项目，使用了跟节点rpc
    private static final String ETCD_ROOT_PATH = "/rpc/";

    // 本地注册的节点 key 集合 :主要用于维护续期
    private final Set<String> localRegisterNodeKeySet = new HashSet<>();

    // 注册中心服务缓存
    private final RegistryServiceCache registryServiceCache = new RegistryServiceCache();
    
    // 正在监听的 key 集合
    private final Set<String> watchingKeySet = new ConcurrentHashSet<>();//ConcurrentHashSet:防止并发冲突


    public static void main(String[] args) throws ExecutionException, InterruptedException {

        Client client = Client.builder().endpoints("http://localhost:2379").build();

        KV kvClient = client.getKVClient();
        ByteSequence key = ByteSequence.from("test_key".getBytes());
        ByteSequence value = ByteSequence.from("test_value".getBytes());

        kvClient.put(key,value).get();

        CompletableFuture<GetResponse> getFuture = kvClient.get(key);

        GetResponse response = getFuture.get();

        kvClient.delete(key).get();
    }

    @Override
    public void init(RegistryConfig registryConfig) {
        client = Client.builder()
                .endpoints(registryConfig.getAddress())
                .connectTimeout(Duration.ofMillis(registryConfig.getTimeout()))
                .build();

        kvClient = client.getKVClient();

        // 初始化时调用心跳机制给服务提供者续约
        heartBeat();
    }

    @Override
    public void register(ServiceMetaInfo serviceMetaInfo) throws Exception {
        // 创建Lease 和kv 客户端
        Lease leaseClient = client.getLeaseClient();

        // 创建一个30秒的租约
        long leaseId = leaseClient.grant(30).get().getID();

        serviceMetaInfo.setServiceHost("localhost");
        serviceMetaInfo.setServicePort(8080);
        // 设置键值对
        String registerKey = ETCD_ROOT_PATH + serviceMetaInfo.getServiceNodeKey();
        ByteSequence key = ByteSequence.from(registerKey, StandardCharsets.UTF_8);
        ByteSequence value = ByteSequence.from(JSONUtil.toJsonStr(serviceMetaInfo),StandardCharsets.UTF_8);
        /** key:    /rpc/com.yupi.example.common.service.UserService:1.0/localhost:8080
            value:{"serviceName":"com.yupi.example.common.service.UserService",
                    "serviceVersion":"1.0",
                    "serviceAddress":"http://localhost:8080",
                    "serviceHost":"localhost",
                    "servicePort":8080,
                    "serviceGroup":"default"
                    }
        **/
//        System.out.println("tttttttttttttt" + key + "tttttttttttttt" + value);
        // 将键值对与租约关联起来，并设置过期时间
        PutOption putOption = PutOption.builder().withLeaseId(leaseId).build();
        kvClient.put(key,value,putOption).get();

        // 把节点信息添加到本地缓存
        localRegisterNodeKeySet.add(registerKey);
    }

    @Override
    public void unRegister(ServiceMetaInfo serviceMetaInfo) {
        String registerKey = ETCD_ROOT_PATH + serviceMetaInfo.getServiceNodeKey();
        ByteSequence key = ByteSequence.from(registerKey, StandardCharsets.UTF_8);
        kvClient.delete(key);

        // 需要从本地缓存中删除
        localRegisterNodeKeySet.remove(registerKey);
    }

    @Override
    public List<ServiceMetaInfo> serviceDiscovery(String serviceKey) {

        // 优先从缓存中获取服务
        List<ServiceMetaInfo> cachedServiceMetaInfoList = registryServiceCache.readCache();

        if (cachedServiceMetaInfoList !=null){
            return cachedServiceMetaInfoList;
        }

        // 前缀搜索
        String searchPrefix = ETCD_ROOT_PATH + serviceKey + "/";

       try{
           // 前缀查询
           GetOption getOption = GetOption.builder().isPrefix(true).build();
           List<KeyValue> keyValues = kvClient.get(
                           ByteSequence.from(searchPrefix, StandardCharsets.UTF_8), getOption)
                   .get()
                   .getKvs();

           // 解析服务信息
           List<ServiceMetaInfo> serviceMetaInfoList = keyValues.stream()
                   .map(keyValue -> {
                       String key = keyValue.getKey().toString(StandardCharsets.UTF_8);
                       System.out.println("dddddddd"+key);
                       // 监听 key 变化
                       watch(key);

                       String value = keyValue.getValue().toString(StandardCharsets.UTF_8);
                       return JSONUtil.toBean(value, ServiceMetaInfo.class);
                   })
                   .collect(Collectors.toList());

           // 写入服务缓存
           registryServiceCache.writeCache(serviceMetaInfoList);
           return serviceMetaInfoList;
       }catch (Exception e){
           throw new RuntimeException("获取服务列表失败",e);
       }
    }



    @Override
    public void destroy() {
        System.out.println("当前节点下线");

        // 遍历本节点的所有 key
        for (String key : localRegisterNodeKeySet){
            try{
                kvClient.delete(ByteSequence.from(key,StandardCharsets.UTF_8)).get();
            }catch (Exception e){
                throw new RuntimeException(key + "节点下线失败");
            }
        }

        // 释放资源
        if (kvClient != null){
            kvClient.close();
        }
        if (client != null){
            client.close();
        }
    }

    /**
     *  使用Hutool工具类的CronUtil实现定时任务,对本地注册的key集合中每一个节点进行 重新注册 操作 (相当于续期了)
     */
    @Override
    public void heartBeat() {
        // 10秒续签一次
        CronUtil.schedule("*/10 * * * * *", new Task() {
            @Override
            public void execute() {
                // 遍历本节点的所有的key
                for (String key : localRegisterNodeKeySet){
                    try{
                        List<KeyValue> keyValues = kvClient.get(ByteSequence.from(key, StandardCharsets.UTF_8))
                                .get()
                                .getKvs();

                        // 如果节点过期,需要重新启动节点才能重新注册
                        if (CollUtil.isEmpty(keyValues)){
                            continue;
                        }
                        // 节点未过期,重新注册
                        KeyValue keyValue = keyValues.get(0);
                        String value = keyValue.getValue().toString(StandardCharsets.UTF_8);
                        ServiceMetaInfo serviceMetaInfo = JSONUtil.toBean(value, ServiceMetaInfo.class);
                        register(serviceMetaInfo);
                    }catch (Exception e){
                        throw new RuntimeException(key + "续签失败",e);
                    }
                }
            }
        });


        // 支持秒级别定时任务
        CronUtil.setMatchSecond(true);
        CronUtil.start();
    }

    /**
     * 监听:消费端
     * @param serviceNodeKey
     */
    @Override
    public void watch(String serviceNodeKey) {
        Watch watchClient = client.getWatchClient();

        boolean newWatch = watchingKeySet.add(serviceNodeKey);

        if (newWatch){
            //开启监听
            watchClient.watch(ByteSequence.from(serviceNodeKey,StandardCharsets.UTF_8),response ->{
               for (WatchEvent event : response.getEvents()){

                   switch (event.getEventType()){

                       case DELETE:
                           registryServiceCache.clearCache();
                           break;
                       case PUT:
                       default:
                           break;
                   }
               }
            });
        }
    }
}
