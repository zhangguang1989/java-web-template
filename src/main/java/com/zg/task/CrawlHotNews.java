package com.zg.task;

import com.alibaba.fastjson.JSON;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.BrowserVersionFeatures;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.UrlUtils;
import com.hankcs.hanlp.HanLP;
import com.zg.util.DateUtils;
import com.zg.vo.HotArticle;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by guang.zhang on 2017/5/2.
 */
@Component
public class CrawlHotNews {

    private final String toutiaoNewsRedisKeyPrefix = "ToutiaoNews_";

    private final String weixinNewsRedisKeyPrefix = "WeixinNews_";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    private RedisTemplate<Object,Object> redisTemplate;

    //@Scheduled(fixedRate = 1000 * 60 * 10, initialDelay = 1000)
    public void crawlToutiaoNews() {
        logger.info("start crawl baidu hotword then toutiao news");
        try {
            String redisKey = toutiaoNewsRedisKeyPrefix + DateUtils.formateToday("yyyyMM");
            List<HotArticle> hotArticles = new ArrayList<>();
            Document doc = Jsoup.connect("http://top.baidu.com/buzz?b=1&fr=topindex").get();
            Element table = doc.getElementsByClass("list-table").first();
            Elements wordEs = table.select("a.list-title");
            Thread.sleep(2000L);
            for (Element wordE : wordEs) {

                HotArticle hotArticle = new HotArticle();
                hotArticle.setHotword(wordE.text());

                String searchUrl = "https://m.toutiao.com/search/?keyword=" + wordE.text();

                Document searchDoc = Jsoup.connect(searchUrl).get();

                Elements sections = searchDoc.select("section");
                for (Element section : sections){
                    Element imgE = section.select("img").first();
                    if (imgE == null){
                        continue;
                    }
                    hotArticle.setImgSrc(imgE.attr("src"));
                    Element linkE = section.select("a").first();
                    hotArticle.setLink("http://m.toutiao.com" + linkE.attr("href"));
                    Element titleE = section.select("h3").first();
                    hotArticle.setTitle(titleE.text());
                    break;
                }

                System.out.println(JSON.toJSONString(hotArticle));
                hotArticles.add(hotArticle);
                redisTemplate.opsForHash().put(redisKey,hotArticle.getHotword(),hotArticle);
                Thread.sleep(2000L);
            }
            redisTemplate.expire(redisKey,1,TimeUnit.DAYS);
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }
    }

    //@Scheduled(fixedRate = 1000 * 60 * 10, initialDelay = 1000)
    public void crawlWeixinNews() {

        logger.info("start crawl baidu hotword then weixin news");

        try {

            String redisKey = weixinNewsRedisKeyPrefix + DateUtils.formateToday("yyyyMMdd");

            Long timeDiffOfWeek = 3600 * 24 * 7 * 1000L;

            WebClient webClient = new WebClient(BrowserVersion.CHROME);
            List<HotArticle> hotArticles = new ArrayList<>();
            Document doc = Jsoup.connect("http://top.baidu.com/buzz?b=1&fr=topindex").get();
            Element table = doc.getElementsByClass("list-table").first();
            Elements wordEs = table.select("a.list-title");
            Thread.sleep(2000L);
            for (Element wordE : wordEs) {

                HotArticle hotArticle = new HotArticle();
                hotArticle.setHotword(wordE.text());

                String searchUrl = "http://weixin.sogou.com/weixin?type=2&ie=utf8&query=" + wordE.text();
                URL url = UrlUtils.toUrlUnsafe(searchUrl);
                url = UrlUtils.encodeUrl(url, false, Charset.forName("UTF-8"));
                HtmlPage page = webClient.getPage(url);
                Document searchDoc = Jsoup.parse(page.asXml());
                Element newsListE = searchDoc.select(".news-list").first();
                Element li = newsListE.select("li").first();

                Element timeDiv = li.select("div.s-p").first();
                String timeStr = timeDiv.attr("t");
                Long publishTime = Long.valueOf(timeStr);
                if (timeStr.length() == 10) {
                    publishTime *= 1000;
                }
                Long nowTime = new Date().getTime();
                if (nowTime - publishTime > timeDiffOfWeek) {
                    continue;
                }

                Element linkE = li.select("div.txt-box a").first();
                hotArticle.setLink(linkE.attr("href"));
                hotArticle.setTitle(linkE.text());

                Element imgE = li.select("img").first();
                if (imgE != null) {
                    hotArticle.setImgSrc(imgE.attr("src"));
                }

                System.out.println(JSON.toJSONString(hotArticle));
                hotArticles.add(hotArticle);
                redisTemplate.opsForHash().put(redisKey,hotArticle.getHotword(),hotArticle);
                Thread.sleep(2000L);
            }
            redisTemplate.expire(redisKey,1,TimeUnit.DAYS);
        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }

    }
}
