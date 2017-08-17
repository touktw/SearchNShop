package co.esclub.searchnshop.model.db

import android.util.Log
import co.esclub.searchnshop.model.item.SearchItem
import io.realm.*

/**
 * Created by tae.kim on 27/06/2017.
 */

object SearchItemRealmManager : RealmManager<SearchItem>() {
    override val idFieldName = "id"
    override val clazz = SearchItem::class.java
}

abstract class RealmManager<ITEM_T : RealmObject> : DBManager<ITEM_T> {
    abstract val clazz: Class<ITEM_T>
    abstract val idFieldName: String
    override fun save(item: ITEM_T?) {
        item?.let {
            DB.realm()?.beginTransaction()
            DB.realm()?.copyToRealmOrUpdate(it)
            DB.realm()?.commitTransaction()
        }
    }

    override fun saveAll(items: List<ITEM_T>?) {
        items?.let {
            DB.realm()?.beginTransaction()
            DB.realm()?.copyToRealmOrUpdate(it)
            DB.realm()?.commitTransaction()
        }
    }

    override fun get(id: String?): ITEM_T? =
            DB.realm()?.where(clazz)?.equalTo("id", id)?.findFirst()

    override fun getAll(): List<ITEM_T>? =
            DB.realm()?.where(clazz)?.findAll()

    fun getAllByField(field: String?, value: String?): List<ITEM_T>? =
            DB.realm()?.where(clazz)?.equalTo(field, value)?.findAll()

    override fun delete(id: String?) {
        DB.realm()?.beginTransaction()
        DB.realm()?.where(SearchItem::class.java)
                ?.equalTo(idFieldName, id)?.findAll()?.deleteAllFromRealm()
        DB.realm()?.commitTransaction()
    }

    override fun deleteAll() {
        DB.realm()?.beginTransaction()
        DB.realm()?.where(SearchItem::class.java)
                ?.findAll()?.deleteAllFromRealm()
        DB.realm()?.commitTransaction()
    }

    override fun deleteAll(ids: List<String>) {
        DB.realm()?.beginTransaction()
        for (id in ids) {
            DB.realm()?.where(SearchItem::class.java)
                    ?.equalTo("id", id)?.findAll()?.deleteAllFromRealm()
        }
        DB.realm()?.commitTransaction()
    }


    fun addChangeListener(listener: RealmChangeListener<Realm>) {
        DB.realm()?.addChangeListener(listener)
    }

    fun removeChangeListener(listener: RealmChangeListener<Realm>) {
        DB.realm()?.removeChangeListener(listener)
    }

    fun close() {
        DB.realm()?.close()
    }

    fun dump() {
        val datas = DB.realm()?.where(SearchItem::class.java)?.findAll()
        Log.d("###", "dump SearchItem")
        datas?.let {
            for (item in it) {
                Log.d("###", "id:" + item.id)
            }
        }
    }

    fun realm(): Realm? {
        return DB.realm()
    }
}

object DB {
    val SCHEME_VERSION = 2
    val config = RealmConfiguration.Builder().schemaVersion(SCHEME_VERSION.toLong())
            .migration(Migration).build()

    fun realm(): Realm? {
        return Realm.getInstance(config)
    }

    object Migration : RealmMigration {
        override fun migrate(realm: DynamicRealm?, oldVersion: Long, newVersion: Long) {
            Log.d("###", "migrate old[${oldVersion}] new[${newVersion}]")
            val schema = realm?.schema as RealmSchema
            val old = oldVersion
            if (old == 1L || old == 0L) {
                val objectSchema = schema.get("SearchItem")
                objectSchema.addField("lastSearchTime", Long::class.java)
            }
//            if (old <= 2L) {
//                val items = realm.where("SearchItem").findAll()
//                for (item in items) {
//                    val searchItem = SearchItem(item)
//                    if (searchItem.keyWord?.contains(" ") ?: false) {
//                        searchItem.removeSpacing()
//                        if (realm.where("SearchItem")
//                                .equalTo("id", searchItem.id)
//                                .findFirst() == null) {
//                            val newObj = realm.createObject("SearchItem", searchItem.id)
//                            newObj.set("keyWord", searchItem.keyWord)
//                            newObj.set("mallName", searchItem.mallName)
//                            newObj.set("lastSearchTime", searchItem.lastSearchTime)
//                            newObj.setList("items", searchItem.getTransItems())
//
//                        }
//                        item.deleteFromRealm()
//                    }
//                }
//            }
        }

    }
}
