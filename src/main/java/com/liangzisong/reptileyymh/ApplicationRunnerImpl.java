package com.liangzisong.reptileyymh;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.liangzisong.reptileyymh.utils.OkHttpUtil;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@Slf4j
public class ApplicationRunnerImpl implements ApplicationRunner {
    @Autowired
    private OkHttpClient okHttpClient;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(15);
        ExecutorService bookFixedThreadPool = Executors.newFixedThreadPool(15);
        ExecutorService coverFixedThreadPool = Executors.newFixedThreadPool(15);
        String booksStr = OkHttpUtil.postJsonParams("https://m.yymh8807.com/query/books?type=cartoon&ranking=wjb&paged=true&size=15&page=2", "");
        JSONArray bootsListJa = JSON.parseObject(booksStr).getJSONObject("content").getJSONArray("list");
        File errUrlFile = new File("/media/liangzisong/liang/tmp/err.url");
        if (errUrlFile.exists()) {
            errUrlFile.delete();
        }
        errUrlFile.createNewFile();
        FileOutputStream outputStream = new FileOutputStream(errUrlFile);
        for (Object bootsListOb : bootsListJa) {
            bookFixedThreadPool.execute(() -> {
                JSONObject bootsListJo = (JSONObject) bootsListOb;
                String bookId = bootsListJo.getString("id");
                String name = bootsListJo.getString("name");
                String pathName = "/media/liangzisong/liang/tmp/" + name;
                File dir = new File(pathName);
                if (!dir.isDirectory()) {
                    dir.mkdir();
                }
                String coverUrl = bootsListJo.getString("coverUrl");
                if (StringUtils.isNotBlank(coverUrl)) {
                    coverFixedThreadPool.execute(() -> {
                        log.info("xieru-cover");
                        OkHttpUtil.dowloadImage(coverUrl, pathName, "cover.", outputStream);
                        log.info("xieru-cover-ok");
                    });
                }

                String extensionUrl = bootsListJo.getString("extensionUrl");
                if (StringUtils.isNotBlank(extensionUrl)) {
                    coverFixedThreadPool.execute(() -> {
                        log.info("xieru-extension");
                        OkHttpUtil.dowloadImage(extensionUrl, pathName, "extension.", outputStream);
                        log.info("xieru-extension-ok");
                    });
                }

//        String bookId= "1489";
//        String jsonParams = OkHttpUtil.postJsonParams("https://m.yymh8807.com/query/book?id="+bookId, "");
//        JSONObject reustJo = JSON.parseObject(jsonParams).getJSONObject("content");
                //
                String bookResult = OkHttpUtil.postJsonParams("https://m.yymh8807.com/query/book/directory?bookId=" + bookId, "");
                JSONArray bookAr = JSONObject.parseObject(bookResult).getJSONArray("content");
                int i = 0;
                for (Object bookOb : bookAr) {
                    JSONObject bookJo = (JSONObject) bookOb;
//                File file = new File(pathName +File.separator+ bookJo.getString("title"));
//                if (!file.isDirectory()) {
//                    file.mkdir();
//                }
                    String InfoReult = OkHttpUtil.postJsonParams("https://m.yymh8807.com/query/book/chapter?bookId=" + bookId + "&chapterId=" + bookJo.getString("id"), "");
                    JSONObject resultObj = JSON.parseObject(InfoReult);
                    JSONArray imageList = resultObj.getJSONObject("content").getJSONArray("imageList");

                    for (Object image : imageList) {
                        i++;
                        JSONObject imageJo = (JSONObject) image;
                        String url = imageJo.getString("url");
//                    String finalPathName = pathName +File.separator+ bookJo.getString("title");
                        int finalI = i;
                        fixedThreadPool.execute(() -> {
                            log.info("xieru-image");
                            OkHttpUtil.dowloadImage(url, pathName, finalI + ".", outputStream);
                            log.info("xieru-image-ok");
                        });
                    }

                }
            });


        }


    }

}
