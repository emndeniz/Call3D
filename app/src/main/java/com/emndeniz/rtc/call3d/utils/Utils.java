package com.emndeniz.rtc.call3d.utils;

public class Utils {

    /**
     * UserName format contains '@' which is not acceptable as topic. We should change it to '-'.
     * @param appTopic  Topic prefix of application
     * @param userName  User name of user
     * @return  user fire base topic
     */
    public static String getFireBaseUserTopicFormat(String appTopic,String userName){
        return appTopic + "_" + userName.replace("@","-");
    }
}
