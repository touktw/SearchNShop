package co.esclub.searchnshop.model.firebase

import android.os.AsyncTask
import android.text.format.DateUtils
import co.esclub.searchnshop.model.db.SearchItemRealmManager
import co.esclub.searchnshop.model.item.SearchItem
import co.esclub.searchnshop.model.item.ShopItem
import co.esclub.searchnshop.net.NShopSearch
import co.esclub.searchnshop.net.NaverSearchService
import co.esclub.searchnshop.util.LogCat
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

/**
 * Created by tae.kim on 13/07/2017.
 */

object FirebaseDBManager {
    val TAG = FirebaseDBManager::class.java.simpleName
    val datas = HashMap<String, SearchData>()
    val db = FirebaseDatabase.getInstance()

    init {
        db.reference.child("items").addChildEventListener(object : ChildEventListener {
            override fun onCancelled(error: DatabaseError?) {
                LogCat.d(TAG, "onCancelled " + error.toString())
            }

            override fun onChildMoved(snapshot: DataSnapshot?, lastName: String?) {
                LogCat.d(TAG, "onChildMoved lastName[${lastName}]")
                val data = snapshot?.getValue(SearchData::class.java)
                data?.let {
                    LogCat.d(TAG, "keyword:${it.keyWord}")
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot?, lastName: String?) {
                LogCat.d(TAG, "onChildChanged lastName[${lastName}]")
                val data = snapshot?.getValue(SearchData::class.java)
                data?.let {
                    LogCat.d(TAG, "keyword:${it.keyWord}")
                    datas[it.keyWord!!] = it
                }
            }

            override fun onChildAdded(snapshot: DataSnapshot?, lastName: String?) {
                LogCat.d(TAG, "onChildAdded lastName[${lastName}]")
                val data = snapshot?.getValue(SearchData::class.java)
                data?.let {
                    LogCat.d(TAG, "keyword:${it.keyWord}")
                    datas[it.keyWord!!] = it
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot?) {
                LogCat.d(TAG, "onChildRemoved")
                val data = snapshot?.getValue(SearchData::class.java)
                data?.let {
                    LogCat.d(TAG, "keyword:${it.keyWord}")
                    datas.remove(it.keyWord)
                }
            }

        })
    }

    fun isNeedUpdate(query: String): Boolean {
        val data = datas[query]
        data?.let {
            return (System.currentTimeMillis() - it.lastUpdateTime) > DateUtils.HOUR_IN_MILLIS
        }
        return true
    }

    fun loadOneShot(query: String, mallName: String) {
        LogCat.d(TAG, "loadOneShot query:" + query)
        db.reference.child("items").child(query)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(snapshot: DatabaseError?) {
                        LogCat.d(TAG, "onCancelled")
                    }

                    override fun onDataChange(snapshot: DataSnapshot?) {
                        LogCat.d(TAG, "onDataChange")
                        val data = snapshot?.getValue(SearchData::class.java)
                        data?.let {
                            it.keyWord?.let { key ->
                                datas[key] = it
                                updateToRepository(it, mallName)
                            }
                        }
                    }

                })
    }

    private fun updateToRepository(data: SearchData, mallName: String) {
        val searchItem = SearchItem(data.keyWord, mallName)
        searchItem.lastSearchTime = data.lastUpdateTime
        for (item in data.items.filter { it.mallName == mallName }) {
            searchItem.items.add(ShopItem(item))
        }
        SearchItemRealmManager.save(searchItem)
    }

    fun getDate(dateString: String?): Long {
        var time: Long? = null
        try {
            time = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US).parse(dateString).time
        } catch (e: Exception) {
            LogCat.e(TAG, e.toString())
        } finally {
            if (time == null) {
                time = System.currentTimeMillis()
            }
        }
        return time
    }

    class LoadDataTask(val query: String, val mallName: String) : AsyncTask<Void, Void, SearchData>() {
        override fun doInBackground(vararg params: Void?): SearchData {
            val searchData = SearchData()
            searchData.keyWord = query
            val itemMap = TreeMap<Int, NItem>()
            for (i in 0..4) {
                val startIndex = i * NShopSearch.DISPLAY + 1
                val call = NaverSearchService.retrofit.create(NaverSearchService::class.java)
                        .getShopItemsForFireBase(query, startIndex, NShopSearch.DISPLAY)
                val response = call.execute()
                if (response.isSuccessful) {
                    val body = response.body()
                    body?.let {
                        it.items?.let { it2 ->
                            searchData.lastUpdateTime = getDate(it.lastBuildDate)
                            for (i in it2.indices) {
                                val item = it2[i]
                                item.position = startIndex + i
                                itemMap[item.position] = item
                            }
                        }
                    }
                }
            }
            searchData.items.addAll(itemMap.values)

            db.reference.child("items").child(query).setValue(searchData)
            return searchData
        }

        override fun onPostExecute(result: SearchData?) {
//            result?.let {
//                updateToRepository(it, mallName)
//            }
//            MainActivity.isRefreshing.set(false)
        }
    }

    fun queryDataFromNaver(query: String, mallName: String) {
        LoadDataTask(query, mallName).execute()
    }
}