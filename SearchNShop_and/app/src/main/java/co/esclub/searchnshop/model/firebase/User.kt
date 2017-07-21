package co.esclub.searchnshop.model.firebase

import co.esclub.searchnshop.util.LogCat
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

/**
 * Created by tae.kim on 20/07/2017.
 */


@IgnoreExtraProperties
class User {
    val TAG = User::class.java.simpleName
    var id: String? = null
    var lastConnectTimeString: String? = null
    var lastConnectTimeMillis: Long? = null
    var lastUpdateTimeString: String? = null
    var queries = HashMap<String, UserItem>()

    fun dump() {
        LogCat.d(TAG, "userInfo")
        LogCat.d(TAG, "id:${id}")
        LogCat.d(TAG, "lastConnectTimeString:${lastConnectTimeString}")
        LogCat.d(TAG, "queries:${queries.size}")
    }
}

@IgnoreExtraProperties
class UserItem(var keyword: String?, var mallName: String?) {
    @Exclude
    fun id(): String {
        return keyword + "_" + mallName
    }
}