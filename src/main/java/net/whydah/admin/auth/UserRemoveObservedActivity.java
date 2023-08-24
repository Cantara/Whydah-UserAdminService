package net.whydah.admin.auth;

import org.valuereporter.activity.ObservedActivity;

public class UserRemoveObservedActivity extends ObservedActivity {
    public static final String USER_SESSION_ACTIVITY = "userSession";
    private static final String USER_SESSION_ACTIVITY_DB_KEY = "userid";

    public UserRemoveObservedActivity(String userid,String usersessionfunction,String applicationtokenid, String applicationid) {
        super(USER_SESSION_ACTIVITY, System.currentTimeMillis());
        put("userid", userid);
        put("usersessionfunction", usersessionfunction);
        put("applicationtokenid", applicationtokenid);
        put("applicationid", applicationid);
    }
}