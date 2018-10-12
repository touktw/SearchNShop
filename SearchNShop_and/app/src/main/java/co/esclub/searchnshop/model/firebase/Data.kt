package co.esclub.searchnshop.model.firebase

import co.esclub.searchnshop.util.LogCat
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat

/**
 * Created by tae.kim on 25/07/2017.
 */


//  /users/{id}/
@IgnoreExtraProperties
class User(val id: String) {
    var connectTime: Long = 0
    var connectTimeString: String? = null
    var updateTimeString: String? = null
    val userQueries = HashMap<String, UserQuery>()
}

@IgnoreExtraProperties
data class UserQuery(val keyword: String?, val mallName: String?) {
    val id = keyword + "_" + mallName
}

////  /keywords/{keyword}/
//@IgnoreExtraProperties
//data class KeywordItem(val keyword: String = "") {
//    var updateTime: Long = 0
//}


//  /items/{keyword}/
val SDF = SimpleDateFormat.getTimeInstance(SimpleDateFormat.MEDIUM)
@IgnoreExtraProperties
open class SearchResult {
    @SerializedName("keyword")
    open var keyword: String? = null
    @SerializedName("shopItem")
    open var shopItem: ArrayList<ShopItem>? = null

    var updateTime: Long? = null

    @Exclude
    fun dump() {
        val TAG = SearchResult::class.java.simpleName
        val time = if(updateTime != null) SDF.format(updateTime) else "unknown"
        LogCat.d(TAG, "keyword:${keyword} item size:${shopItem?.size} " +
                "time:${time}")
    }
}

@IgnoreExtraProperties
open class ShopItem {
    @SerializedName("title")
    open var title: String? = null
    @SerializedName("link")
    open var link: String? = null
    @SerializedName("image")
    open var image: String? = null
    @SerializedName("mallName")
    open var mallName: String? = null
    @SerializedName("position")
    open var position: Int = 0
}

//  /time
open class Time {
    @SerializedName("time")
    open var time: Long? = null
}

//  /version
open class Version {
    @SerializedName("version")
    open var version: Int? = null
}