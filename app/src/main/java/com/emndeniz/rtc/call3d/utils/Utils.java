package com.emndeniz.rtc.call3d.utils;



import android.content.Context;
import android.content.SharedPreferences;

import com.emndeniz.rtc.call3d.R;

public class Utils {

    public static final String EXTRA_CAMERA2 = "org.appspot.apprtc.CAMERA2";

    private final static String LOG_TAG = Utils.class.getSimpleName();

    public class Keys{

    }

    private static Context appContext;
    public static void setAppContext(Context context){
        appContext = context;
    }
    /**
     * UserName format contains '@' which is not acceptable as topic. We should change it to '-'.
     * @param userName  User name of user
     * @return  user fire base topic
     */
    public static String getFireBaseUserTopicFormat(String userName){
        return getFireBaseTopic() + "_" + userName.replace("@","-");
    }

    public static String getFireBaseTopic(){
       return appContext.getResources().getString(R.string.fire_base_app_topic_key);
    }

    public static String getLocalUserName(){

        SharedPreferences sharedPref = appContext.getSharedPreferences(appContext.getResources().getString(R.string.shared_pref_key), Context.MODE_PRIVATE);
        return sharedPref.getString(appContext.getResources().getString(R.string.user_name),null);
    }

    /*private boolean useCamera2() {
        return Camera2Enumerator.isSupported(appContext); //&& appContext.getIntent().getBooleanExtra(EXTRA_CAMERA2, true);
    }*/



}
