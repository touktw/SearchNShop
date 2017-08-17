package co.esclub.searchnshop.net

import android.os.AsyncTask
import co.esclub.searchnshop.model.firebase.FirebaseDBManager
import co.esclub.searchnshop.model.firebase.SearchResult
import co.esclub.searchnshop.model.firebase.ShopItem
import co.esclub.searchnshop.util.LogCat
import java.net.HttpURLConnection
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by tae.kim on 26/07/2017.
 */

object NaverSearchManager {
    val TAG = NaverSearchManager::class.java.simpleName
    fun querying(queries: List<String>?, listener: ((results: List<SearchResult>?) -> Unit)?) {
        LoadDataTask(queries, listener).execute()
    }

    private fun load(query: String): SearchResult? {
        LogCat.d(TAG, "load query:${query}")
        val searchResult = SearchResult()
        searchResult.keyword = query
        searchResult.shopItem = ArrayList<ShopItem>()
        var success = false
        val itemMap = TreeMap<Int, ShopItem>()
        for (i in 0..4) {
            val startIdx = i * 100 + 1
            val call = NaverSearchService.retrofit.create(NaverSearchService::class.java)
                    .getShopItemsForFireBase(query, startIdx, 100)
            val res = call.execute()
            if (res.isSuccessful) {
                success = true
                res.body()?.let {
                    it.items?.let { items ->
                        for (idx in items.indices) {
                            val item = items[idx]
                            item.position = startIdx + idx
                            itemMap[item.position] = item
                        }
                    }
                }
            }
        }
        if (success) {
            searchResult.shopItem?.addAll(itemMap.values)
        }
        return searchResult
    }

    class LoadDataTask(val queries: List<String>?,
                       val listener: ((results: List<SearchResult>?) -> Unit)?)
        : AsyncTask<Void, Void, List<SearchResult>>() {
        override fun doInBackground(vararg params: Void?): List<SearchResult> {
            val ret = ArrayList<SearchResult>()
            queries?.let {
                if (it.isNotEmpty()) {
                    for (query in it) {
                        if (query.isNotEmpty()) {
                            load(query)?.let { data ->
                                ret.add(data)
                            }
                        }
                    }
                }
            }
            return ret
        }

        override fun onPostExecute(result: List<SearchResult>) {
            listener?.invoke(result)
        }
    }
}