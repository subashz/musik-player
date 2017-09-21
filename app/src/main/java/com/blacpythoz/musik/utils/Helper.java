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
        return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,dp,resources.getDisplayMetrics());
    }

}
