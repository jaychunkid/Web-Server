import socket.WebServer;

public class startServer {

    public static void main(String[] args){
        //在8080端口启动Web服务器
        new WebServer().startServer(8080);
    }

}
