package k.peter.om_el_nour.Save_Data;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefs {

    final static String FileName = "Setting";

    // Read String from SherdPrefes
    public static String readSharedString(Context ctx, String Value, String defaultValue) {
        SharedPreferences sharedPref = ctx.getSharedPreferences(FileName, Context.MODE_PRIVATE);
        return sharedPref.getString(Value, defaultValue);
    }
    // Save String from SherdPrefes
    public static void saveSharedString(Context ctx, String TAG, String Value) {
        SharedPreferences sharedPref = ctx.getSharedPreferences(FileName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(TAG,Value);
        editor.apply();
    }

    // Save Boolean from SherdPrefes
    public static void saveSharedBoolean(Context ctx, String TAG, Boolean Value){
        SharedPreferences sharedPref = ctx.getSharedPreferences(FileName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(TAG,Value);
        editor.apply();
    }
    // Read Boolean from SherdPrefes
    public static Boolean readSharedBoolean(Context ctx, String TAG, Boolean defaultValue) {
        SharedPreferences sharedPref = ctx.getSharedPreferences(FileName, Context.MODE_PRIVATE);
        return sharedPref.getBoolean(TAG, defaultValue);
    }


    // also can add another fun to save/read int - and other types of data
}
