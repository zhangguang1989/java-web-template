package com.zg.vo;

public interface ResponseCode {

    /**
     * 操作成功
     */
    String SUCCESS = "0";

    /**
     * 系统级方法异常
     */
    String SYSTEM_EXCEPTION = "1";

    /**
     * 业务类异常
     */
    String BUSINESS_EXCEPTION = "2";

    /**
     * 参数异常
     */
    String PARAM_EXCEPTION = "3";

    /**
     * 短时间内重复请求
     */
    String DUPLICATE_REQUEST = "4";

}
