## 网络服务器
分布式计算课程实验，基于socket实现网络服务器，支持响应GET命令和静态CGI程序调用。
* `Request`：封装请求信息内容，其中静态方法`parse()`能够从输入流中读取请求信息并生成`Request`对象
* `Response`：封装根据请求信息向浏览器回发响应的方法
* `StreamSocket`：流式socket操作封装
* `WebServer`：服务器主类，每接收到一个浏览器请求，启动一个`HTTPThread`进行处理
### 程序测试
* 运行`startServer`中的main方法
* 浏览器访问本地8080端口即可
