package app.csb.yrkqw2;

import android.content.Context;
import android.content.SharedPreferences;

public class AuthManager {
    private static final String PREF_NAME = "AuthPrefs";
    private static final String KEY_TOKEN = "token";
    private static AuthManager instance;
    private SharedPreferences sharedPreferences;

    private AuthManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized AuthManager getInstance(Context context) {
        if (instance == null) {
            instance = new AuthManager(context.getApplicationContext());
        }
        return instance;
    }

    public void saveToken(String token) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_TOKEN, token);
        editor.apply();
    }

    public String getToken() {
        return sharedPreferences.getString(KEY_TOKEN, null);
    }

    public boolean isLoggedIn() {
        return getToken() != null;
    }

    public void logout() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_TOKEN);
        editor.apply();
    }
}
