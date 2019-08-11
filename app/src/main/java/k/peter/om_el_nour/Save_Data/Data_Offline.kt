package k.peter.om_el_nour.Save_Data

import android.app.Application
import com.google.firebase.database.FirebaseDatabase

class Data_Offline: Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
    }
}