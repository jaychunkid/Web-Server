package socket;

import http.Request;
import http.Response;

import java.io.IOException;
import java.net.ServerSocket;

public class WebServer {

    public void startServer(int port){
        try{
            ServerSocket server = new ServerSocket(port);
            while(true){
                //为每个连接开启一个子线程进行处理
                StreamSocket socket = new StreamSocket(server.accept());
                new Thread(new HTTPThread(socket)).start();
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    //连接处理线程
    private class HTTPThread implements Runnable{

        private StreamSocket socket;

        HTTPThread(StreamSocket socket){
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                System.out.println(socket.getIP() + ":" + socket.getPort() + "  " + "Connected");
                //获取连接请求内容
                Request request = Request.parse(socket.getInputStream());
                //根据请求信息回发响应
                Response.sendResponse(request, socket.getOutputStream());
                System.out.println(socket.getIP() + ":" + socket.getPort() + "  " + "Disconnected\n");
                //关闭输入输出流和套结字
                socket.close();
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }

}
