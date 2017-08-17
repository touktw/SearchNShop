package co.esclub.searchnshop.model.firebase

import android.text.format.DateUtils
import co.esclub.searchnshop.model.item.SearchItem
import co.esclub.searchnshop.model.repository.SearchItemRepository
import co.esclub.searchnshop.net.NaverSearchManager
import co.esclub.searchnshop.util.LogCat
import co.esclub.searchnshop.util.UUIDFactory
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.DateFormat
import java.util.*
import kotlin.collections.HashMap

/**
 * Created by tae.kim on 13/07/2017.
 */

class FirebaseDBManager {
    val MAX_KEYWORD_CNT = 20
    val TAG = FirebaseDBManager::class.java.simpleName
    val db = FirebaseDatabase.getInstance()
    val user: User
    val itemRef = db.reference.child("searchItems")
    val userRef = db.reference.child("users")
    val itemListeners = HashMap<String, ItemListener>()

    init {
        user = User(UUIDFactory.uuid.get())
        user.connectTime = System.currentTimeMillis()
        user.connectTimeString = DateFormat.getDateTimeInstance().format(Date())
        SearchItemRepository.getAll()?.let {
            for (item in it.map { UserQuery(it.keyWord?.trim()?.replace(" ", ""), it.mallName) }) {
                user.userQueries[item.id] = item
                addItemListener(item.keyword)
            }
        }
        updateUser()
    }

    inner class ItemListener(val keyword: String) : ValueEventListener {
        override fun onCancelled(error: DatabaseError?) {
            LogCat.e(TAG, "item:${keyword} error:${error}")
        }

        override fun onDataChange(snapshot: DataSnapshot?) {
            val item = snapshot?.getValue(SearchResult::class.java)
            LogCat.d(TAG, "ItemListener onDataChange keyword:${item?.keyword} isNull:${item == null}")
            if (item == null) {
                // server doesnt has item, get item from naver
                queryingItem(keyword)
            } else {
                item.dump()
                updateRepository(item)
            }
        }

    }

    private fun queryingItem(keyword: String?) {
        LogCat.d(TAG, "queryingItem:${keyword}")
        keyword?.let {
            val query = it.trim().replace(" ", "")
            FirebaseManager.getTime { time ->
                NaverSearchManager.querying(Arrays.asList(query), { results ->
                    results?.let {
                        LogCat.d(TAG, "get results, time:${time}, size:${it.size}")
                        for (result in it) {
                            result.updateTime = time
                            updateItem(result)
                        }
                    }
                })
            }
        }
    }

    private fun loadItem(keyword: String?) {
        LogCat.d(TAG, "loadItem:${keyword}")
        keyword?.let {
            val key = keyword.trim().replace(" ", "")
            FirebaseManager.getTime { t ->
                FirebaseManager.getItem(key, t, this)
            }
        }
    }

    private fun addItemListener(keyword: String?) {
        LogCat.d(TAG, "addItemListener keyword:${keyword}")
        keyword?.let {
            val key = it.trim().replace(" ", "")
            if (!itemListeners.containsKey(key)) {
                val listener = ItemListener(key)
                itemRef.child(key).addValueEventListener(listener)
                itemListeners[key] = listener
            } else {
                loadItem(keyword)
            }
        }
    }

    private fun removeItemListener(keyword: String?) {
        LogCat.d(TAG, "removeItemListener keyword:${keyword}")
        keyword?.let {
            val key = it.trim().replace(" ", "")
            itemListeners[key]?.let {
                itemRef.child(keyword).removeEventListener(it)
                itemListeners.remove(key)
            }
        }
    }

    fun updateRepository(result: SearchResult?) {
        LogCat.d(TAG, "updateToRepository query:${result?.keyword}")
        result?.let {
            SearchItemRepository.getAll()?.let {
                for (item in it.filter { it.keyWord?.trim()?.replace(" ", "") == result.keyword }) {
                    SearchItemRepository.updateItem(item, result)
                }
            }
        }
    }

    fun checkVersion(listener: ((value: Int) -> Unit)?) {
        FirebaseManager.getVersion { v ->
            listener?.invoke(v)
        }
    }

    fun delete(idList: List<String>?) {
        LogCat.d(TAG, "delete")
        idList?.let {
            for (id in it) {
                val keyword = user.userQueries[id]?.keyword
                SearchItemRepository.getAll()
                        ?.filter { it.keyWord?.trim()?.replace(" ", "") == keyword }?.let {
                    if (it.isEmpty()) {
                        //no more keyword in repository
                        removeItemListener(keyword)
                    }
                }
//                user.userQueries.remove(id)
//                val keywordMap = user.userQueries.values
//                        .filter { it.keyword?.trim()?.replace(" ", "") == keyword }
//                val isEmpty = keywordMap.isEmpty()
//                for (item in keywordMap) {
//                    LogCat.d(TAG, "k:${item.keyword}")
//                }
//                LogCat.d(TAG, "isEmpty:${isEmpty}")
//                if (isEmpty) {
//                    // no more has this keyword
//                    removeItemListener(keyword)
//                }
            }
//            updateUser()
        }
    }

    fun add(searchItems: List<SearchItem>?) {
        LogCat.d(TAG, "add")
        searchItems?.let {
            // update user
            for (item in it) {
                LogCat.d(TAG, "search item keyword:${item.keyWord}")
                val userItem = UserQuery(item.keyWord, item.mallName)
                user.userQueries.put(userItem.id, userItem)
                addItemListener(item.keyWord)
            }
            updateUser()
        }

    }

    fun updateUser() {
        userRef.child(user.id).setValue(user)
    }

    fun updateItem(result: SearchResult?) {
        LogCat.d(TAG, "updateItem:${result?.keyword} time:${result?.updateTime}")
        result?.let {
            itemRef.child(it.keyword).setValue(it)
        }
    }

    private fun getUpdateNeededList(time: Long): List<SearchItem>? {
        SearchItemRepository.getAll()?.let {
            return it.filter { time - it.lastSearchTime > DateUtils.HOUR_IN_MILLIS }
        }
        return null
    }

    fun checkUpdate() {
        LogCat.d(TAG, "checkUpdate")
        user.updateTimeString = DateFormat.getDateTimeInstance().format(Date())
        updateUser()
        val currTime = System.currentTimeMillis()

        if (getUpdateNeededList(currTime)?.size ?: 0 > 0) {
            FirebaseManager.getTime {
                time ->
                getUpdateNeededList(time)?.let {
                    NaverSearchManager.querying(it.map { it.keyWord?.trim()?.replace(" ","")!! },{
                        results ->
                        results?.let{
                            for (item in it) {
                                item.updateTime = time
                                updateItem(item)
                            }
                        }
                    })
                }
            }
        }
    }
}