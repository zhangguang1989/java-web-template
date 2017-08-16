package com.zg.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.NotBlank;

import java.io.Serializable;

/**
 * Created by guang.zhang on 2017/8/16.
 */
@ApiModel
public class CompTxtVO implements Serializable {

    @ApiModelProperty(value = "中文文本一")
    @NotBlank
    private String s1;

    @ApiModelProperty(value = "中文文本二")
    @NotBlank
    private String s2;

    public String getS1() {
        return s1;
    }

    public void setS1(String s1) {
        this.s1 = s1;
    }

    public String getS2() {
        return s2;
    }

    public void setS2(String s2) {
        this.s2 = s2;
    }
}
