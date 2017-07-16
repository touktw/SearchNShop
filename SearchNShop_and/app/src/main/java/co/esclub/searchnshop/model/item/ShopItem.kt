package co.esclub.searchnshop.model.item

import co.esclub.searchnshop.model.firebase.NItem
import com.google.gson.annotations.SerializedName
import io.realm.RealmObject

/**
 * Created by tae.kim on 06/06/2017.
 */


open class ShopItem : RealmObject, Item {
    constructor() {

    }

    constructor(title: String,
                link: String,
                image: String,
                mallName: String, position: Int) {
        this.title = title
        this.link = link
        this.image = image
        this.mallName = mallName
        this.position = position
    }

    constructor(item: NItem) {
        this.title = item.title
        this.link = item.link
        this.image = item.image
        this.mallName = item.mallName
        this.position = item.position
    }

    @SerializedName("title")
    open var title: String? = null
    @SerializedName("link")
    open var link: String? = null
    @com.google.gson.annotations.SerializedName("image")
    open var image: String? = null
    @com.google.gson.annotations.SerializedName("mallName")
    open var mallName: String? = null
    open var position: Int = 0
}
