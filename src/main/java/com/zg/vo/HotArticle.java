package com.zg.vo;

import java.io.Serializable;

/**
 * Created by guang.zhang on 2017/4/27.
 */
public class HotArticle implements Serializable {

     private String hotword;
     private String title;
     private String link;
     private String imgSrc;

    public String getHotword() {
        return hotword;
    }

    public void setHotword(String hotword) {
        this.hotword = hotword;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getImgSrc() {
        return imgSrc;
    }

    public void setImgSrc(String imgSrc) {
        this.imgSrc = imgSrc;
    }
}
