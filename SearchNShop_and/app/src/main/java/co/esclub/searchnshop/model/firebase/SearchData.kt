package co.esclub.searchnshop.model.firebase

import co.esclub.searchnshop.model.item.ShopItem
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import java.util.*

/**
 * Created by tae.kim on 30/06/2017.
 */

class QueryData {
    var mallName: String? = null
    var searchData: SearchData? = null
}

@IgnoreExtraProperties
data class Keyword(val keyword: String) {
    var lastUpdateTime: Long = 0
}

@IgnoreExtraProperties
class SearchData {
    var keyWord: String? = null
    var lastUpdateTime: Long = 0
    var items: ArrayList<co.esclub.searchnshop.model.firebase.ShopItem> = ArrayList<co.esclub.searchnshop.model.firebase.ShopItem>()
}


//@IgnoreExtraProperties
//open class NItem {
//
//    @com.google.gson.annotations.SerializedName("title")
//    open var title: String? = null
//    @com.google.gson.annotations.SerializedName("link")
//    open var link: String? = null
//    @com.google.gson.annotations.SerializedName("image")
//    open var image: String? = null
//    @com.google.gson.annotations.SerializedName("mallName")
//    open var mallName: String? = null
//    open var position: Int = 0
//}
//
//@IgnoreExtraProperties
//class Item {
//    var title: String? = null
//    var link: String? = null
//    var image: String? = null
//    var mallName: String? = null
//    var position: Int? = null
//}