package k.peter.om_el_nour.Check_Classes

import android.content.Context
import android.os.Build
import android.support.annotation.RequiresApi
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import k.peter.om_el_nour.Save_Data.Date
import k.peter.om_el_nour.Save_Data.SharedPrefs
import k.peter.om_el_nour.Save_Data.sharedTAG
import java.text.SimpleDateFormat
import java.util.*

class CheckDate {

 companion object {
        //Today Date
        var today = Calendar.getInstance().time
        //Date Format
        val sdf = SimpleDateFormat("yyyy/MM/dd")

        //Dates Of Plan
        var Start_Date:java.util.Date? = null
        var End_Date:java.util.Date?   = null

        val calender = Calendar.getInstance()
        //Int Value Fore Loop
        var i = 0
    }

 fun CheckPlan(ctx:Context){

        var Ref = FirebaseDatabase.getInstance().reference
        Ref.keepSynced(true)//Enable Data Synced
        Ref.child("Plan").addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onDataChange(p0: DataSnapshot) {
                for (date in p0.children){
                    var item = date.getValue(Date::class.java)
                    if (i==0){
                        i=1
                        calender.time = sdf.parse(item!!.Date)
                        End_Date = calender.time
                    }else{
                        i=0
                        calender.time = sdf.parse(item!!.Date)
                        Start_Date = calender.time
                    }//End else
                }//End For
                if (Start_Date!! <= today && today <= End_Date!!){
                    SharedPrefs.saveSharedBoolean(ctx,sharedTAG().CheckDate,true)
                }else {
                    SharedPrefs.saveSharedBoolean(ctx,sharedTAG().CheckDate,false)
                }//End else
            }//End onDataChange
        })//End addValueEventListener
    }//End CheckPlan
}