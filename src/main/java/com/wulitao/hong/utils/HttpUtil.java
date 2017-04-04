package com.wulitao.hong.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * HTTP工具类
 * @author wulitao
 * @date 2017年3月7日
 * @subscription
 */
public class HttpUtil {
    
    /**
     * 默认格式化编码格式
     */
    public static final String DEFAULT_ENCODE = "UTF-8";
    
    /**
     * GBK编码
     */
    public static final String GBK_ENCODE = "GBK";
    
    /**
     * get请求
     */
    public static final String GET = "GET";
    
    /**
     * post请求
     */
    public static final String POST = "POST";
    
    /**
     * 服务器返回的set-cookie指令
     */
    public static final String SET_COOKIE = "SET-COOKIE";
    
    /**
     * header中的cookie字段的key
     */
    public static final String COOKIE = "Cookie";
    
    /**
     * 系统换行符
     */
    public static final String LINE_SEPARATOR = "line.separator";
    
    /**
     * http访问地址
     */
    public String url;
    
    /**
     * 请求回调接口（用于获取响应头，提取保存的cookie信息）
     */
    private Callback callback;
    
    /**
     * 是否返回byte数组（用于网络资源文件获取，默认为false，返回json文本）
     */
    private boolean isByteArray = false;
    
    /**
     * 是否异步加载（开启新线程访问）
     */
    private boolean async = false;
    
    /**
     * 编码格式
     */
    private String encode;
    
    /**
     * 请求参数
     */
    private Map<String, String> data = new HashMap<String, String>();
    
    /**
     * 请求头部
     */
    private Map<String, String> header = new HashMap<String, String>();
    
    /**
     * request cookies
     */
    private Map<String, String> cookies = new HashMap<String, String>();
    
    /**
     * 私有构造方法
     */
    private HttpUtil(){}
    
    /**
     * 获取示例（非单例模式）
     * @return
     */
    public static HttpUtil getInstance(){
        return new HttpUtil();
    }
    
    /**
     * 初始化访问地址url
     * @param url
     * @return
     */
    public HttpUtil connect(String url){
        return connect(url, DEFAULT_ENCODE);
    }
    
    public HttpUtil connect(String url, String encode){
        this.url = url;
        this.encode = encode;
        return this;
    }
    
    /**
     * 设置读取方式为stream（默认是文本）
     * @return
     */
    public HttpUtil byteArray(){
        isByteArray = true;
        return this;
    }
    
    /**
     * 设置http访问方式，采用异步加载
     * @return
     */
    public HttpUtil async(){
        async = true;
        return this;
    }
    
    /**
     * 设置请求参数，注意格式化编码
     * @param key
     * @param value
     * @return
     */
    public HttpUtil data(String key, String value){
        data.put(key, value);
        return this;
    }
    
    /**
     * 设置请求头部
     * @param key
     * @param value
     * @return
     */
    public HttpUtil header(String key, String value){
        header.put(key, value);
        return this;
    }
    
    /**
     * 设置请求cookie
     * @param key
     * @param value
     * @return
     */
    public HttpUtil cookie(String key, String value){
        cookies.put(key, value);
        return this;
    }

    /**
     * 设置请求响应接口
     * @param callback
     * @return
     */
    public HttpUtil callBack(Callback callback){
        this.callback = callback;
        return this;
    }
    
    /**
     * 发起get请求，返回响应数据
     * @return
     */
    public Object get(){
        return getResponse(GET);
    }
    
    /**
     * 发起post请求，返回相应数据
     * @return
     */
    public Object post(){
        return getResponse(POST);
    }
    
    /**
     * 获取服务端响应
     * @param method
     * @return
     */
    public Object getResponse(final String method){
        if (async) {
            // 异步加载
            new Thread(new Runnable() {
                
                @Override
                public void run() {
                    doHttp(method);
                }
            }).start();
        } else {
            return doHttp(method);
        }
        return null;
    }
    
    /**
     * 具体http交互实现方法
     * @param method
     * @return
     */
    public Object doHttp(String method){
        HttpURLConnection connection = null;
        try {
            if (GET.equals(method) && !data.isEmpty()) {
                // get请求拼接字符串
                url += "?" + getParamter(data);
            }
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod(method);
            // 设置header
            if (!header.isEmpty()) {
                for (Entry<String, String> entry : header.entrySet()) {
                    connection.addRequestProperty(entry.getKey(), entry.getValue());
                }
            }
            // 设置cookies
            if (!cookies.isEmpty()) {
                connection.addRequestProperty(COOKIE, getCookies(cookies));
            }
            if (POST.equals(method) && !data.isEmpty()) {
                // 传参不为空且访问方式为post才进行赋值
                connection.setDoOutput(true);
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(), encode));
                writer.write(getParamter(data));
                writer.flush();
                writer.close();
            }
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                if (isByteArray) {
                    byte[] bytes = getBytes(connection.getInputStream());
                    // 获取资源下载stream流
                    if (callback != null) {
                        callback.success(connection, bytes);
                    }
                    return bytes;
                } else {
                    // 通常获取json文本
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), encode));
                    String line;
                    // 采用builder
                    StringBuilder builder = new StringBuilder();
                    while ((line = reader.readLine()) != null) {
                        builder.append(line);
                        builder.append(System.getProperty(LINE_SEPARATOR));
                    }
                    if (callback != null) {
                        callback.success(connection, builder.toString());
                    }
                    reader.close();
                    return builder.toString();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (callback != null) {
                callback.failure(e);
            }
            return e;
            
        } finally{
            if (connection != null) {
                connection.disconnect();
            }
        }
        return null;
    }
    
    /**
     * 将封装的map参数转换为字符串，以便写入输出流中
     */
    public String getParamter(Map<String, String> map){
        return convertMapToStr(map, "=", "&");
    }
    
    /**
     * 将封装的map参数转换为字符串，以便写入header中
     */
    public String getCookies(Map<String, String> map){
        return convertMapToStr(map, "=", ";");
    }
    
    /**
     * 变换map为string类型，指定字段分割符
     * @param map
     * @param firstSeparator
     * @param secondSeparator
     * @return
     */
    public String convertMapToStr(Map<String, String> map, String firstSeparator, String secondSeparator){
        if (map.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        // encode下字符串，避免参数含有特殊字符
        try {
            for (Entry<String, String> entry : map.entrySet()) {
                builder.append(URLEncoder.encode(entry.getKey(), encode));
                builder.append(firstSeparator);
                builder.append(URLEncoder.encode(entry.getValue(), encode));
                builder.append(secondSeparator);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        // 去掉最后一个&
        if (builder.length() > 0) {
            return builder.substring(0, builder.length() - 1);
        }
        return builder.toString();
    }

    /**
     * 通过指定的key值从响应头header获取对应的值
     * @param connection
     * @param name
     * @return
     */
    public static HttpCookie getCookieByName(HttpURLConnection connection, String name){
        HttpCookie cookie = null;
        // 获取header信息，结构为Map<String, List<String>>
        Map<String, List<String>> map = connection.getHeaderFields();
        for (Entry<String, List<String>> entry : map.entrySet()) {
            // 遍历map，获取指定的property("set-cookie")
            if (SET_COOKIE.equalsIgnoreCase(entry.getKey())) {
                // 获取当下的list
                List<String> values = entry.getValue();
                for (String value : values) {
                    if (value.contains(name)) {
                        // 若存在该key，则通过分号;分离成数组
                        String[] temps = value.split(";");
                        for (String str : temps) {
                            if (str.contains(name)) {
                                String cookieVal = str.split("=")[1];
                                cookie = new HttpCookie(name, cookieVal);
                                break;
                            }
                        }
                    }
                }
            }
        }
        return cookie;
    }

    /**
     * 根据InputStream获取byte数组
     */
    public static byte[] getBytes(InputStream is){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            byte[] buffer = new byte[1024]; // 用数据装
            int len;
            while ((len = is.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            baos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 关闭流一定要记得。
        return baos.toByteArray();
    }
    
    /**
     * http请求接口回调接口
     */
    interface Callback{
        
        /**
         * 主要用于多线程回调
         * @param connection 用于获取响应头cookie
         * @param response 可能是json，亦或资源字节数组byte[]
         */
        void success(HttpURLConnection connection, Object response);
        
        /**
         * 访问失败回调接口
         * @param e 异常信息
         */
        void failure(Exception e);
    }
}