package co.esclub.searchnshop.viewmodel

import android.content.Context
import android.content.SharedPreferences
import android.databinding.ObservableField
import android.preference.PreferenceManager
import android.widget.CompoundButton
import co.esclub.searchnshop.model.item.SearchItem
import co.esclub.searchnshop.model.repository.SearchItemRepository

/**
 * Created by tae.kim on 16/07/2017.
 */

class AddItemModel(val context: Context, val listener: Listener?) {
    interface Listener {
        fun onSubmitted(target: List<SearchItem>)
    }

    val isChecked = ObservableField<Boolean>(isChecked())
    val hint = ObservableField<String>(if (isChecked()) preference().getString("mall_name", "") else "")
    private fun preference(): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }

    fun onCheckedChanged(button: CompoundButton, isChecked: Boolean) {
        preference().edit().putBoolean("remind_mall_name", isChecked).apply()
    }

    private fun isChecked(): Boolean {
        return preference().getBoolean("remind_mall_name", false)
    }

    fun submit(keywords: String?, mallName: String?) {
        keywords?.let { it ->
            var mall: String = ""
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
                val item = SearchItem(keyword.trim(), mall)
                if (SearchItemRepository.get(item.id) == null) {
//                    SearchItemRepository.save(item)
                    target.add(item)
                }
            }
            if (isChecked.get()) {
                preference().edit().putString("mall_name", mall).apply()
            }
            listener?.onSubmitted(target)
        }
    }
}