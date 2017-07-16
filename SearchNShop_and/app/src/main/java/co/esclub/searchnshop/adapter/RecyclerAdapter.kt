package co.esclub.searchnshop.adapter

import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import co.esclub.searchnshop.R
import co.esclub.searchnshop.databinding.CardItem3Binding
import co.esclub.searchnshop.model.item.SearchItem
import co.esclub.searchnshop.model.repository.SearchItemRepository
import co.esclub.searchnshop.util.LogCat
import co.esclub.searchnshop.viewmodel.MainViewModel
import co.esclub.searchnshop.viewmodel.SearchItemViewModel
import io.realm.Sort
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by tae.kim on 06/06/2017.
 */

class RecyclerAdapter(private val context: Context, val mainViewModel: MainViewModel)
    : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {
    val TAG = RecyclerAdapter::class.java.simpleName
    val items = ArrayList<SearchItem>()
    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(viewGroup.context).inflate(R.layout.card_item3, null))
    }

    fun addAll(items: List<SearchItem>) {
        LogCat.d(TAG, "addAll size:" + items.size)
        this.items.clear()
        this.items.addAll(items)
        sorting()
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, pos: Int) {
        LogCat.d(TAG, "onBindViewHolder pos[${pos}]")

        val position = pos
        val item = items.get(position)

        val binding = DataBindingUtil.bind<CardItem3Binding>(viewHolder.itemView)
        binding.context = context
        binding.model = SearchItemViewModel(item, binding)
        binding.mainViewModel = mainViewModel

        viewHolder.itemView.tag = item
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    var sort: Sort? = null
    private fun sorting() {
        Collections.sort(items, object : Comparator<SearchItem> {
            override fun compare(o1: SearchItem?, o2: SearchItem?): Int {
                if (sort == Sort.ASCENDING) return o1?.keyWord?.compareTo(o2?.keyWord ?: "") ?: 0
                else return o2?.keyWord?.compareTo(o1?.keyWord ?: "") ?: 0
            }

        })
    }

    fun changeSort() {
        when (sort) {
            null -> sort = Sort.ASCENDING
            Sort.ASCENDING -> sort = Sort.DESCENDING
            Sort.DESCENDING -> sort = Sort.ASCENDING
        }
        sorting()
        notifyDataSetChanged()
    }

    init {
        SearchItemRepository.getAll()?.let {
            addAll(it)
        }
    }
}
