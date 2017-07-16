package co.esclub.searchnshop.util

import android.util.Log
import co.esclub.searchnshop.BuildConfig

/**
 * Created by tae.kim on 16/07/2017.
 */


object LogCat {
    val TAG = "EsClub"
    fun d(tag: String = "###", msg: String) {
        if (BuildConfig.DEBUG) Log.d("${TAG}[${tag}]", msg)
    }

    fun e(tag: String = "###", msg: String) {
        Log.e("${TAG}[${tag}]", msg)
    }
}