package com.baiyun.asstorage.helper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimeHelper {
    public static String getNowTime() {
        Calendar calendar = Calendar.getInstance();
        Date now = new Date();
        calendar.setTime(now);
        calendar.add(Calendar.HOUR_OF_DAY,0);
        SimpleDateFormat f=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return f.format(calendar.getTime());
    }


}
