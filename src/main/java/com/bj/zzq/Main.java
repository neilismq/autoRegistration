package com.bj.zzq;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import sun.awt.SunHints;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.HashMap;

/**
 * @Author: zhaozhiqiang
 * @Date: 2019/1/8
 * @Description:
 */
public class Main {
    private static String cookie = "";

    public static void main(String[] args) {
        Main main = new Main();
        main.login();
        main.sendValidateCode();
    }

    private static String base64ToString(String str) {
        try {
            str = Base64.getEncoder().encodeToString(str.getBytes("utf-8"));
        } catch (UnsupportedEncodingException e) {

        }
        return str;
    }

    //请求工具
    private static CloseableHttpResponse doHttp(String method, String url, HashMap<String, String> headers, HashMap<String, String> params) {
        //默认http客户端，毫秒级
        CloseableHttpClient httpclient = HttpClients.createDefault();

        //生成url
        URIBuilder builder = null;
        try {
            builder = new URIBuilder(url);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        //添加参数
        if (params != null) {
            for (String key : params.keySet()) {
                builder.addParameter(key, params.get(key));
            }
        }


        URI uri = null;
        try {
            uri = builder.build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        // 创建http请求
        HttpRequestBase httpRequestBase = null;
        if ("GET".equalsIgnoreCase(method)) {
            httpRequestBase = new HttpGet(uri);
        } else if ("POST".equalsIgnoreCase(method)) {
            httpRequestBase = new HttpPost(uri);
        }

        //添加header
        if (headers != null) {
            for (String name : headers.keySet()) {
                httpRequestBase.addHeader(name, headers.get(name));
            }
        }
        if(!"".equals(cookie)){
            httpRequestBase.addHeader("Cookie",cookie);
        }

        // 执行请求
        CloseableHttpResponse response = null;
        try {
            response = httpclient.execute(httpRequestBase);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    /**
     * 登录
     */
    private void login() {
        //登录地址
        String loginUrl = "http://www.bjguahao.gov.cn/quicklogin.htm";
        String username = "18511914092";
        String password = "zzq798828932";

        //参数
        HashMap<String, String> params = new HashMap();
        params.put("mobileNo", base64ToString(username));
        params.put("password", base64ToString(password));
        params.put("yzm", "");
        params.put("isAjax", "true");

        CloseableHttpResponse response = doHttp("post", loginUrl, null, params);

        // 判断返回状态是否为200
        if (response.getStatusLine().getStatusCode() == 200) {

            //获取cookie
            Header[] allHeaders = response.getAllHeaders();
            //获取cookie
            for (Header header : allHeaders) {
                String name = header.getName();
                if ("Set-Cookie".equals(name)) {
                    String value = header.getValue();
                    value = value.substring(0, value.indexOf(";") + 1);
                    cookie += " " + value;
                }
            }
            cookie = cookie.substring(1);

            //返回内容
            try {
                String result = EntityUtils.toString(response.getEntity(), "UTF-8");
                JSONObject jsonObject = (JSONObject) JSONObject.parse(result);
                Boolean hasError = jsonObject.getBoolean("hasError");
                Integer code = jsonObject.getInteger("code");
                if (hasError == false && code == 200) {
                    System.out.println("-------------------");
                    System.out.println("     登录成功");
                    System.out.println("-------------------");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 发送验证码
     */
    private void sendValidateCode()  {
        String url="http://www.bjguahao.gov.cn/v/sendorder.htm";
        String method="POST";
        CloseableHttpResponse response = doHttp(method, url, null, null);
        try {
            String result = EntityUtils.toString(response.getEntity(), "UTF-8");
            System.out.println("发送验证码返回结果："+result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
