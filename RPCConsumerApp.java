package com.jimubox;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: liruichuan
 * @Date: 2019/9/27 14:09
 * @Description: rpc demo 客户端发起rpc请求
 */
public class RPCConsumerApp {

    public static void main(String[] args) {

        Shop shop = new ShopImpl();

        String resp = shop.buy("颈椎病康复指南",1);

        System.out.println("购买结果：" + resp);

    }
}

/**
 * 商店
 */
interface Shop{

    /**
     * 购买商品
     * @param name
     * @param count
     * @return
     */
    public String buy(String name,int count);
}

/**
 * 商店实现类(使用代理模式)
 * 其真正的实现类应该是部署到远端服务器上 本地只是个虚拟的实现类
 * 其中封装了远程调用接口的细节
 */
class ShopImpl implements Shop{

    //测试代码 端口暂时写死
    private final static int PORT = 9090;

    @Override
    public String buy(String name, int count) {

        String result = null;

        try{
            //根据服务名称 获取注册中心服务列表 默认是 接口.方法
            List<String> providers = lookupProviders("Shop.buy");

            //根据负载均衡策略筛选提供服务的节点
            String providerAddress = chooseProvider(providers);

            //通讯协议可选择 无论是http  还是socket 都可以
            Socket socket = new Socket(providerAddress,PORT);

            //将请求参数进行序列化
            ShopRequest shopRequest = new ShopRequest(name,count);

            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());

            //将请求发送给服务提供者
            objectOutputStream.writeObject(shopRequest);

            //接受响应
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());

            Object resp = objectInputStream.readObject();
            result = resp.toString();
            System.out.println("购买结果为："+resp.toString());

        }catch (Exception e){
            e.printStackTrace();
            System.out.println("购买异常："+e.getMessage());
        }
        return result;
    }


    /**
     * 获取指定服务的服务实例列表
     *
     * 分布式系统中会有多个服务节点提供服务 通常会有注册中心来管理实例列表
     *
     * @return
     */
    public List<String> lookupProviders(String serveName){

        List<String> providers = new ArrayList<>();
        providers.add("127.0.0.1");

        return providers;
    }

    /**
     *
     * 负载均衡算法 根据服务实例列表 筛选具体服务实例节点提供服务
     * @return
     */
    public String chooseProvider(List<String> providers){
        return providers.get(0);
    }
}

/**
 * rpc 请求参数体
 * 包括请求的接口名 参数 等
 */
@Data
@NoArgsConstructor
class ShopRequest implements Serializable{


    private String method = "buy";

    private String name;

    private int count;

    public ShopRequest(String name, int count) {
        this.name = name;
        this.count = count;
    }
}
