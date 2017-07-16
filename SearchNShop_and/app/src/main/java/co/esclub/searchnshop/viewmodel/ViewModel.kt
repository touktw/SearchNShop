package co.esclub.searchnshop.viewmodel

import android.app.Activity

/**
 * Created by tae.kim on 16/07/2017.
 */

abstract class ActivityViewModel(val activity: Activity) : ViewModel

interface ViewModel {
    fun onCreate()
    fun onResume()
    fun onPause()
    fun onDestroy()
}