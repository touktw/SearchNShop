package co.esclub.searchnshop.model.db

/**
 * Created by tae.kim on 27/06/2017.
 */
interface DBManager<ITEM_T> {
    fun save(item: ITEM_T?)
    fun saveAll(items: List<ITEM_T>?)
    fun get(id: String?): ITEM_T?
    fun getAll(): List<ITEM_T>?
    fun delete(id: String?)
    fun deleteAll(ids: List<String>)
    fun deleteAll()
}