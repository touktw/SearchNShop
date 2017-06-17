package co.esclub.searchnshop.net

import android.os.AsyncTask
import co.esclub.searchnshop.model.SearchItem
import co.esclub.searchnshop.model.SearchResult
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * Created by tae.kim on 17/06/2017.
 */


object NShopSearch {
    val DISPLAY = 100
    var WAIT = AtomicInteger()

    interface Listener {
        fun onPrepare()
        fun onComplete(results: List<SearchItem>?)
    }

    fun search(searchItems: List<SearchItem>, listener: Listener?) {
        WAIT.set(0)
        ExecuteTask(searchItems, listener).execute()
    }

    class ExecuteTask(val searchItems: List<SearchItem>, val listener: Listener?)
        : AsyncTask<Void, Void, Void>() {
        val results = ArrayList<SearchItem>()
        override fun doInBackground(vararg params: Void?): Void? {
            for (searchItem in searchItems) {
                results.add(searchItem)
                searchItem.isSuccess = true
                for (i in 0..10) {
                    val startIndex = i * DISPLAY + 1
                    val call = NaverSearchService.retrofit.create(NaverSearchService::class.java)
                            .getShopItems(searchItem.keyWord, startIndex, DISPLAY)
                    WAIT.incrementAndGet()
                    call.enqueue(CallBack(searchItem, startIndex, WAIT))
                }
            }

            while (WAIT.get() > 0) {
                Thread.sleep(10)
            }
            return null
        }

        override fun onPreExecute() {
            super.onPreExecute()
            listener?.onPrepare()
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            val ret = ArrayList<SearchItem>()
            for (item in results) {
                if (item.isSuccess) {
                    item.items.addAll(item.itemTreeMap.values)
                    ret.add(item)
                }
            }
            listener?.onComplete(ret)
        }

    }

    class CallBack(val searchItem: SearchItem, private val startIndex: Int, val wait: AtomicInteger)
        : Callback<SearchResult> {
        override fun onResponse(call: Call<SearchResult>?, response: Response<SearchResult>?) {
            response?.let {
                if (it.isSuccessful) {
                    val body = it.body()
                    var i = startIndex
                    for (item in body!!.items!!) {
                        if (item.mallName == searchItem.mallName) {
                            item.position = i
                            searchItem.itemTreeMap.put(i, item)
                        }
                        i++
                    }
                    searchItem.lastSearchTime = System.currentTimeMillis()
                }
            }
            wait.decrementAndGet()
        }

        override fun onFailure(call: Call<SearchResult>?, t: Throwable?) {
            searchItem.isSuccess = false
            wait.decrementAndGet()
        }
    }
}