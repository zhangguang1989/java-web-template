package com.zg.controller;
import com.zg.service.TextService;
import com.zg.util.RandomUtils;
import com.zg.vo.CompTxtVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


/**
 * Created by guang.zhang on 2017/4/12.
 */
@Controller
@RequestMapping("/")
public class BaseController {


    @Autowired
    private TextService textService;

    @PostMapping("/compTxt")
    @ResponseBody
    @ApiOperation(value = "计算中文文本相似度")
    Object compTxt(@RequestBody @Validated CompTxtVO vo) {
        return textService.compare(vo.getS1(),vo.getS2());
    }

    @PostMapping("/strLen")
    @ResponseBody
    @ApiOperation(value = "统计字符串长度")
    Object strLen(@RequestBody String s) {
        return s.length();
    }

    @GetMapping("/port")
    @ResponseBody
    @ApiOperation(value = "获取随机注册端口")
    Object port() {
        return RandomUtils.randPort();
    }

    @GetMapping("/randomStr")
    @ResponseBody
    @ApiOperation(value = "获取随机字符串")
    Object randomStr(@RequestParam Integer length) {
        return RandomUtils.randStr(length);
    }

}
