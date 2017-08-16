package com.zg.controller;
import com.zg.service.TextService;
import com.zg.util.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


/**
 * Created by guang.zhang on 2017/4/12.
 */
@Controller
@RequestMapping("/")
public class BaseController {


    @Autowired
    private TextService textService;

    @GetMapping("/compare")
    @ResponseBody
    Object compare(@RequestParam String s1, @RequestParam String s2) {
        return textService.compare(s1,s2);
    }

    @GetMapping("/strLen")
    @ResponseBody
    Object strLen(@RequestParam String s) {
        return s.length();
    }

    @GetMapping("/port")
    @ResponseBody
    Object port() {
        return RandomUtils.randPort();
    }

    @GetMapping("/randomStr")
    @ResponseBody
    Object randomStr(@RequestParam Integer length) {
        return RandomUtils.randStr(length);
    }

}
