package com.bj.zzq;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

/**
 * @Author: zhaozhiqiang
 * @Date: 2019/1/8
 * @Description:
 */
public class Main {
    public static void main(String[] args) throws URISyntaxException, IOException {
        //默认http客户端，毫秒级
        CloseableHttpClient httpclient = HttpClients.createDefault();

        //生成url
        URIBuilder builder = new URIBuilder("");

        //添加参数
        HashMap<String, String> param = new HashMap();
        for (String key : param.keySet()) {
            builder.addParameter(key, param.get(key));
        }

        URI uri = builder.build();
        // 创建http GET请求
        HttpGet httpGet = new HttpGet(uri);

        //添加头
        httpGet.addHeader("aa","bb");

        // 执行请求
        CloseableHttpResponse response = httpclient.execute(httpGet);

        // 判断返回状态是否为200
        if (response.getStatusLine().getStatusCode() == 200) {
            String result = EntityUtils.toString(response.getEntity(), "UTF-8");
        }
    }
}
