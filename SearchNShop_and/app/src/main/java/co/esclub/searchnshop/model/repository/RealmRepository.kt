package co.esclub.searchnshop.model.repository

import co.esclub.searchnshop.model.db.RealmManager
import co.esclub.searchnshop.model.db.SearchItemRealmManager
import co.esclub.searchnshop.model.firebase.SearchData
import co.esclub.searchnshop.model.item.SearchItem
import co.esclub.searchnshop.model.item.ShopItem
import co.esclub.searchnshop.util.LogCat
import io.realm.RealmObject

/**
 * Created by tae.kim on 27/06/2017.
 */


abstract class RealmRepository<ITEM_T : RealmObject> : Repository<ITEM_T> {
    abstract val db: RealmManager<ITEM_T>
    override fun save(item: ITEM_T?) {
        db.save(item)
    }

    override fun saveAll(items: List<ITEM_T>?) {
        db.saveAll(items)
    }

    override fun delete(id: String?) {
        db.delete(id)
    }

    override fun deleteAll() {
        db.deleteAll()
    }

    override fun deleteAll(ids: List<String>) {
        db.deleteAll(ids)
    }

    override fun get(id: String?): ITEM_T? {
        return db.get(id)
    }

    override fun getAll(): List<ITEM_T>? {
        return db.getAll()
    }
}

object SearchItemRepository : RealmRepository<SearchItem>() {
    val TAG = SearchItemRepository::class.java.simpleName
    override val db = SearchItemRealmManager
    fun dump() = db.dump()
    fun updateItem(searchItem: SearchItem, data: SearchData) {
        LogCat.d(TAG, "updateItem")
        val realm = db.realm()
        realm?.let {
            it.beginTransaction()
            searchItem.lastSearchTime = data.lastUpdateTime
            searchItem.items.clear()
            val matchedItems = data.items.filter { it.mallName == searchItem.mallName }
            for (matchedItem in matchedItems) {
                searchItem.items.add(ShopItem(matchedItem))
            }
            it.commitTransaction()
        }
    }
}
