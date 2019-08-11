package k.peter.om_el_nour.Activity

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.widget.Toast
import k.peter.om_el_nour.Adapters.Main_recycle_adapter
import k.peter.om_el_nour.R
import kotlinx.android.synthetic.main.main_activity.*
import android.view.MenuItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.miguelcatalan.materialsearchview.MaterialSearchView
import k.peter.om_el_nour.Check_Classes.CheckDate
import k.peter.om_el_nour.Check_Classes.InternetConnection
import k.peter.om_el_nour.Save_Data.Item_Model
import k.peter.om_el_nour.Save_Data.SharedPrefs
import k.peter.om_el_nour.Save_Data.User
import k.peter.om_el_nour.Save_Data.sharedTAG
import kotlinx.android.synthetic.main.add_user.*
import kotlinx.android.synthetic.main.date.*
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class Main : AppCompatActivity() {

    companion object {
        var list = ArrayList<Item_Model>()
        //Recycler Adapter
        lateinit var adapter: Main_recycle_adapter
        //Database And DatabaseReference
        lateinit var database:FirebaseDatabase
        lateinit var Ref : DatabaseReference
        //Save Files
        val Folder = File("/sdcard/Om_El_Nour/")//Folder Path
        lateinit var fileOutputStream: FileOutputStream
        lateinit var StatisticsFile: File
        lateinit var PlanFile: File
        //Save User Gender/Permission/Code
        lateinit var Gender: String
        lateinit var Permission: String
        lateinit var Code: String

        //Save dates && Check Plan
        val sdf = SimpleDateFormat("yyyy_MM_dd")
        var date: String? = null
        var check_date :Boolean = false

        var i: Int = 0 //For Comment Dialog Loop
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        //support toolbar
        setSupportActionBar(toolbar)
        // RecycleView set LayoutManager
        Main_Recycler.setHasFixedSize(true)
        val manager = LinearLayoutManager(this)
        manager.orientation = LinearLayoutManager.VERTICAL
        Main_Recycler.layoutManager = manager
        adapter = Main_recycle_adapter(this, list)

        //Database
        database = FirebaseDatabase.getInstance()
        Ref = database.reference
        Ref.keepSynced(true)//Make Database Synced

        //initialize User Gender/Permission/Code
        Gender = SharedPrefs.readSharedString(this,sharedTAG().Gender,"")
        Permission = SharedPrefs.readSharedString(this,sharedTAG().Permission,"")
        Code = SharedPrefs.readSharedString(this,sharedTAG().Code,"")

        //Check if there is a plan at the moment / if true data will load
        check_date = SharedPrefs.readSharedBoolean(this,sharedTAG().CheckDate,false)
        //Get The Data From FirebaseDatabase And Show It
        loadData()

        //Get Date Used To Save Files
        var Today = sdf.format(Calendar.getInstance().time)

        //Save Plan And Statistics as File.txt
        StatisticsFile = File(Folder,"Statistics ($Today).txt")
        PlanFile       = File(Folder,"OmElNour ($Today).txt")
    }


    //Main Menu functions
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        val item = menu!!.findItem(R.id.action_search)
        search_view.setMenuItem(item)
        search_view.setOnQueryTextListener(object :MaterialSearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }//End onQueryTextSubmit
            override fun onQueryTextChange(newText: String?): Boolean {
                try{
                    adapter.filter.filter(newText)
                }catch (e: Exception){}
                return true
            }//End onQueryTextChange
        })//End Listener
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when{
            item?.itemId == R.id.action_search ->{return true}
            item?.itemId == R.id.ADD_User ->{addUser()}
            item?.itemId == R.id.NotDone ->{
                if (item.title == "Not Done"){
                    loadNotDoneList()
                    item.title = ("Your List")
                }else{
                    loadData()
                    item.title = ("Not Done")
                }//End else
            }
            item?.itemId == R.id.ListDone->{
                if (item.title == "List Done"){
                    loadDoneList()
                    item.title = ("Your List")
                }else{
                    loadData()
                    item.title = ("List Done")
                }//End else
            }
            item?.itemId == R.id.statistics ->{
                //Check Write Permission
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                    //Request The Permission
                    ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),1)
                }else{
                    statistics()
                }//End else
            }
            item?.itemId == R.id.Start ->{startPlan()}
            item?.itemId == R.id.End ->{
                //Check Write Permission
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                    //Request The Permission
                    ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),1)
                }else{
                    endPlan()
                }//End else
            }
            item?.itemId == R.id.Logout ->{logout()}
        }//End When
        return super.onOptionsItemSelected(item)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        //Check User permission in normal will Disable some options
        if (SharedPrefs.readSharedString(this,sharedTAG().Permission,"B") == "B"){
            menu?.getItem(1)?.isVisible = false
            menu?.getItem(2)?.isVisible = false
            menu?.getItem(4)?.isVisible = false
            menu?.getItem(5)?.isVisible = false
            menu?.getItem(6)?.isVisible = false
        }//End if
        return true
    }


    //Load Data from FirebaseDatabase
    private fun loadData(){
        //Check if there is a plan at the moment / if true data will load
        if (check_date){
            //Get The Data From FirebaseDatabase And Show It
            Ref.child(Gender).orderByChild("khadem").equalTo(Code)
                .addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }//End OnCancelled
                    override fun onDataChange(p0: DataSnapshot) {
                        list.clear()
                        for (snapshot in p0.children) {
                            var item = snapshot.getValue(Item_Model::class.java)
                            //Filter Data Add Only items Which not Done Yet
                            if (item!!.done.equals("0") && item.comment.isNullOrEmpty()) {
                                list.add(item)
                            }//End if
                        }//End For
                        Main_Recycler.adapter = adapter
                    }//End OnDataChange
                })//End The Listener
        }else {
            Toast.makeText(this,"There is no plan at the moment",Toast.LENGTH_LONG).show()
        }//End else
    }
    //Load Data Of items Done to allow for user to edit it
    private fun loadDoneList(){
        //Check if there is a plan at the moment / if true data will load
        if (check_date){
            //Get The Data From FirebaseDatabase And Show It
            Ref.child(Gender).orderByChild("khadem").equalTo(Code)
                .addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }//End OnCancelled
                    override fun onDataChange(p0: DataSnapshot) {
                        list.clear()
                        for (snapshot in p0.children){
                            var item = snapshot.getValue(Item_Model::class.java)
                            //Filter Data Add Only items Which not Done Yet
                            if (!item!!.done.equals("0") || !item.comment.isNullOrEmpty()){
                                list.add(item)
                            }//End if
                        }//End For
                        Main_Recycler.adapter = adapter
                    }//End OnDataChange
                })//End The Listener
        }else {
            Toast.makeText(this,"There is no plan at the moment",Toast.LENGTH_LONG).show()
        }//End else
    }
    //Load Not Done items yet (list)
    private fun loadNotDoneList(){
        //Check if there is a plan at the moment / if true data will load
        if (check_date){
            //Get The Data From FirebaseDatabase And Show It
            list.clear() // Clear Data list First
            Ref.child(Gender).orderByChild("done").equalTo("0")
                .addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }//End OnCancelled
                    override fun onDataChange(p0: DataSnapshot) {
                        list.clear()
                        for (snapshot in p0.children){
                            var item = snapshot.getValue(Item_Model::class.java)
                            list.add(item!!)
                        }//End For
                        Main_Recycler.adapter = adapter
                    }//End OnDataChange
                })//End The Listener
        }else {
            Toast.makeText(this,"There is no plan at the moment",Toast.LENGTH_LONG).show()
        }//End else
    }


    //Add New User
    private fun addUser(){
        // Gender && Permission Variables
        var gender = ""
        var permission = ""

        //Add User Dialog
        val user = Dialog(this)
        user.setContentView(R.layout.add_user)
        user.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        //Save Gender
        user.User_Gender.setOnCheckedChangeListener {_,checkedId ->
            when (checkedId) {
                R.id.radio_Male -> {gender = "Male"}
                R.id.radio_Female -> {gender = "Female"}
            }//End When
        }
        //Save Permission
        user.User_Permission.setOnCheckedChangeListener {_, checkedId ->
            when (checkedId) {
                R.id.radio_A -> {permission = "A"}
                R.id.radio_B -> {permission = "B"}
            }//End When
        }

        //OnClick Cancel Button
        user.User_cancel.setOnClickListener {user.dismiss()}
        //OnClick Add Button
        user.ADD_User.setOnClickListener {
            //Check if any filed is empty /if else add the user
            when {
                //Check Name
                user.User_Name.text.isNullOrEmpty() -> user.User_Name.error = "You need to fill this field"
                //Check Email
                user.User_Email.text.isNullOrEmpty() -> user.User_Email.error = "You need to fill this field"
                //Check Password
                user.User_Password.text.isNullOrEmpty() -> user.User_Password.error = "You need to fill this field"
                //Check Code
                user.User_Code.text.isNullOrEmpty() -> user.User_Code.error = "You need to fill this field"
                //Check Gender
                gender == "" -> Toast.makeText(this,"Please Choose Gender",Toast.LENGTH_SHORT).show()
                //Check Permission
                permission == "" -> Toast.makeText(this,"Please Choose Permission",Toast.LENGTH_SHORT).show()
                // if all field is added will add user
                !InternetConnection(this).isConnectToInternet -> Toast.makeText(this,"Check Internet Connection",Toast.LENGTH_SHORT).show()
                else -> {
                    //ADD Account
                    val Auth = FirebaseAuth.getInstance()
                    Auth.createUserWithEmailAndPassword(user.User_Email.text.toString(),user.User_Password.text.toString())
                        .addOnCompleteListener(this){ task->
                            if (task.isSuccessful) {
                                //Save User Data
                                var User = User()
                                User.Name = user.User_Name.text.toString()
                                User.Code = user.User_Code.text.toString().toInt()
                                User.Gender = gender
                                User.Permission = permission

                                //Add User in Firebase Database
                                var Ref = FirebaseDatabase.getInstance().reference
                                Ref.child("Users").child(Auth.currentUser!!.uid).setValue(User)

                                Toast.makeText(this, "User Add Successfully", Toast.LENGTH_SHORT).show()
                                user.dismiss()
                            }//End if
                            else {Toast.makeText(this, "Faild", Toast.LENGTH_SHORT).show()}
                    }//End addOnCompleteListener
                }//End else
            }//End When
        }
        user.show()
    }


    //Start Plan By Change The Plan Dates In Database
    private fun startPlan(){
        //The Date Dialog
        val Date = Dialog(this)
        Date.setContentView(R.layout.date)
        Date.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        //On Date Change
        Date.Date_Calender?.setOnDateChangeListener { _ , year, month, dayOfMonth ->
            date = "$year/${month+1}/$dayOfMonth"
        }//End OnDateChangeListener

        //On cancel Button Clicked
        Date.date_cancel.setOnClickListener {
            i = 0
            Date.dismiss()
        }//End On Click
        //On Ok Button Clicked
        Date.date_ok.setOnClickListener {
            when(i){
                0->{
                    i = 1
                    Ref.child("Plan").child("Start").child("Date").setValue(date)
                    Toast.makeText(this,"Plan Started At: $date",Toast.LENGTH_SHORT).show()
                }//End Case 1
                1->{
                    i = 0
                    Ref.child("Plan").child("End").child("Date").setValue(date)
                    Toast.makeText(this,"Plan Ended At: $date",Toast.LENGTH_SHORT).show()
                    Toast.makeText(this,"Plan Started Successfully",Toast.LENGTH_SHORT).show()
                    CheckDate().CheckPlan(this)
                    Date.dismiss()
                }//End Case 2
            }//End When
        }//End On Click

        //Show the First Dialog
        Date.show()
    }
    //End Plan By Make All done is equal = 0 in Database And Delete All Comments
    private fun endPlan(){
        //Alert Dialog
        val dialog: AlertDialog
        val builder = AlertDialog.Builder(this,R.style.AlertDialog)
        builder.setMessage("You Will Remove All Comments And Make All Items Not Done. Are You Sure?")
        builder.setPositiveButton("Yes") { _, _ ->
            //On Click yes All Data Will Rest
            Ref.child("Users").orderByChild("gender").equalTo(Gender)
                .addListenerForSingleValueEvent(object :ValueEventListener{
                    override fun onCancelled(p0: DatabaseError) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }
                    override fun onDataChange(p0: DataSnapshot) {
                        for (snapshot in p0.children){
                            var user = snapshot.getValue(User::class.java)
                            writeEndPlanFile(user!!.Name,user.Code.toString())
                        }//End for
                        Ref.child("Plan").child("End").child("Date").setValue("2000/10/31")
                        Ref.child("Plan").child("Start").child("Date").setValue("2000/10/31")
                        //Change CheckDate Boolean
                        CheckDate().CheckPlan(this@Main)
                        Toast.makeText(this@Main,"End Successfully",Toast.LENGTH_SHORT).show()
                    }//End onDataChange
                })//End Listener
        }//End OnClick Yes
        builder.setNegativeButton("No") { _, _ -> }//End OnClick No
        //Builder Create And Dialog show
        dialog = builder.create()
        dialog.show()
    }
    //write End Plan file and delete comments and set all not done
    private fun writeEndPlanFile(Name: String,Code: String){
        Ref.child(Gender).orderByChild("khadem").equalTo(Code)
            .addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }
                override fun onDataChange(p0: DataSnapshot) {
                    for (snapshot in p0.children){
                        var item = snapshot.getValue(Item_Model::class.java)
                        //Check if Folder is Created
                        if (!Folder.exists()){Folder.mkdirs()}
                        //Save a File Have All Not Done items
                        if (item!!.done.equals("0")){
                            try {
                                fileOutputStream = FileOutputStream(PlanFile,true)
                                fileOutputStream.write("Name: ${item.name}\nKhadem: $Name\nComment: ${item.comment}\n\n".toByteArray())
                                fileOutputStream.close()
                            }catch (e: Exception){}
                        }//End if
                        Ref.child(Gender).child(item.name.toString()).child("done").setValue("0")
                        Ref.child(Gender).child(item.name.toString()).child("comment").setValue("")
                    }//End For
                }//End onDataChange
            })//End Listener
    }


    //Show statistics about how much items done for all users
    private fun statistics() {
        Ref.child("Users").orderByChild("gender").equalTo(Gender)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }//End OnCancelled
                override fun onDataChange(p0: DataSnapshot) {
                    for (snapshot in p0.children) {
                        var item = snapshot.getValue(User::class.java)
                        writeStatisticsFile(item!!.Code.toString(),item.Name)
                    }//End for
                    Toast.makeText(this@Main,"File Saved Successfully",Toast.LENGTH_SHORT).show()
                }//End onDataChange
            })//End Listener
    }
    //Loop To Calculate the percentage for all users and print it in txt file
    private fun writeStatisticsFile(Code:String, Name:String){
        var listA = ArrayList<Item_Model>()
        var listB = ArrayList<Item_Model>()
        var listC = ArrayList<Item_Model>()
        Ref.child(Gender).orderByChild("khadem").equalTo(Code)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }//End OnCancelled
                override fun onDataChange(p0: DataSnapshot) {
                    listA.clear()
                    listB.clear()
                    for (snapshot in p0.children){
                        var item = snapshot.getValue(Item_Model::class.java)
                        listA.add(item!!)

                        //Filter Data Add Only items Which not Done Yet
                        if (item.done.equals("1")){
                            listB.add(item)
                        }//End if

                        //Filter Data Add Only items Have a Comment
                        if (!item.comment.isNullOrEmpty()){
                            listC.add(item)
                        }//End if

                    }//End For
                    try {
                        if (!Folder.exists()){Folder.mkdirs()} //Check if Folder is Created
                        fileOutputStream = FileOutputStream(StatisticsFile,true)
                        fileOutputStream.write(("$Name : ${listB.size} Out Of ${listA.size} And ${listC.size} Have a Comments  " +
                                "${((listB.size.toDouble()/listA.size.toDouble())*100).toInt()}% Done\n\n").toByteArray())
                        fileOutputStream.close()
                    }catch (e: Exception){}
                }//End OnDataChange
            })//End The Listener
    }


    // Back Button
    override fun onBackPressed() {
        if (search_view.isSearchOpen) {
            search_view.closeSearch()
            Main_Recycler.adapter = adapter
        } else {
            super.onBackPressed()
        }
    }
    //Logout From Account
    private fun logout(){
        //SignOut From Firebase Account
        var Auth = FirebaseAuth.getInstance()
        Auth.signOut()

        //Delete Current User Data
        SharedPrefs.saveSharedString(this,sharedTAG().Code,"")
        SharedPrefs.saveSharedString(this,sharedTAG().Gender,"")
        SharedPrefs.saveSharedString(this,sharedTAG().Permission,"")

        //Cancel Login Activity Skipping
        SharedPrefs.saveSharedBoolean(this,sharedTAG().User_Found,false)

        //Go To The Login Activity and finish Main Activity
        startActivity(Intent(this,Login::class.java))
        finish()
    }
}
