package socket;

import java.io.*;
import java.net.Socket;

public class StreamSocket {

    private Socket socket;
    private OutputStream os;     //向流中写入信息
    private InputStream is;      //从流中读取信息
    private String ip;           //记录连接方ip地址
    private int port;            //记录连接方端口号

    StreamSocket(Socket socket) throws IOException {
        this.socket = socket;
        ip = socket.getInetAddress().toString();
        port = socket.getPort();
        setStreams();
    }

    private void setStreams() throws IOException {
        is = socket.getInputStream();
        os = socket.getOutputStream();
    }

    OutputStream getOutputStream(){
        return os;
    }

    InputStream getInputStream(){
        return is;
    }

    String getIP(){
        return ip;
    }

    int getPort() { return port; }

    void close() throws IOException {
        is.close();
        os.close();
        socket.close();
    }

}
