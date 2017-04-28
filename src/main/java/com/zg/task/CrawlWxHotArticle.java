package com.zg.task;

import com.alibaba.fastjson.JSON;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.zg.vo.WxHotArticle;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * Created by guang.zhang on 2017/4/27.
 */
@Component
public class CrawlWxHotArticle {

    private final String redisKey = "WxNew";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    private RedisTemplate<Object,Object> redisTemplate;

    private final String userAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36";

    @Scheduled(fixedRate = 1000 * 60 * 10, initialDelay = 1000)
    public void crawl() {

        logger.info("start crawl");
        WebClient webClient = null;
        try {
            webClient = new WebClient();
            List<WxHotArticle> wxHotArticles = new ArrayList<>();
            Document doc = Jsoup.connect("http://weixin.sogou.com/").userAgent(userAgent).referrer("http://weixin.sogou.com/").get();
            Element topwordsE = doc.getElementById("topwords");
            Elements wordEs = topwordsE.getElementsByTag("a");
            Thread.sleep(2000L);
            for (Element wordE : wordEs) {
                WxHotArticle wxHotArticle = new WxHotArticle();
                String topword = wordE.text();
                wxHotArticle.setHotword(topword);
                String searchUrl = wordE.attr("href");

                //Document searchDoc = Jsoup.connect(searchUrl).userAgent(userAgent).referrer("http://weixin.sogou.com/").get();
                HtmlPage page = webClient.getPage(searchUrl);
                Document searchDoc = Jsoup.parse(page.asXml());

                Element newsListE = searchDoc.getElementsByClass("news-list").first();
                Element linkE = newsListE.select("a").first();
                wxHotArticle.setLink(linkE.attr("href"));
                wxHotArticle.setTitle(linkE.text());
                if (wxHotArticle.getTitle().equals("")){
                    System.out.println(linkE.toString());
                    Document articleDoc = Jsoup.connect(wxHotArticle.getLink()).userAgent(userAgent).get();
                    Element h2E = articleDoc.select("h2").first();
                    wxHotArticle.setTitle(h2E.text());
                }
                Element imgE = newsListE.select("img").first();
                wxHotArticle.setImgSrc(imgE.attr("src"));
                System.out.println(JSON.toJSONString(wxHotArticle));
                wxHotArticles.add(wxHotArticle);
                Thread.sleep(2000L);
            }
            redisTemplate.opsForValue().set(redisKey,wxHotArticles,3 , TimeUnit.DAYS);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if (webClient != null) {
                webClient.close();
            }
        }
    }

    private Map<String, String> parseCookie() {
        String cookieStr = "CXID=2E84D706C328DDF08391E142F28284DB; IPLOC=CN5101; SUV=1493277882308373; ABTEST=0|1493277890|v1; SNUID=F1DE5F538D88C0AE57BA73718E67231A; weixinIndexVisited=1; JSESSIONID=aaa9t4qYYKPB47yYSfFSv; sct=1; PHPSESSID=bf8i33gjj4fnkfnimlqg5220g1; SUIR=F1DE5F538D88C0AE57BA73718E67231A; ad=jZllllllll2YMNUslllllV6xh2GlllllTHVFKlllll9lllllpCxlw@@@@@@@@@@@; SUID=7C53D1DE5E68860A58F9D52F00036D7B; seccodeRight=success; successCount=1|Thu, 27 Apr 2017 11:08:19 GMT";
        Map<String, String> cookieMap = new HashMap<>();
        for(String cookie: cookieStr.split(";")) {
            String[] kv = cookie.trim().split("=");
            cookieMap.put(kv[0],kv[1]);
        }
        return cookieMap;
    }

}
