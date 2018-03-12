package open.lanya.com.ipad_mike;

import android.app.Application;

import com.joanzapata.iconify.fonts.FontAwesomeModule;

import open.lanya.com.ipad_mike.init.Frame;
import open.lanya.com.ipad_mike.net.AddCookieInterceptor;
import open.lanya.com.ipad_mike.util.FontEcModule;

/**
 * Created by microtech on 2018/2/28.
 */

public class MyApp extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        Frame.init(this)
                .withIcon(new FontAwesomeModule())
                .withIcon(new FontEcModule())//https://doc.newmicrotech.cn/otsmobile/app/     //http://192.168.1.135:9988/mk/app/
                .withApiHost("https://doc.newmicrotech.cn/mk/app/")
                //.withInterceptor(new DebugInterceptor("index.html",R.raw.test))
                .withWxchaAppId("wxace207babfef510d")//微信的APPID
                .withWxchartSecRet("ec5f7134a2c99e34e9a0f90c896da95d")//微信的scret
                .withJavaScriptinterface("web")
                //添加Cookie同步拦截器
                .withInterceptor(new AddCookieInterceptor())
                .weithWebHost("https://www.baidu.com/")
                .configure();//初始化
    }
}
