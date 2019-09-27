package com.jimubox;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author: liruichuan
 * @Date: 2019/9/27 14:36
 * @Description: rpc 服务提供者
 */
public class RPCServerApp {
    private final static int PORT = 9090;

    private Shop shop = new RealShopImpl();

    public static void main(String[] args) throws Exception{

        RPCServerApp rpcServerApp = new RPCServerApp();
        rpcServerApp.run();

    }

    public void run() throws Exception{
        ServerSocket serverSocket = new ServerSocket(PORT);
        try {

            //循环接受客户端请求
            while (true){
                Socket socket = serverSocket.accept();
                try{
                    ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());

                    Object request = objectInputStream.readObject();
                    System.out.println("客户端请求参数:"+request.toString());

                    String buyResult = null;
                    if(request instanceof ShopRequest){
                        ShopRequest shopRequest = (ShopRequest) request;
                        if("buy".equalsIgnoreCase(shopRequest.getMethod())){
                            buyResult = shop.buy(shopRequest.getName(),shopRequest.getCount());
                        }
                    }

                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                    objectOutputStream.writeObject(buyResult);

                }catch (Exception e){
                    System.out.println(e.getMessage());
                }finally {
                    socket.close();
                }
            }

        }catch (Exception e){
            System.out.println("exception :" + e.getMessage());
        }finally {
            serverSocket.close();
        }
    }
}

/**
 * 商店实现类
 */
class RealShopImpl implements Shop {

    @Override
    public String buy(String name, int count) {

        String result = null;

        try {
            result = "恭喜您!购买【" + name + "】成功,数量为【" + count + "】";
        } catch (Exception e) {
            System.out.println("购买异常：" + e.getMessage());
            result = "商店打烊 暂时无法出售货物";
        }
        return result;
    }
}
