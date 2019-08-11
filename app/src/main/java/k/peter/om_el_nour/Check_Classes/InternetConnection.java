package k.peter.om_el_nour.Check_Classes;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class InternetConnection {
    private Context context;
    public InternetConnection(Context context){this.context = context;}
    public Boolean isConnectToInternet(){
        ConnectivityManager con = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
        if (con != null){
          NetworkInfo info = con.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
            return true;
            }
        }
        return false;

    }
}
