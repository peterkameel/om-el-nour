package k.peter.om_el_nour.Broadcast

import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import k.peter.om_el_nour.Save_Data.Date
import java.text.SimpleDateFormat
import java.util.*
import android.support.v4.app.NotificationManagerCompat
import k.peter.om_el_nour.Activity.Login
import k.peter.om_el_nour.R

class Notify_Receiver: BroadcastReceiver() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context, intent: Intent?) {
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

        //Initialize Intent To Open Application From Notification
        val intent = Intent(context, Login::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)


        var Ref = FirebaseDatabase.getInstance().reference
        Ref.keepSynced(true)//Enable Data Synced
        Ref.child("Plan").addListenerForSingleValueEvent(object : ValueEventListener {
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
                    //Build The Notification
                    val builder = NotificationCompat.Builder(context,"")
                        .setSmallIcon(R.drawable.ic_notify)
                        .setLargeIcon(BitmapFactory.decodeResource(context.resources,R.mipmap.ic_launcher))
                        .setColor(Color.TRANSPARENT)
                        .setContentTitle("افتقاد ام النور")
                        .setContentText("Started at: ${sdf.format(Start_Date)} Ended at: ${sdf.format(End_Date)}")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .setDefaults(Notification.DEFAULT_ALL)
                    val notificationManager = NotificationManagerCompat.from(context)
                    notificationManager.notify(0, builder.build())
                }//End If
            }//End onDataChange
        })//End addValueEventListener
    }//End on Receive
}//End Class