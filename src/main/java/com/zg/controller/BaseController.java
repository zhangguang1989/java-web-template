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
@RequestMapping("/base")
public class BaseController {


    @Autowired
    private TextService textService;

    @GetMapping("/compare")
    @ResponseBody
    Object compare(@RequestParam(required = false) String s1, @RequestParam(required = false) String s2) {
        if (s1 == null) {
            s1 = "人工智能的变革主要通过低调的机器学习算法，这需要我们给机器大量的实例，让人工智能去学习如何模仿人类的行为";
        }
        if (s2 == null) {
            s2 = "人工智能的变革需要我们给机器大量的实例,主要通过低调的机器学习算法，让人工智能去学习如何模仿人类的行为.如果参数是正零或负零，那么结果是一样的参数.巧妙地打包安装程序。只需下载安装包，随地把它解压缩";
        }
        return textService.compare(s1,s2);
    }

}
