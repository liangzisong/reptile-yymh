package com.liangzisong.reptileyymh.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;

import com.liangzisong.reptileyymh.SpringUtils;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.internal.connection.RouteException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by qhong on 2018/7/3 16:55
 **/
public class OkHttpUtil {

    private static final Logger logger = LoggerFactory.getLogger(OkHttpUtil.class);

    /**
     * 根据map获取get请求参数
     *
     * @param queries
     * @return
     */
    public static StringBuffer getQueryString(String url, Map<String, String> queries) {
        StringBuffer sb = new StringBuffer(url);
        if (queries != null && queries.keySet().size() > 0) {
            boolean firstFlag = true;
            Iterator iterator = queries.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry<String, String>) iterator.next();
                if (firstFlag) {
                    sb.append("?" + entry.getKey() + "=" + entry.getValue());
                    firstFlag = false;
                } else {
                    sb.append("&" + entry.getKey() + "=" + entry.getValue());
                }
            }
        }
        return sb;
    }

    /**
     * 调用okhttp的newCall方法
     *
     * @param request
     * @return
     */
    private static String execNewCall(Request request) {
        Response response = null;
        try {
            OkHttpClient okHttpClient = SpringUtils.getBean(OkHttpClient.class);
            response = okHttpClient.newCall(request).execute();
            int status = response.code();
            logger.info("fan回状态{}]", status);
            if (response.isSuccessful()) {
                return response.body().string();
            }
        } catch (Exception e) {
            logger.error("okhttp3 put error >> ex = {}", ExceptionUtils.getStackTrace(e));
        } finally {
            if (response != null) {
                response.close();
            }
        }
        return "";
    }

    /**
     * get
     *
     * @param url     请求的url
     * @param queries 请求的参数，在浏览器？后面的数据，没有可以传null
     * @return
     */
    public static String get(String url, Map<String, String> queries) {
        StringBuffer sb = getQueryString(url, queries);
        Request request = new Request.Builder()
                .url(sb.toString())
                .build();
        return execNewCall(request);
    }

    /**
     * post
     *
     * @param url    请求的url
     * @param params post form 提交的参数
     * @return
     */
    public static String postFormParams(String url, Map<String, String> params) {
        FormBody.Builder builder = new FormBody.Builder();
        //添加参数
        if (params != null && params.keySet().size() > 0) {
            for (String key : params.keySet()) {
                builder.add(key, params.get(key));
            }
        }
        Request request = new Request.Builder()
                .url(url)
                .post(builder.build())
                .build();
        return execNewCall(request);
    }


    /**
     * Post请求发送JSON数据....{"name":"zhangsan","pwd":"123456"}
     * 参数一：请求Url
     * 参数二：请求的JSON
     * 参数三：请求回调
     */
    public static String postJsonParams(String url, String jsonParams) {
//        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonParams);
//        Request request = new Request.Builder()
//                .url(url)
//                .header("Cookie","inviteriid=S0168; ciu_key=1f184897-e53d-4a0e-bad4-4a697fc605c4$39.83.152.103; ticket=117b0342-1ba7-41d5-81fe-e652bb362f34; JSESSIONID=1uuto1t21ep521f6uarrj63d0")
//                .post(requestBody)
//                .build();
//        return execNewCall(request);
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(5000).setConnectionRequestTimeout(5000)
                .setSocketTimeout(5000).build();
        httpPost.setConfig(requestConfig);
        httpPost.addHeader("Content-type", "application/json; charset=utf-8");
        httpPost.setHeader("Accept", "application/json");
        // 传入的header参数
        httpPost.setHeader("Cookie", "inviteriid=S0168; ciu_key=1f184897-e53d-4a0e-bad4-4a697fc605c4$39.83.152.103; ticket=117b0342-1ba7-41d5-81fe-e652bb362f34; JSESSIONID=1uuto1t21ep521f6uarrj63d0");
        httpPost.setEntity(new StringEntity("", Charset.forName("UTF-8")));
        CloseableHttpResponse res = null;
        try {
            res = httpClient.execute(httpPost);
          return EntityUtils.toString(res.getEntity());
        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
        } finally {
            try {
                if (res != null) {
                    res.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * Post请求发送xml数据....
     * 参数一：请求Url
     * 参数二：请求的xmlString
     * 参数三：请求回调
     */
    public static String postXmlParams(String url, String xml) {
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/xml; charset=utf-8"), xml);
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        return execNewCall(request);
    }

    public static void main(String[] args) {
        dowloadImage("https://image2.dayi58.com/04c4a04ace0b65c011b6ece3ece82bdb/601767e8/b1507/c233405/6a4b5d38-b776-49a0-9944-196b0724e452.png"
                , "/media/liangzisong/liang/Adobe/", "aa.", null);
    }

    public static void dowloadImage(String urlStr, String filePath, String name,FileOutputStream outputStream) {
        URL url = null;
        try {
            url = new URL(urlStr);
        } catch (MalformedURLException e) {
            if (outputStream != null) {
                try {
                    outputStream.write((urlStr+"\r\n").getBytes(StandardCharsets.UTF_8));
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }finally {
                    try {
                        outputStream.flush();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
            }
            e.printStackTrace();
        }
        URLConnection uc = null;
        try {
            if (url == null) {
                return;
            }
            uc = url.openConnection();
            uc.addRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.143 Safari/537.36");
            uc.addRequestProperty("Referer", "https://m.yymh8807.com/");
            uc.addRequestProperty("Sec-Fetch-Site", "cross-site");
            uc.addRequestProperty("Sec-Fetch-Mode","no-cors");
//            uc.addRequestProperty("Host","image2.dayi58.com");
//            uc.addRequestProperty("sec-ch-ua","\"Chromium\";v=\"88\", \"Google Chrome\";v=\"88\", \";Not A Brand\";v=\"99\"");
//            uc.addRequestProperty("sec-ch-ua-mobile","?0");
//            uc.addRequestProperty("Sec-Fetch-Dest","image");
//            uc.addRequestProperty("Cookie","ciu_key=e8320226-e077-4e30-99e8-9328e54ef2a4$222.175.185.58; ticket=117b0342-1ba7-41d5-81fe-e652bb362f34; JSESSIONID=11z4gjym7v5d21s177aqdwa6sk");
//            uc.addRequestProperty("referrer","no-referrer");
//            uc.addRequestProperty("Sec-Fetch-Dest","image");
        } catch (IOException e) {
            if (outputStream != null) {
                try {
                    outputStream.write((urlStr+"\r\n").getBytes(StandardCharsets.UTF_8));
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }finally {
                    try {
                        outputStream.flush();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
            }
            e.printStackTrace();
        }
        InputStream inputStream = null;
        FileOutputStream out = null;
        try {
            if (uc == null) {
                return;
            }
            inputStream = uc.getInputStream();
            String fileType = urlStr.substring(urlStr.lastIndexOf(".") + 1);
            out = new FileOutputStream(filePath + File.separator + name + fileType);
            int j = 0;
            while ((j = inputStream.read()) != -1) {
                out.write(j);
            }
        } catch (IOException e) {
            if (outputStream != null) {
                try {
                    outputStream.write((urlStr+"\r\n").getBytes(StandardCharsets.UTF_8));
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }finally {
                    try {
                        outputStream.flush();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
            }
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

}
