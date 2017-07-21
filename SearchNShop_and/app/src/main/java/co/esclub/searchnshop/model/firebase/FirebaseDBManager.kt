package co.esclub.searchnshop.model.firebase

import android.databinding.Observable
import android.os.AsyncTask
import android.text.format.DateUtils
import co.esclub.searchnshop.model.item.SearchItem
import co.esclub.searchnshop.model.repository.SearchItemRepository
import co.esclub.searchnshop.net.NaverSearchService
import co.esclub.searchnshop.util.LogCat
import co.esclub.searchnshop.util.UUIDFactory
import com.google.firebase.database.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by tae.kim on 13/07/2017.
 */

object FirebaseDBManager {
    val TAG = FirebaseDBManager::class.java.simpleName
    val datas = HashMap<String, SearchData>()
    val db = FirebaseDatabase.getInstance()
    var user: User = User()

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
                    updateToRepository(it)
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

    fun init() {
        UUIDFactory.uuid.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(p0: Observable?, p1: Int) {
                if (p0 === UUIDFactory.uuid) {
                    val uuid = p0.get()
                    if (!uuid.isNullOrEmpty()) {
                        initializeUserInfo(uuid)
                    }
                }
            }

        })
        val uuid = UUIDFactory.uuid.get()
        if (!uuid.isNullOrEmpty()) {
            initializeUserInfo(uuid)
        }
    }

    fun updateUser() {
        db.reference.child("users").child(user.id).setValue(user)
    }

    fun initializeUserInfo(uuid: String) {
        user.id = uuid
        user.lastConnectTimeMillis = System.currentTimeMillis()
        user.lastConnectTimeString = DateFormat.getDateTimeInstance().format(Date())
        SearchItemRepository.getAll()?.let {

            for (item in it.map { UserItem(it.keyWord, it.mallName) }) {
                user.queries.put(item.id(), item)
                loadOneShot(item.keyword!!)
            }
        }
        updateUser()
    }

    fun checkVersion(listener: (value: Int) -> Unit) {
        db.reference.child("version").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError?) {
                LogCat.e(TAG, "getVersion error:${error?.toString()}")
            }

            override fun onDataChange(snapshot: DataSnapshot?) {
                val value = snapshot?.value as Long
                LogCat.d(TAG, "version:${value}")
                listener.invoke(value.toInt())
            }
        })
    }

    fun loadOneShot(query: String) {
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
                                updateToRepository(it)
                            }
                        }
                    }

                })
    }

    private fun updateToRepository(data: SearchData) {
        LogCat.d(TAG, "updateToRepository query:${data.keyWord}")
        SearchItemRepository.getAll()?.let { searchItems ->
            for (searchItem in searchItems) {
                if (data.keyWord == searchItem.keyWord) {
                    SearchItemRepository.updateItem(searchItem, data)
                }
            }
        }
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

    fun load(query: String): SearchData? {
        val searchData = SearchData()
        var success = false
        searchData.keyWord = query
        val itemMap = TreeMap<Int, NItem>()
        for (i in 0..4) {
            val startIdx = i * 100 + 1
            val call = NaverSearchService.retrofit.create(NaverSearchService::class.java)
                    .getShopItemsForFireBase(query, startIdx, 100)
            val res = call.execute()
            if (res.isSuccessful) {
                success = true
                res.body()?.let {
                    searchData.lastUpdateTime = getDate(it.lastBuildDate)
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
            searchData.items.addAll(itemMap.values)
            db.reference.child("items").child(query).setValue(searchData)
        }
        return searchData
    }

    class LoadDataTask(val queries: List<String>?, val listener: (() -> Unit)?)
        : AsyncTask<Void, Void, List<SearchData>>() {
        override fun doInBackground(vararg params: Void?): List<SearchData> {
            val ret = ArrayList<SearchData>()
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

        override fun onPostExecute(result: List<SearchData>) {
            for (data in result) {
                updateToRepository(data)
            }
            listener?.invoke()
//            result?.let {
//                updateToRepository(it, mallName)
//            }
//            MainActivity.isRefreshing.set(false)
        }
    }

    fun queryDataFromNaver(queries: List<String>?, listener: (() -> Unit)?) {
        LoadDataTask(queries, listener).execute()
    }

    fun addNew(target: List<SearchItem>, listener: (() -> Unit)?) {
        for (item in target) {
            val data = datas[item.keyWord]
            if (data == null) {
                item.keyWord?.let {
                    queryDataFromNaver(Arrays.asList(it), listener)
                }
            } else {
                updateToRepository(data)
            }
        }
        addToUser(target.map { UserItem(it.keyWord, it.mallName) })
    }

    fun addToUser(items: List<UserItem>?) {
        items?.let {
            for (item in it) {
                user.queries.put(item.id(), item)
            }
            updateUser()
        }
    }

    fun deleteAll(deleteItems: List<String>?) {
        LogCat.d(TAG, "deleteAll")
        deleteItems?.let {
            for (id in it) {
                user.queries.remove(id)
            }
        }
        updateUser()
    }

    fun checkForUpdate(listener: (() -> Unit)?) {
        user.lastUpdateTimeString = DateFormat.getDateTimeInstance().format(Date())
        updateUser()
        queryDataFromNaver(datas.values
                .filter { System.currentTimeMillis() - it.lastUpdateTime > DateUtils.HOUR_IN_MILLIS }
                .map { it.keyWord!! }, {
            listener?.invoke()
        })
    }
}