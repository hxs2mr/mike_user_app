package open.lanya.com.ipad_mike.util;

import android.content.res.Resources;
import android.util.DisplayMetrics;

import open.lanya.com.ipad_mike.init.Frame;


/**
 * Created by microtech on 2017/11/14.
 */

public class DimenUtil {
    public static int getScreenWidth(){
        final Resources resources = Frame.getApplicationContext().getResources();
        final DisplayMetrics dm = resources.getDisplayMetrics();
        return dm.widthPixels;//得到屏幕的宽
    }
    public static  int getScreenHeight(){
        final Resources resources = Frame.getApplicationContext().getResources();
        final DisplayMetrics dm = resources.getDisplayMetrics();
        return dm.heightPixels;//得到屏幕的高
    }
}

