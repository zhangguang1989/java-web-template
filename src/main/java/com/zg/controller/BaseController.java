package com.zg.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zg.HttpUtils;
import com.zg.vo.ResponseResult;
import com.zg.vo.HotArticle;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.io.*;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by guang.zhang on 2017/4/12.
 */
@Controller
@RequestMapping("/list")
public class BaseController {

    private final String url = "http://api.juheapi.com/japi/toh";
    private final String key = "21700811f5716921964220b9c015296d";

    private final String redisKeyPrefix = "TodayInHistory_";

    private final String toutiaoNewsRedisKey = "ToutiaoNews";


    @Resource
    private RedisTemplate<Object,Object> redisTemplate;

    @RequestMapping("/todayInHistory")
    @ResponseBody
    Object todayInHistory() throws IOException {

        Calendar today = Calendar.getInstance();
        int month = today.get(Calendar.MONTH);
        int day = today.get(Calendar.DAY_OF_MONTH);
        String redisKey = redisKeyPrefix + String.format("%02d%02d",month,day);

        JSONArray cachedResult = (JSONArray) redisTemplate.opsForValue().get(redisKey);

        if (cachedResult != null){
            return cachedResult;
        }

        Map<String,Object> params = new HashMap<>();
        params.put("v","1.0");
        params.put("key",key);
        params.put("month",month);
        params.put("day",day);
        String urlWithParams = url + "?" + urlencode(params);
        String respStr = null;
        try {
            respStr = HttpUtils.get(urlWithParams);
        } catch (Exception e) {
            e.printStackTrace();
        }
        JSONObject resp = JSON.parseObject(respStr);
        if ((int)resp.get("error_code") == 0){
            JSONArray result = resp.getJSONArray("result");
            redisTemplate.opsForValue().set(redisKey,result,3 , TimeUnit.DAYS);
            return result;
        }
        return new ResponseResult(resp.get("reason"));
    }

    @RequestMapping("/hotNews")
    @ResponseBody
    Object hotNews() throws IOException {
        List<HotArticle> cachedResult = (List<HotArticle>) redisTemplate.opsForValue().get(toutiaoNewsRedisKey);
        if (cachedResult != null){
            return cachedResult;
        }
        return "无数据";
    }

    //将map型转为请求参数型
    public static String urlencode(Map<String,Object>data) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry i : data.entrySet()) {
            try {
                sb.append(i.getKey()).append("=").append(URLEncoder.encode(i.getValue()+"","UTF-8")).append("&");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
}
