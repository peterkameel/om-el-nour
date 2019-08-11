package k.peter.om_el_nour.Adapters

import android.app.Dialog
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import k.peter.om_el_nour.R
import kotlinx.android.synthetic.main.main_recycler_item.view.*
import java.lang.Exception
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.widget.Filter
import android.widget.Filterable
import android.widget.Toast
import com.google.firebase.database.FirebaseDatabase
import k.peter.om_el_nour.Save_Data.Item_Model
import k.peter.om_el_nour.Save_Data.SharedPrefs
import k.peter.om_el_nour.Save_Data.sharedTAG
import kotlinx.android.synthetic.main.comment.*

class Main_recycle_adapter(private val context: Context, private var List: ArrayList<Item_Model>):
    RecyclerView.Adapter<Main_recycle_adapter.ViewHolder>(),Filterable{

    var LtemList: ArrayList<Item_Model> = List
    //initialize User Gender
    val Gender = SharedPrefs.readSharedString(context, sharedTAG().Gender,"")

    override fun getFilter(): Filter {
        return object :Filter(){
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                var search = constraint.toString()
                LtemList = if (search.isEmpty()){
                    List
                }else {
                    var resultList = ArrayList<Item_Model>()
                    for (item in LtemList){
                        if (item.name!!.toLowerCase().contains(search.toLowerCase())) {
                            resultList.add(item)
                        }//End if
                    }//End for
                    resultList
                }//End else
                val filterResults = Filter.FilterResults()
                filterResults.values = LtemList
                return filterResults
            }//End performFiltering

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                LtemList = results!!.values as ArrayList<Item_Model>
                notifyDataSetChanged()
            }//End publishResults
        }//End object
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(p0.context).inflate(R.layout.main_recycler_item,p0,false))
    }

    override fun getItemCount() = LtemList.size

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        val item = LtemList[p1]

        p0.name.text    = item.name
        p0.year.text    = item.year
        p0.address.text = item.address
        // check If Item Have a Comment Layout Change Colors
        if (!item.comment.isNullOrEmpty()){
            p0.Main.setBackgroundColor(Color.parseColor("#393E46"))
            p0.name.setTextColor(Color.parseColor("#EEEEEE"))
            p0.year.setTextColor(Color.parseColor("#EEEEEE"))
            p0.address.setTextColor(Color.parseColor("#EEEEEE"))
            p0.done.isEnabled = false
        }//End If


        //Show Image Buttons Layout
        p0.card.setOnClickListener {
            if (p0.ImageButtons.isShown){
                p0.Main.removeView(p0.ImageButtons)
            }else{
                p0.Main.addView(p0.ImageButtons)
            }
        }//End onClick
        
        //Intent Phone Number to Call
        p0.call.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:" + item.phone)
            context.startActivity(intent)
        }//End onClick
        
        //Done And Remove item from list
        p0.done.setOnClickListener {
            val reference = FirebaseDatabase.getInstance().reference
            reference.keepSynced(true)//Make Database Synced
            try {
                reference.child(Gender).child(item.name!!).child("done").setValue("1")
                Toast.makeText(context, "Done", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show()
            }//End Catch
        }//End onClick
        
        //Add Comment to the item
        p0.comment.setOnClickListener {
            //Show comment dialog from comment.xml
            val comment = Dialog(context)
            comment.setContentView(R.layout.comment)
            comment.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            //Show the Last Comment
            comment.comment_text.setText(item.comment)

            //on click cancel button
            comment.comment_cancel.setOnClickListener { comment.dismiss() } //close the dialog

            //on click comment button
            comment.comment_action.setOnClickListener {
                var Ref = FirebaseDatabase.getInstance().reference
                Ref.keepSynced(true)//Make Database Synced
                try {
                    Ref.child(Gender).child(item.name!!).child("comment").setValue(comment.comment_text.text.toString())
                    Toast.makeText(context, "Comment Add Successfully", Toast.LENGTH_LONG).show()
                    comment.dismiss()
                } catch (e: Exception) {
                    Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show()
                }//End Catch
            }// Add the comment

            comment.show()
        }//End onClick
        
    }

    class ViewHolder(itemview: View): RecyclerView.ViewHolder(itemview) {
        val card     = itemview.Admin_Card
        val name     = itemview.Item_Name
        val year     = itemview.Item_Class
        val address  = itemview.Item_Address
        val Main     = itemview.Main_Layout
        val call     = itemview.call
        val done     = itemview.done
        val comment  = itemview.comment

        //Layout Have The ImageButtons
        val ImageButtons = itemview.Image_Buttons_Layout
        //Hide Image Button Layout Until card clicked
        val remove   = Main.removeView(ImageButtons)
    }

}