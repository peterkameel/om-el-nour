package k.peter.om_el_nour.Save_Data

//Data Model For Recycler Item
data class Item_Model(
    var name: String? = null,
    var year: String? = null,
    var address: String? = null,
    var phone: String? = null,
    var comment: String? = null,
    var khadem: String? = null,
    var done: String? = null
)

//Data Model For Current User
data class User (
    var Code: Int = 0,
    var Permission:String = "",
    var Gender: String ="",
    var Name: String =""
)

//Data Madel For Plan Date
data class Date(
    var Date:String = ""
)

//SharedPrefs TAG
 class sharedTAG() {
    val Code: String = "Code"
    val Gender: String = "Gender"
    val Permission: String = "Permission"
    val User_Found: String = "User_Found"
    val CheckDate : String = "CheckDate"
}
