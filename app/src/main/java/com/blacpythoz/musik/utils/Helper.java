package com.blacpythoz.musik.utils;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;


/**
 * Created by deadsec on 9/18/17.
 */

public class Helper {
    public static int dpToPx(Context context, float dp) {
        Resources resources = context.getResources();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
    }

    public static String toTimeFormat(long millSecond) {
        long duration = millSecond / 1000;
        int hours = (int) duration / 3600;
        int remainder = (int) duration - hours * 3600;
        int minute = remainder / 60;
        remainder = remainder - minute * 60;
        int second = remainder;
        String strMinute = Integer.toString(minute);
        String strSecond = Integer.toString(second);
        String strHour;
        if (strMinute.length()< 2){
            strMinute = "0" + minute;
        }
        if (strSecond.length() < 2) {
            strSecond = "0" + second;
        }
        if (hours == 0) {
            return strMinute + ":" + strSecond;
        } else {
            return hours + ":" + strMinute + ":" + strSecond;
        }
    }

}
