package com.ruoyi.project.monitor.job.util;

import com.ruoyi.common.utils.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * @Author hyr
 * @Description
 * @Date create in 2023/6/1 14:17
 */
public class DateTimeUtil {

    public static String utcToDate(String time) {
        if (StringUtils.isEmpty(time)) {
            return "";
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = null;
        try {
            date = dateFormat.parse(time);
        }
        catch (ParseException e) {
            e.printStackTrace();
        }
        SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat2.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        String beijingTime = dateFormat2.format(date);
        // System.out.println(beijingTime);
        return beijingTime;
    }
}
