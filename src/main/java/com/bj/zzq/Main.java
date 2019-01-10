package com.bj.zzq;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
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
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import sun.awt.SunHints;

import javax.swing.plaf.SliderUI;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * 取消预约后，该号不会马上放出来，fuck~
 *
 * @Author: zhaozhiqiang
 * @Date: 2019/1/8
 * @Description:
 */
public class Main {
    private static String cookie = "";
    private static Map orderParams = new HashMap<String, String>();
    private static String domain = "http://www.bjguahao.gov.cn";
    private static String loginUrl = "http://www.bjguahao.gov.cn/quicklogin.htm";

    static {
        orderParams.put("hospitalType", "1");
    }

    public static void main(String[] args) {
        Main main = new Main();
        String username = "18511914092";
        String password = "zzq798828932";
//        //医院名称，全称
//        String hospitalName = "";
//        //科室名称，全称
//        String departmentName = "";
//        //预约日期 yyyy-MM-dd
//        String date = "";
//        //上午还是下午 1-上午，2-下午
//        String amOrPm = "";
//        //医生名称,包括 普通门诊、副主任医师、主任医师、真正姓名
//        String doctorName = "";
//        //医生职位,包括 普通门诊、副主任医师、主任医师、知名专家
//        String doctorPosition = "";

        //登录账号
        main.login(username, password);
        main.hospitalId("北京大学第三医院");
        //main.cancleOrder("100747890");
        //因为验证码发送较慢，先发送
        //main.sendValidateCode();
        //出诊号-北京大学第三医院-中医科-普通门诊（医生）-病人id-就医卡号-医保卡号-报销类型-验证码
//        String[] test = {"59981348", "142", "200039608", "201147114", "230962426", "", "", "1"};
//        //先从控制台获取验证码
//        System.out.print("请输入验证码：");
//        Scanner scan = new Scanner(System.in);
//        String validateCode = scan.nextLine();
//        main.order(test[0], test[1], test[2], test[3], test[4], test[5], test[6], test[7], validateCode);
    }

    public static String base64ToString(String str) {
        try {
            str = Base64.getEncoder().encodeToString(str.getBytes("utf-8"));
        } catch (UnsupportedEncodingException e) {

        }
        return str;
    }

    //请求工具
    public static String doHttp(String method, String url, HashMap<String, String> headers, HashMap<String, String> params) {
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
        if (!"".equals(cookie)) {
            httpRequestBase.addHeader("Cookie", cookie);
        }

        //设置代理,方便查看请求
        HttpHost proxy = new HttpHost("127.0.0.1", 8888, "http");
        RequestConfig config = RequestConfig.custom().setProxy(proxy).build();
        httpRequestBase.setConfig(config);

        String result = "";
        try {
            // 执行请求
            CloseableHttpResponse response = httpclient.execute(httpRequestBase);
            result = EntityUtils.toString(response.getEntity(), "UTF-8");

            if ("http://www.bjguahao.gov.cn/quicklogin.htm".equals(url))
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;

    }

    /**
     * 登录
     */
    public void login(String username, String password) {
        //登录地址
        String loginUrl = "http://www.bjguahao.gov.cn/quicklogin.htm";

        //参数
        HashMap<String, String> params = new HashMap();
        params.put("mobileNo", base64ToString(username));
        params.put("password", base64ToString(password));
        //固定参数
        params.put("yzm", "");
        params.put("isAjax", "true");

        String result = doHttp("post", loginUrl, null, params);

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

    /**
     * 发送验证码
     */
    public void sendValidateCode() {
        String url = "http://www.bjguahao.gov.cn/v/sendorder.htm";
        String method = "POST";
        CloseableHttpResponse response = doHttp(method, url, null, null);
        try {
            String result = EntityUtils.toString(response.getEntity(), "UTF-8");
            System.out.println("发送验证码返回结果：" + result);
            System.out.println("请赶紧将验证码输入...");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 预约请求
     *
     * @param dutySourceId      出诊号
     * @param hospitalId        医院id
     * @param departmentId      科室id
     * @param doctorId          医生id
     * @param patientId         病人id
     * @param hospitalCardId    就医卡号
     * @param medicareCardId    医保卡号
     * @param reimbursementType 报销类型
     * @param smsVerifyCode     验证码
     */
    public void order(String dutySourceId, String hospitalId, String departmentId, String doctorId, String patientId, String hospitalCardId, String medicareCardId, String reimbursementType, String smsVerifyCode) {
        String orderUrl = "http://www.bjguahao.gov.cn/order/confirmV1.htm";
        String method = "POST";
        HashMap<String, String> params = new HashMap<>();
        params.put("dutySourceId", dutySourceId);
        params.put("hospitalId", hospitalId);
        params.put("departmentId", departmentId);
        params.put("doctorId", doctorId);
        params.put("patientId", patientId);
        params.put("hospitalCardId", hospitalCardId);
        params.put("medicareCardId", medicareCardId);
        params.put("reimbursementType", reimbursementType);
        params.put("smsVerifyCode", smsVerifyCode);
        //固定参数
        params.put("childrenBirthday", "");
        params.put("dlRegType", "-1");
        params.put("dlMajorId", "");
        params.put("mapDoctorId", "");
        CloseableHttpResponse response = doHttp(method, orderUrl, null, params);
        try {
            String result = EntityUtils.toString(response.getEntity(), "UTF-8");
            JSONObject jsonObject = (JSONObject) JSONObject.parse(result);
            Integer code = jsonObject.getInteger("code");
            String orderId = jsonObject.getString("orderId");
            if (code == 1) {
                System.out.println("预约成功,订单号为:" + orderId + "，具体内容请查看手机短信提示");
            } else {
                System.out.println("预约失败：" + result);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 取消预约
     *
     * @param orderId 订单号
     */
    public void cancleOrder(String orderId) {
        String url = "http://www.bjguahao.gov.cn/order/cel.htm";
        String method = "post";

        HashMap<String, String> params = new HashMap<>();
        params.put("orderId", orderId);
        params.put("hospitalType", "1");//todo:目前暂时全部填1
        params.put("isAjax", "true");
        CloseableHttpResponse response = doHttp(method, url, null, params);
        String result = null;
        try {
            result = EntityUtils.toString(response.getEntity(), "UTF-8");
            //{"code":200,"msg":"OK"}
            JSONObject jsonObject = (JSONObject) JSONObject.parse(result);
            Integer code = jsonObject.getInteger("code");
            if (code == 200) {
                System.out.println("取消预约成功！");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param hospitalName 医院名称(需要全称)
     */
    public void hospitalId(String hospitalName) throws IOException {

        String searchUrl = "http://www.bjguahao.gov.cn/hp/search.htm";
        String method = "GET";
        HashMap<String, String> params = new HashMap<>();
        params.put("words", hospitalName);

        CloseableHttpResponse response = doHttp(method, searchUrl, null, params);

        String result = EntityUtils.toString(response.getEntity(), "UTF-8");
        //返回的是html页面
        Document document = Jsoup.parse(result);
        Elements hospitalLink = document.select("a.yiyuan_co_xzyy");
        String hospitalHref = hospitalLink.attr("href");
        //example /hp/appoint/1/142.htm
        String[] split = hospitalHref.split("/");
        String hospitalId = split[4].substring(0, split[4].length() - 3);
        orderParams.put("hospitalId", hospitalId);

        CloseableHttpResponse get = doHttp("GET", domain + hospitalHref, null, null);

    }


}
