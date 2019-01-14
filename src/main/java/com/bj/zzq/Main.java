package com.bj.zzq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
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
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import sun.awt.SunHints;

import javax.swing.plaf.SliderUI;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 取消预约后，该号不会马上放出来，fuck~
 * fuck,验证码一天发送次数不能超过10次，玩完~
 *
 * @Author: zhaozhiqiang
 * @Date: 2019/1/8
 * @Description:
 */
public class Main {
    private static String cookie = "";
    private static HashMap<String, String> orderParams = new HashMap<String, String>();
    private static String domain = "http://www.bjguahao.gov.cn";
    private static String loginUrl = "http://www.bjguahao.gov.cn/quicklogin.htm";

    static {
        //默认值
        orderParams.put("hospitalType", "1");
        orderParams.put("hospitalCardId", "");
        orderParams.put("medicareCardId", "");

        //固定参数
        orderParams.put("childrenBirthday", "");
        orderParams.put("dlRegType", "-1");
        orderParams.put("dlMajorId", "");
        orderParams.put("mapDoctorId", "");
    }

    public static void main(String[] args) throws IOException, URISyntaxException {
        Main main = new Main();
        String username = "18511914092";
        String password = "zzq798828932";
        //医院名称，全称
        String hospitalName = "北京大学第三医院";
        //科室名称，全称
        String departmentName = "骨科门诊";
        //预约日期 yyyy-MM-dd
        String date = "2019-01-22";
        //上午还是下午 1-上午，2-下午
        String timeSlot = "1";
        //医生名称,包括 普通门诊、副主任医师、主任医师、真正姓名
        String doctorName = "副主任医师";
        //医生职位,包括 普通门诊、副主任医师、主任医师、知名专家
        String doctorPosition = "知名专家";
        //病人名称
        String patientName = "赵志强";
        //报销类型 1-医保卡
        String reimbursementType = "1";
        //登录账号
        main.login(username, password);
        main.sendValidateCode();
        main.doOrder(hospitalName, departmentName, timeSlot, date, doctorName, doctorPosition, patientName, reimbursementType);
    }

    public static String base64ToString(String str) {
        try {
            str = Base64.getEncoder().encodeToString(str.getBytes("utf-8"));
        } catch (UnsupportedEncodingException e) {

        }
        return str;
    }

    //请求工具
    public static String doHttp(String method, String url, HashMap<String, String> headers, HashMap<String, String> params) throws URISyntaxException, IOException {
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

        URI uri = builder.build();

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

        // 执行请求
        CloseableHttpResponse response = httpclient.execute(httpRequestBase);
        String result = EntityUtils.toString(response.getEntity(), "UTF-8");

        // 如果是登录请求，获取cookie
        if (loginUrl.equals(url)) {
            Header[] allHeaders = response.getAllHeaders();
            for (Header header : allHeaders) {
                String name = header.getName();
                if ("Set-Cookie".equals(name)) {
                    String value = header.getValue();
                    value = value.substring(0, value.indexOf(";") + 1);
                    cookie += " " + value;
                }
            }
            cookie = cookie.substring(1);
        }

        return result;

    }

    /**
     * 登录
     */
    public void login(String username, String password) throws IOException, URISyntaxException {
        //登录地址
        String loginUrl = "http://www.bjguahao.gov.cn/quicklogin.htm";

        //参数
        HashMap<String, String> params = new HashMap();
        params.put("mobileNo", base64ToString(username));
        params.put("password", base64ToString(password));

        //固定参数
        params.put("yzm", "");
        params.put("isAjax", "true");

        //返回内容
        String result = doHttp("post", loginUrl, null, params);
        JSONObject jsonObject = (JSONObject) JSONObject.parse(result);
        Boolean hasError = jsonObject.getBoolean("hasError");
        Integer code = jsonObject.getInteger("code");
        if (hasError == false && code == 200) {
            System.out.println("-------------------");
            System.out.println("     登录成功");
            System.out.println("-------------------");
        }
    }

    /**
     * 发送验证码
     */
    public void sendValidateCode() throws IOException, URISyntaxException {
        String url = "http://www.bjguahao.gov.cn/v/sendorder.htm";
        String method = "POST";
        String result = doHttp(method, url, null, null);
        System.out.println("发送验证码返回结果：" + result);
        System.out.println("请赶紧将验证码输入...");
    }

    /**
     * 预约请求
     * dutySourceId      出诊号
     * hospitalId        医院id
     * departmentId      科室id
     * doctorId          医生id
     * patientId         病人id
     * hospitalCardId    就医卡号
     * medicareCardId    医保卡号
     * reimbursementType 报销类型
     */
    public void order(HashMap<String, String> params) throws IOException, URISyntaxException {
        String orderUrl = "http://www.bjguahao.gov.cn/order/confirmV1.htm";
        String method = "POST";

        //验证码是手动输入
        System.out.println("请输入验证码：");
        Scanner scan = new Scanner(System.in);
        String validateCode = scan.nextLine();
        params.put("smsVerifyCode", validateCode);

        String result = doHttp(method, orderUrl, null, params);
        JSONObject jsonObject = (JSONObject) JSONObject.parse(result);
        Integer code = jsonObject.getInteger("code");
        String orderId = jsonObject.getString("orderId");
        if (code == 1) {
            System.out.println("预约成功,订单号为:" + orderId + "，具体内容请查看手机短信提示");
        } else {
            System.out.println("预约失败：" + result);
        }
    }

    /**
     * 取消预约
     *
     * @param orderId 订单号
     */
    public void cancleOrder(String orderId) throws IOException, URISyntaxException {
        String url = "http://www.bjguahao.gov.cn/order/cel.htm";
        String method = "post";

        HashMap<String, String> params = new HashMap<>();
        params.put("orderId", orderId);
        params.put("hospitalType", "1");//todo:目前暂时全部填1
        params.put("isAjax", "true");
        String result = doHttp(method, url, null, params);
        //{"code":200,"msg":"OK"}
        JSONObject jsonObject = (JSONObject) JSONObject.parse(result);
        Integer code = jsonObject.getInteger("code");
        if (code == 200) {
            System.out.println("取消预约成功！");
        }
    }

    /**
     * @param hospitalName      医院名称
     * @param departmentName    科室名
     * @param timeSlot          时间段
     * @param doctorName        医生姓名
     * @param patientName       病人姓名
     * @param reimbursementType 报销类型 1-医保，其他的后面再弄
     * @throws IOException
     * @throws URISyntaxException
     */
    public void doOrder(String hospitalName, String departmentName, String timeSlot, String orderDate, String doctorName, String doctorPosition, String patientName, String reimbursementType) throws IOException, URISyntaxException {

        //医院id
        String searchUrl = "http://www.bjguahao.gov.cn/hp/search.htm";
        String method = "GET";
        HashMap<String, String> params = new HashMap<>();
        params.put("words", hospitalName);
        String result = doHttp(method, searchUrl, null, params);
        //返回html页面
        Document document = Jsoup.parse(result);
        Elements hospitalLink = document.select("a.yiyuan_co_xzyy");
        String hospitalHref = hospitalLink.get(0).attr("href");
        //example /hp/appoint/1/142.htm
        String[] split = hospitalHref.split("/");
        String hospitalId = split[4].substring(0, split[4].length() - 4);
        orderParams.put("hospitalId", hospitalId);
        //放号时间
        Elements elements1 = document.select("b.yiyuan_telico2");
        Element element = elements1.get(0).parent();
        String outNumTime = element.text();
        System.out.println(hospitalName + " 该医院放号时间为:" + outNumTime);

        //科室id
        String result2 = doHttp("GET", domain + hospitalHref, null, null);
        Document document1 = Jsoup.parse(result2);
        Elements elements = document1.select("a.kfyuks_islogin");
        Iterator<Element> iterator = elements.iterator();
        String departmentHref = "";
        while (iterator.hasNext()) {
            Element next = iterator.next();
            String text = next.text();
            if (departmentName.equals(text)) {
                departmentHref = next.attr("href");
                break;
            }
        }
        String[] split1 = departmentHref.split("/");
        //  /dpt/appoint/12-200004205.htm
        String departmentId = split1[3].substring(split1[3].indexOf("-") + 1, split1[3].indexOf("."));
        orderParams.put("departmentId", departmentId);

        // 查询是否有号
        //hospitalId=142&departmentId=200039608&dutyCode=1&dutyDate=2019-01-17&isAjax=true
        String queryNum = "http://www.bjguahao.gov.cn/dpt/partduty.htm";
        HashMap<String, String> params2 = new HashMap<>();
        params2.put("hospitalId", orderParams.get("hospitalId"));
        params2.put("departmentId", orderParams.get("departmentId"));
        params2.put("dutyCode", timeSlot);//1-上午 2-下午
        params2.put("dutyDate", orderDate);//日期 yyyy-MM-dd
        params2.put("isAjax", "true");

        String result3 = doHttp("POST", queryNum, null, params2);
        if (result3 == null) {
            System.out.println("此时间段没号了，换个时间约？");
            return;
        }
        JSONObject jsonObject = (JSONObject) JSON.parse(result3);
        JSONArray data = (JSONArray) jsonObject.get("data");
        for (int i = 0; i < data.size(); i++) {
            JSONObject o = (JSONObject) data.get(i);
            //剩余号个数
            int remainAvailableNumber = o.getIntValue("remainAvailableNumber");
            if (remainAvailableNumber <= 0) {
                continue;
            }
            String doctorName1 = o.getString("doctorName");
            String doctorTitleName = o.getString("doctorTitleName");
            if (doctorPosition.equals(doctorTitleName)) {
                //约吧，还等啥

                //出诊号
                Integer dutySourceId = o.getInteger("dutySourceId");
                orderParams.put("dutySourceId", String.valueOf(dutySourceId));

                //医生id
                String doctorId = o.getString("doctorId");
                orderParams.put("doctorId", doctorId);

                //病人id
                // /order/confirm/142-200039608-201147114-59981348.htm
                // 从左到右依次为：医院id-科室id-医生id-出诊号
                String orderHtmlUrl = domain + "/order/confirm/" + hospitalId + "-" + departmentId + "-" + doctorId + "-" + dutySourceId + ".htm";
                String result4 = doHttp("GET", orderHtmlUrl, null, null);
                Document document2 = Jsoup.parse(result4);
                Elements elements2 = document2.select("input[name='hzr']");
                Iterator<Element> iterator1 = elements2.iterator();
                while (iterator1.hasNext()) {
                    Element next = iterator1.next();
                    String text = next.text();
                    if (text.contains(patientName)) {
                        //就是这个病人要抢号
                        String patientId = next.attr("value");
                        orderParams.put("patientId", patientId);
                        orderParams.put("reimbursementType", reimbursementType);
                        order(orderParams);
                        break;
                    }
                }
            }
        }
    }


}
