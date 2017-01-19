package com.micronet.mcontrol;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by brigham.diaz on 8/29/2016.
 */
public class Utils {
    public static String formatDate(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }

    public static String formatDateShort(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }

    public static String formatDateShort(long time) {
        return formatDateShort(new Date(time));
    }

    public static String formatDate(long time) {
        return formatDate(new Date(time));
    }

}
