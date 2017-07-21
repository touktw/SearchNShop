package co.esclub.searchnshop.viewmodel

import android.content.Intent
import android.databinding.ObservableField
import co.esclub.searchnshop.R
import co.esclub.searchnshop.activity.DetailActivity
import co.esclub.searchnshop.databinding.CardItem3Binding
import co.esclub.searchnshop.model.item.SearchItem
import co.esclub.searchnshop.util.Const
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by tae.kim on 16/07/2017.
 */

val SDF = SimpleDateFormat("yyyy.MM.dd hh:mm a", Locale.getDefault())

class SearchItemViewModel(val item: SearchItem, val binding: CardItem3Binding) {
    val imageUrl = ObservableField<String>()
    val itemSize = ObservableField<Int>(item.items.size)
    val deleteChecked = ObservableField<Boolean>(false)
    val title = ObservableField<String>("${item.keyWord},${item.mallName}")
    val content = ObservableField<String>(content())
    val time = ObservableField<String>(SDF.format(item.lastSearchTime))

    init {
        if (item.items.size > 0) {
            imageUrl.set(item.items[0].image)
        }
    }

    private fun content(): String {
        var content = binding.context?.getString(R.string.no_result)
        if (item.items.size > 0) {
            content = "["
            for (i in item.items) {
                content += i.position
                content += ", "
            }
            content = content.dropLast(2)
            content += "]"
        }

        return content ?: "[]"
    }

    fun itemSelect() {
        if (item.items.size > 0) {
            val intent = Intent(binding.context, DetailActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra(Const.KEY_ID, item.id)
            binding.context?.startActivity(intent)
        }
    }
}