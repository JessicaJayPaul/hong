# hong
一个非常简单的android图片加载类库，仅供学习参考之用。

> 关于我，请关注  
博客：[wulitao的博客](http://www.wulitao.xyz "wulitao的博客")

## 项目特性

### 图片加载

 1. 提供本地文件、项目资源、网络图片三种加载方式；
 2. 通过给定的ImageView大小压缩图片，防止程序OOM；
 3. 支持内存缓存和磁盘缓存，高效加载图片；

### HTTP操作

 1. GET/POST基本操作；
 2. 异步访问请求，实现接口回调返回数据；
 3. 自由设置请求头，cookie等；
 4. 自定义返回数据格式（string/byte[]）;

### 系统工具

 1. 获取当前应用的版本号；
 2. 获取当前应用的缓存目录；
 3. dp与px互相转换；

### 加密工具

 1. MD5加密字符串；

## 使用方法

Hong.with(context).load(path).resize(width, height).into(imageView);