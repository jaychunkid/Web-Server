package http;

import java.io.*;

public class Response {

    private final static String ROOT = "root";     //文件根目录
    private final static String SERVER_VERSION = "WEBServer: 1.0\r\n";     //服务器版本
    private final static String INDEX_FILE_PATH = ROOT + File.separator + "index.html";       //首页路径
    private final static String ERROR_404_FILE_PATH = ROOT + File.separator + "404.html";     //404页面路径
    private final static String ERROR_500_FILE_PATH = ROOT + File.separator + "500.html";     //500页面路径
    private final static String RESPONSE_LINE_200 = "HTTP/1.1 200 OK \r\n";     //请求成功响应头
    private final static String RESPONSE_LINE_404 = "HTTP/1.1 404 Not Found \r\n";     //404响应头
    private final static String RESPONSE_LINE_500 = "HTTP/1.1 500 Internal Server Error \r\n";     //500响应头

    //根据请求内容，向输出流发送响应内容
    public static void sendResponse(Request request, OutputStream os){
        BufferedOutputStream bufferOS = new BufferedOutputStream(os);
        //根据请求类型做出响应，只支持GET、POST类型处理
        switch (request.getRequestMethod()) {
            case GET:
            case POST:
                if(request.getUri() != null){
                    File targetFile = null;
                    if("/".equals(request.getUri())){
                        //请求URI为空，默认指向主页
                        targetFile = new File(INDEX_FILE_PATH);
                    } else {
                        //请求URI的文件分隔符可能与服务器系统不兼容(如Windows系统),
                        //所以请求URI分割后, 利用Java方法获取系统文件分隔符重新组合
                        String[] fileList = request.getUri().split("/");
                        StringBuilder filePath = new StringBuilder(ROOT);
                        for(String file : fileList){
                            filePath.append(File.separator);
                            filePath.append(file);
                        }
                        targetFile = new File(filePath.toString());
                    }
                    sendFileResponse(targetFile, bufferOS);
                }
                break;
            case HEAD:
            case TRACE:
            case DELETE:
            case OPTIONS:
            case PUT:
            default:
                send404Response(bufferOS);     //对不支持的请求类型, 输出404页面
                break;
        }
    }

    //根据文件类型回发响应
    private static void sendFileResponse(File file, BufferedOutputStream os){
        //判断请求文件是否存在
        if(file.exists()){
            if(file.getPath().endsWith(".cgi")){
                //若请求文件为CGI程序, 则运行CGI程序并回发程序输出
                sendCGIResponse(file, os);
            } else {
                System.out.println("Process: send file response");
                try {
                    //初始化响应头
                    String responseHeader = SERVER_VERSION + "Content-Type: text/html\r\n" +
                            "Content-Length: " + file.length() + "\r\n\r\n";
                    os.write(RESPONSE_LINE_200.getBytes());     //发送响应行
                    os.write(responseHeader.getBytes());        //发送响应头
                    sendFileData(os, file);                     //发送响应内容
                    os.flush();
                } catch (Exception e) {
                    e.printStackTrace();     //传输错误, 打印错误信息
                }
            }
        } else {
            send404Response(os);     //文件不存在, 回发404错误
        }
    }

    //回发CGI程序响应
    private static void sendCGIResponse(File file, BufferedOutputStream os) {
        System.out.println("Process: send CGI response");
        try {
            //运行CGI程序
            Process cgi = Runtime.getRuntime().exec(file.getPath());
            StringBuffer errorStringBuffer = new StringBuffer();
            StringBuffer outputStringBuffer = new StringBuffer();
            //子线程中获取CGI程序的错误输出
            new Thread(() -> {
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(cgi.getInputStream()));
                    String str = null;
                    while ((str = bufferedReader.readLine()) != null) {
                        outputStringBuffer.append(str);
                        outputStringBuffer.append("\r\n");
                    }
                    bufferedReader.close();
                } catch (Exception e) {
                    e.printStackTrace();     //传输错误, 打印错误信息
                }
            }).start();
            //子线程中获取CGI程序的普通输出
            new Thread(() -> {
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(cgi.getErrorStream()));
                    String str = null;
                    while ((str = bufferedReader.readLine()) != null) {
                        errorStringBuffer.append(str);
                        errorStringBuffer.append("\n");
                    }
                    bufferedReader.close();
                } catch (Exception e) {
                    e.printStackTrace();     //传输错误, 打印错误信息
                }
            }).start();
            //等待CGI程序运行完成
            cgi.waitFor();
            if(errorStringBuffer.length() > 0){
                //若程序输出错误信息, 则回发500错误
                System.err.println(errorStringBuffer.toString());
                send500Response(os);
            } else {
                //回发CGI程序输出内容
                os.write(RESPONSE_LINE_200.getBytes());
                os.write(SERVER_VERSION.getBytes());
                os.write(outputStringBuffer.toString().getBytes());
                os.flush();
            }
        } catch (InterruptedException e){
            e.printStackTrace();
            send500Response(os);     //CGI程序运行错误, 回发500错误
        } catch (IOException e) {
            e.printStackTrace();     //传输错误, 打印错误信息
        }
    }

    //回发404错误响应
    private static void send404Response(BufferedOutputStream os){
        System.out.println("Process: send 404 response");
        try{
            File errorFile = new File(ERROR_404_FILE_PATH);
            //初始化响应头
            String responseHeader = SERVER_VERSION + "Content-Type: text/html\r\n" +
                    "Content-Length: " + errorFile.length() + "\r\n\r\n";
            os.write(RESPONSE_LINE_404.getBytes());     //回发响应行
            os.write(responseHeader.getBytes());        //回发响应头
            sendFileData(os, errorFile);                //回发响应内容
            os.flush();
        } catch (Exception e){
            e.printStackTrace();     //传输错误, 打印错误信息
        }
    }

    //回发500错误响应
    private static void send500Response(BufferedOutputStream os){
        System.out.println("Process: send 500 response");
        try{
            File errorFile = new File(ERROR_500_FILE_PATH);
            String responseHeader = SERVER_VERSION + "Content-Type: text/html\r\n" +
                    "Content-Length: " + errorFile.length() + "\r\n\r\n";
            os.write(RESPONSE_LINE_500.getBytes());     //回发响应行
            os.write(responseHeader.getBytes());        //回发响应头
            sendFileData(os, errorFile);                //回发响应内容
            os.flush();
        } catch (Exception e){
            e.printStackTrace();     //传输错误, 打印错误信息
        }
    }

    //从文件中读取信息, 并发送到指定输出流中
    private static void sendFileData(BufferedOutputStream os, File file) throws IOException {
        FileInputStream fileReader = new FileInputStream(file);
        byte[] buffer = new byte[10240];    //一次读取最大10MB的内容
        int length = 0;     //记录读取的字节数目
        while ((length = fileReader.read(buffer)) != -1) {
            os.write(buffer, 0, length);
        }
        fileReader.close();
    }

}
