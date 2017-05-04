package com.zg.task;

import com.alibaba.fastjson.JSON;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by guang.zhang on 2017/5/2.
 */
@Component
public class CrawlHotNews {

    private final String toutiaoNewsRedisKeyPrefix = "ToutiaoNews_";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    private RedisTemplate<Object,Object> redisTemplate;

    @Scheduled(fixedRate = 1000 * 60 * 10, initialDelay = 1000)
    public void crawl() {
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
            redisTemplate.expire(redisKey,33,TimeUnit.DAYS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        String document = "印尼146岁老人去世，这名老人名叫Saparman Sodimejo，也被称呼为Mbah Ghoto。他的身份证显示，老人生于1870年12月31日。妹以及他的4位妻子，其中最长寿的也在1988年去世了。当然，老人的所有孩子也都没有活过他。如果老人的出生信息被证是真实的，那么146岁的他就要比目前被证实为世界年龄最大的122岁法国老太Jeanne Calment还要老。目前科学家也在质疑Ghoto老人的年龄，因为根据去年的一份研究报告，人类生命的自然生长极限应该不超过125年。然而Ghoto老人的孙子表示，老人早在24年前就准备了墓碑。他活到122岁之后，随时都准备好了死亡的到来。老人去年还在为自己的逝世做准备，但这一天迟迟未到。";
        List<String> sentenceList = HanLP.extractSummary(document, 3);
        System.out.println(sentenceList);

    }
}
