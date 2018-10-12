package co.esclub.searchnshop.viewmodel

import android.content.Context
import android.content.SharedPreferences
import android.databinding.ObservableField
import android.preference.PreferenceManager
import android.widget.CompoundButton
import co.esclub.searchnshop.model.item.SearchItem
import co.esclub.searchnshop.model.repository.SearchItemRepository
import co.esclub.searchnshop.util.LogCat

/**
 * Created by tae.kim on 16/07/2017.
 */

class AddItemModel(val context: Context, val listener: Listener?) {
    val TAG = AddItemModel::class.java.simpleName

    interface Listener {
        fun onSubmitted(target: List<SearchItem>)
    }

    val isChecked = ObservableField<Boolean>(isChecked())
    val hint = ObservableField<String>(if (isChecked()) preference().getString("mall_name", "") else "")
    private fun preference(): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }

    fun onCheckedChanged(button: CompoundButton, isChecked: Boolean) {
        this.isChecked.set(isChecked)
    }

    private fun isChecked(): Boolean {
        val isChecked = preference().getBoolean("remind_mall_name", false)
        LogCat.d(TAG, "isChecked:${isChecked}")
        return isChecked
    }

    fun submit(keywords: String?, mallName: String?) {
        keywords?.let { it ->
            var mall: String
            if (mallName.isNullOrEmpty()) {
                val h = hint.get()
                if (h.isNullOrEmpty()) return
                mall = h
            } else {
                mall = mallName!!
            }

            val keywordArray = it.split(",")
            val target = ArrayList<SearchItem>()
            for (keyword in keywordArray) {
                if (keyword.isNotEmpty() && keyword.isNotBlank()) {
                    val item = SearchItem(keyword, mall)
                    if (SearchItemRepository.get(item.id) == null) {
                        SearchItemRepository.save(item)
                        target.add(item)
                    }
                }
            }
            val isChecked = this.isChecked.get()
            val isNotEmpty = mall.isNotEmpty()
            LogCat.d(TAG, "submit isChecked:${isChecked} isNotEmpty:${isNotEmpty}")
            if (isChecked && isNotEmpty) {
                preference().edit().putString("mall_name", mall).apply()
                preference().edit().putBoolean("remind_mall_name", isChecked).apply()
            }
            listener?.onSubmitted(target)
        }
    }
}