package co.esclub.searchnshop.model.item

import co.esclub.searchnshop.util.LogCat
import io.realm.DynamicRealmObject
import java.util.TreeMap

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey

/**
 * Created by tae.kim on 06/06/2017.
 */

open class SearchItem : RealmObject, Item {
    @Ignore
    val TAG = SearchItem::class.java.simpleName

    @PrimaryKey
    open var id: String? = null
    open var keyWord: String? = null
    open var mallName: String? = null
    open var lastSearchTime: Long = 0

    @Ignore
    var isSuccess = false

    @Ignore
    var itemTreeMap = TreeMap<Int, ShopItem>()

    open var items = RealmList<ShopItem>()
        internal set

    constructor(keyWord: String?, mallName: String?) {
        this.keyWord = keyWord
        this.mallName = mallName
        this.id = makeID(keyWord, mallName)
    }

    constructor() {
    }

    fun dump() {
        LogCat.d(TAG, "KEY_WORD: " + keyWord!!)
        LogCat.d(TAG, "MALL_NAME: " + mallName!!)
        LogCat.d(TAG, "LAST_UPDATE_TIME" + lastSearchTime)
        if (items.size > 0) {
            for (item in items) {
                LogCat.d(TAG, "====")
                LogCat.d(TAG, "TITLE: " + item.title)
                LogCat.d(TAG, "====")
            }
        } else {
            LogCat.d(TAG, "Item size 0")
        }
    }

    companion object {
        val KEY_ID = "id"

        fun makeID(keyWord: String?, mallName: String?): String {
            return keyWord + "_" + mallName
        }
    }

    constructor(item: DynamicRealmObject) {
        this.keyWord = item.getString("keyWord")
        this.mallName = item.getString("mallName")
        this.lastSearchTime = item.getLong("lastSearchTime")
        for (shopItem in item.getList("items")) {
            shopItem?.let {
                items.add(ShopItem(shopItem))
            }
        }
    }

    fun removeSpacing() {
        this.keyWord = keyWord?.trim()?.replace(" ", "")
        this.id = makeID(keyWord, mallName)
    }

    fun getTransItems(): RealmList<DynamicRealmObject>? {
        val ret  = RealmList<DynamicRealmObject>()
        for(item in items) {
            ret.add(DynamicRealmObject(item))
        }
        return ret
    }
}
