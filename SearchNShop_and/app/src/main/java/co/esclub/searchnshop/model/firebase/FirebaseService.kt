package co.esclub.searchnshop.model.firebase

import android.os.AsyncTask
import co.esclub.searchnshop.net.NaverSearchManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.*

/**
 * Created by tae.kim on 16/07/2017.
 */

object FirebaseService {
    val auth = FirebaseAuth.getInstance()
    var isSignIn = false

    fun signIn(listener: (isSignIn: Boolean) -> Unit) {
        auth.signInAnonymously()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        isSignIn = true
                    }
                    listener.invoke(isSignIn)
                }
    }
}


object FirebaseManager {
    val clazz = FirebaseFunctionsService.retrofit.create(FirebaseFunctionsService::class.java)
    fun getItem(keyword: String?, updateTime: Long, manager:FirebaseDBManager?) {
        keyword?.let {
            NetworkTask<SearchResult>({
                val res = clazz.getSearchItem(it).execute()
                if (res.isSuccessful) {
                    when (res.code()) {
                        200 -> {
                            val result = res.body()
                            result?.let {
                                manager?.updateRepository(it)
                            }
                        }
                        204 -> {
                            NaverSearchManager.querying(Arrays.asList(keyword), {

                            })
                        }
                    }
                }
            }).execute()
        }
    }

    fun getVersion(listener: ((version: Int) -> Unit)?) {
        NetworkTask<Version>({
            val res = clazz.version().execute()
            if (res.isSuccessful && res.code() == 200) {
                val result = res.body()
                result?.let {
                    listener?.invoke(it.version ?: 0)
                    return@NetworkTask
                }
            }
            listener?.invoke(0)
        }).execute()
    }

    fun getTime(listener: ((time: Long) -> Unit)?) {
        NetworkTask<Time>({
            val res = clazz.getDate().execute()
            if (res.isSuccessful && res.code() == 200) {
                val result = res.body()
                result?.let {
                    listener?.invoke(it.time ?: -1)
                    return@NetworkTask
                }
            }
            listener?.invoke(-1)
        }).execute()
    }

    class NetworkTask<RET_T>(val netListener: (() -> Unit)?)
        : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void?): Void? {
            netListener?.invoke()
            return null
        }

    }
}

interface FirebaseFunctionsService {
    @GET("version")
    fun version(): Call<Version>

    @GET("time")
    fun getDate(): Call<Time>

    @GET("item")
    fun getSearchItem(@Query("keyword") keyword: String): Call<SearchResult>

    companion object {
        val retrofit = Retrofit.Builder()
                .baseUrl("https://us-central1-keyword-searcher-1e1cb.cloudfunctions.net/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
    }
}