package app.kamix.app;

import android.app.Application;
import android.content.Context;

import app.kamix.localstorage.DAO;
import app.kamix.models.User;

public class KamixApp extends Application {
    public static final int PIN_CODE_INCORRECT = 171;
    public static final int PASSWD_INCORRECT = 172;
    public static final int EXISTING_MOBILE = 181;
    public static final int EXISTING_PASSWD = 182;
    public static final int EXISTING_USER = 1900;
    public static final int INEXISTING_USER = 1910;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public static User getUser(Context context){
        DAO dao = new DAO(context);
        return dao.user();
    }
}
