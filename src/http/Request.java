package http;

import util.Method;

import java.io.*;

//封装请求信息
public class Request {

    private Method requestMethod;     //记录请求类型
    private String uri;               //记录请求URI

    private Request(Method requestMethod, String uri){
        this.requestMethod = requestMethod;
        this.uri = uri;
    }

    Method getRequestMethod(){
        return requestMethod;
    }

    String getUri(){
        return uri;
    }

    //从输入流中读取请求内容，生成Request类对象
    public static Request parse(InputStream is){
        StringBuilder requestStringBuilder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String str = null;
        try {
            //只读取请求报文的请求行和请求头部分，利用请求头尾的空行判断读取结束
            while ((str = reader.readLine()) != null && !"".equals(str)) {
                requestStringBuilder.append(str);
                requestStringBuilder.append("\r\n");
            }
        } catch (IOException e){
            e.printStackTrace();
        }
        //输出请求内容
        System.out.println("Request: ");
        System.out.println(requestStringBuilder.toString());
        //按行分割请求内容
        String[] requestData = requestStringBuilder.toString().split("\r\n");
        if(requestData.length > 0) {
            //分割请求行内容为请求类型、URI、HTTP版本三部分
            String[] requestLine = requestData[0].split(" ");
            if(requestLine.length == 3){
                return new Request(parseRequestMethod(requestLine[0]), requestLine[1]);
            }
        }
        return new Request(Method.UNKNOWN, null);
    }

    //将请求类型字符串转换为对应枚举对象
    private static Method parseRequestMethod(String str){
        switch (str) {
            case "GET":
                return Method.GET;
            case "POST":
                return Method.POST;
            case "HEAD":
                return Method.HEAD;
            case "OPTIONS":
                return Method.OPTIONS;
            case "PUT":
                return Method.POST;
            case "DELETE":
                return Method.DELETE;
            case "TRACE":
                return Method.TRACE;
            default:
                return Method.UNKNOWN;
        }
    }

}
