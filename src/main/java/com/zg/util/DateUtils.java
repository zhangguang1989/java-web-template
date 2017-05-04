package com.zg.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by guang.zhang on 2017/5/4.
 */
public class DateUtils {

    public static String formateToday(String pattern){
        return formate(new Date(), pattern);
    }

    public static String formate(Date date, String pattern){
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
        return dateFormat.format(date);
    }
}
