package co.esclub.searchnshop.model

import com.google.gson.annotations.SerializedName

/**
 * Created by tae.kim on 15/06/2017.
 */

class SearchResult {
    @SerializedName("items")
    var items: List<ShopItem>? = null
}
