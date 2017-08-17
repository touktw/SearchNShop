package co.esclub.searchnshop.net

import co.esclub.searchnshop.model.firebase.ShopItem
import com.google.gson.annotations.SerializedName

/**
 * Created by tae.kim on 15/06/2017.
 */


class NaverSearchResult {
    @SerializedName("items")
    var items: List<ShopItem>? = null
    @SerializedName("lastBuildDate")
    var lastBuildDate: String? = null
}