package k.peter.om_el_nour.Activity

import android.animation.Animator
import android.content.Intent
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.support.v4.content.ContextCompat
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import k.peter.om_el_nour.Check_Classes.CheckDate
import k.peter.om_el_nour.Check_Classes.InternetConnection
import k.peter.om_el_nour.R
import k.peter.om_el_nour.Save_Data.SharedPrefs
import k.peter.om_el_nour.Save_Data.User
import k.peter.om_el_nour.Save_Data.sharedTAG
import kotlinx.android.synthetic.main.login_activity.*

class Login : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)
       //Mack a timer for SplachScreen
        object : CountDownTimer(3000, 1000) {
            override fun onFinish() {
                appName.visibility = GONE
                loadingProgressBar.visibility = GONE
                rootView.setBackgroundColor(ContextCompat.getColor(this@Login, R.color.colorSplashText))
                bookIconImageView.setImageResource(R.mipmap.ic_launcher)
                startAnimation()
            }
            override fun onTick(p0: Long) {}
        }.start()
        //Skip Login Activity If User Save Login Data By Remember me CheckBox
        checker()
        //on button login clicked
        loginButton.setOnClickListener {
            //Check internet connection and save it in boolean value
            val check :Boolean = InternetConnection(this).isConnectToInternet
            //check if username field is empty
            when {
                emailEditText.text!!.isEmpty() -> emailEditText.error = "Enter Email"
                //check if password field is empty
                passwordEditText.text!!.isEmpty() -> passwordEditText.error = "Enter Password"
                //if connection
                check -> login(emailEditText.text.toString(),passwordEditText.text.toString())
                //if no connection
                else -> Toast.makeText(this,"Check Internet Connection",Toast.LENGTH_LONG).show()
            }//End When
        }//End OnClickListener

        //on Button Reset Password Clicked
        forgetPass.setOnClickListener {
            //Check internet connection using Internetconnection::Class and save it in boolean value
            val check :Boolean = InternetConnection(this).isConnectToInternet
            when {
                //check if email is empty
                emailEditText.text!!.isEmpty() -> emailEditText.error = "Enter Your Email!"
                //if connection
                check -> reset(emailEditText.text.toString())
                //if no connection
                else -> Toast.makeText(this,"Check Internet Connection",Toast.LENGTH_LONG).show()
            }//End When
        }//End OnClickListener
    }

    //Skip Login Activity If User Save Login Data By Remember me CheckBox
    private fun checker(){
        val check = java.lang.Boolean.valueOf(SharedPrefs.readSharedBoolean(this,sharedTAG().User_Found,false))
        val intent = Intent(this, Main::class.java)
        intent.putExtra(sharedTAG().User_Found, check)
        //The Value if you click on Login Activity and Set the value is FALSE and whe false the activity will be visible
        if (check) {
            startActivity(intent)
            finish()
        } //If no the Admin Activity not Do Anything
    }

    //Login Request
    private fun login(email:String,password:String){
        //show the Login progress bar
        LoginProgressBar.visibility = VISIBLE
        //Sign In
        val auth = FirebaseAuth.getInstance()
        //Firebase SignIn With Email And Password
        auth.signInWithEmailAndPassword(email,password).addOnCompleteListener {
                task->
            if (task.isSuccessful){
                //Check Date Plan
                CheckDate().CheckPlan(this)
                //IF Successful Get The User Data From Database Using User uid
                val reference = FirebaseDatabase.getInstance().reference
                reference.child("Users").child(auth.currentUser!!.uid)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {
                            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                        }//End OnCancel
                        override fun onDataChange(p0: DataSnapshot) {
                            var user = p0.getValue(User::class.java)
                            //Save User Data in SheredPrefs
                            SharedPrefs.saveSharedString(applicationContext,sharedTAG().Code,user!!.Code.toString())
                            SharedPrefs.saveSharedString(applicationContext,sharedTAG().Gender,user.Gender)
                            SharedPrefs.saveSharedString(applicationContext,sharedTAG().Permission,user.Permission)
                            if (checkbox.isChecked){
                                SharedPrefs.saveSharedBoolean(applicationContext,sharedTAG().User_Found,true) //use this line to skip the activity
                            }//End if (checkBox.isChecked)
                            startActivity(Intent(applicationContext, Main::class.java)) //move to activity
                            finish() // finish the activity
                        }//End OnDataChange
                    })//End Event Listener
            }else{
                //Remove LoginProgressBar
                LoginProgressBar.visibility = GONE
                Toast.makeText(this,"Error Check Username and Password",Toast.LENGTH_LONG).show()
            }//else
        }//End addOnCompleteListener
    }//End Login

    //Send reset email
    private fun reset(email: String){
        val auth = FirebaseAuth.getInstance()
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this,"Email send successfully Check your email",Toast.LENGTH_LONG).show()
                }//End if
            }//End Listener
    }

    //Start Animation to show content
    private fun startAnimation(){
        bookIconImageView.animate().apply {
            x(50f)
            y(100f)
            duration = 1000
        }.setListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(p0: Animator?) {}
            override fun onAnimationEnd(p0: Animator?) {
                afterAnimationView.visibility = VISIBLE }
            override fun onAnimationCancel(p0: Animator?) {}
            override fun onAnimationStart(p0: Animator?) {}
        })
    }

}
