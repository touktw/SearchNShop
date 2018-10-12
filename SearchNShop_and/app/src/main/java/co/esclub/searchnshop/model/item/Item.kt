package co.esclub.searchnshop.model.item

import android.arch.lifecycle.*
import android.arch.persistence.room.*
import android.content.Context

/**
 * Created by tae.kim on 21/06/2017.
 */

interface Item

@Entity(foreignKeys = arrayOf(ForeignKey(entity = Goods::class,
        parentColumns = arrayOf("keyword"),
        childColumns = arrayOf("keyword"))),
        tableName = "goods")
open class Goods {

    var keyword: String? = null

}

@Entity(primaryKeys = arrayOf("keyword", "mall_name"),
        tableName = "keywords")
open class KeywordItem {
    var keyword: String? = null
    @ColumnInfo(name = "mall_name")
    var mallName: String? = null

    @Ignore
    var goodsList: LiveData<List<Goods>>? = null
}

@Database(entities = arrayOf(KeywordItem::class), version = 1)
abstract class KeywordItemDB : RoomDatabase() {
    abstract fun keywordItemDao(): KeywordItemDao
}

@Database(entities = arrayOf(Goods::class), version = 1)
abstract class GoodsDB : RoomDatabase() {
    abstract fun goodsDao(): GoodsDao
}

@Dao
interface GoodsDao {
    @Query("SELECT * from goods")
    fun getAll(): LiveData<List<Goods>>
}

@Dao
interface KeywordItemDao {
    @Query("SELECT * from keywords")
    fun getAll(): LiveData<List<KeywordItem>>

    @Query("SELECT * from keywords WHERE keyword LIKE :keyword")
    fun get(keyword: String): List<KeywordItem>

    @Insert
    fun insert(vararg keywordResults: KeywordItem)

    @Delete
    fun delete(vararg keywordResults: KeywordItem)
}

class RoomDB(val appContext: Context) {
    val db = Room.databaseBuilder(appContext, KeywordItemDB::class.java, "keyword-result")
}

class Test : LifecycleRegistryOwner {
    override fun getLifecycle(): LifecycleRegistry {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun test(context: Context) {
        val db = Room.databaseBuilder(context, KeywordItemDB::class.java, "keyword-result")
                .build()
        db.keywordItemDao().getAll().observe(this, object : Observer<List<KeywordItem>> {
            override fun onChanged(t: List<KeywordItem>?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        })
    }
}