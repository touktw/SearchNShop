package co.esclub.searchnshop.util

import android.content.Context
import android.databinding.Observable
import android.databinding.ObservableField
import android.os.AsyncTask
import android.preference.PreferenceManager
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import java.util.*

/**
 * Created by tae.kim on 20/07/2017.
 */

object UUIDFactory {
    val TAG = UUIDFactory::class.java.simpleName
    val uuid = ObservableField<String>()

    fun init(context: Context) {
        uuid.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(observable: Observable?, p1: Int) {
                if (observable === uuid) {
                    observable.get().let {
                        if (!it.isNullOrEmpty()) {
                            PreferenceManager.getDefaultSharedPreferences(context)
                                    .edit().putString("UUID", it).apply()
                        }
                    }
                }
            }

        })
        uuid.set(PreferenceManager.getDefaultSharedPreferences(context).getString("UUID", ""))
        if (uuid.get().isNullOrEmpty()) {
            AdIdLoadTask.execute(context)
        }
    }

    object AdIdLoadTask : AsyncTask<Context?, Void, Void>() {
        override fun doInBackground(vararg params: Context?): Void? {
            val context = params[0]
            val randUUID = UUID.randomUUID()
            try {
                val adId = AdvertisingIdClient.getAdvertisingIdInfo(context)?.id
                uuid.set("${randUUID}_${adId}")
            } catch (e: Exception) {
                uuid.set(randUUID.toString())
            }

            LogCat.d(TAG, "UUID[${uuid.get()}]")
            return null
        }
    }
}