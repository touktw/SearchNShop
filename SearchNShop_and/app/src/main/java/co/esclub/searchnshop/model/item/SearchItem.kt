package co.esclub.searchnshop.model.item

import android.util.Log

import java.util.TreeMap

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey

/**
 * Created by tae.kim on 06/06/2017.
 */

open class SearchItem : RealmObject, Item {

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
        Log.d("###", "KEY_WORD: " + keyWord!!)
        Log.d("###", "MALL_NAME: " + mallName!!)
        if (items.size > 0) {
            for (item in items) {
                Log.d("###", "====")
                Log.d("###", "TITLE: " + item.title)
                Log.d("###", "====")
            }
        } else {
            Log.d("###", "Item size 0")
        }
    }

    companion object {
        val KEY_ID = "id"

        fun makeID(keyWord: String?, mallName: String?): String {
            return keyWord + "_" + mallName
        }
    }
}
