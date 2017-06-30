package co.esclub.searchnshop.net

import android.os.AsyncTask
import android.util.Log
import co.esclub.searchnshop.activity.MainActivity
import org.jsoup.Jsoup
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


/**
 * Created by tae.kim on 30/06/2017.
 */

class VersionCheckTask(val listener: Listener?) : AsyncTask<String, Void, String>() {
    interface Listener {
        fun onGetVersion(storeVersion: String?)
    }

    override fun doInBackground(vararg params: String?): String? {
        val newVersion = Jsoup.connect("https://play.google.com/store/apps/details?id=" +
                params[0] + "&hl=it")
                .timeout(30000)
                .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                .referrer("http://www.google.com")
                .get()
                .select("div[itemprop=softwareVersion]")
                .first()
                .ownText()
        Log.d("###", "version " + newVersion)
        return newVersion
    }

    override fun onPostExecute(result: String?) {
        listener?.onGetVersion(result)
    }

}

object StoreVersionChecker {
    fun getVersion(packageName: String, listener: VersionCheckTask.Listener?) {
        VersionCheckTask(listener).execute(packageName)
    }

    fun getStoreVersion(packageName: String) {
        Log.d("###", "getStoreVersion " + packageName)
        val call = PlayStoreService.retrofit.
                create(PlayStoreService::class.java).getShopItems(packageName)
        call.enqueue(object : Callback<PlayStore> {
            override fun onFailure(call: Call<PlayStore>?, t: Throwable?) {
                Log.d("###", "Failed " + t.toString())

            }

            override fun onResponse(call: Call<PlayStore>?, response: Response<PlayStore>?) {
                Log.d("###", "onResponse:" + response?.isSuccessful)
                response?.let {
                    if (it.isSuccessful) {
                        val playStore = response.body()
                        Log.d("###", "version:" + playStore?.versionName)
                    }
                }
            }

        })
    }
}