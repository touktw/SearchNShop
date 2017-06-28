package co.esclub.searchnshop.util

import android.text.format.DateUtils

/**
 * Created by tae.kim on 08/06/2017.
 */

object Const {
    val MESSAGE_DELETE = 1
    val MESSAGE_CREATE = 2
    val MESSAGE_UPDATE = 3
    val MESSAGE_CHANGE_DELETE_MODE = 4
    val KEY_ID = "id"
    val KEY_POSITION = "position"

    val SYNC_TIMEOUT_MILLIS = DateUtils.MINUTE_IN_MILLIS * 10
}
