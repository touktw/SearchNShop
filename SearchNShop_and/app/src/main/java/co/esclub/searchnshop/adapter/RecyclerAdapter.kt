package co.esclub.searchnshop.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Message
import android.os.Messenger
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import co.esclub.searchnshop.R
import co.esclub.searchnshop.activity.DetailActivity
import co.esclub.searchnshop.model.RealmManager
import co.esclub.searchnshop.model.SearchItem
import co.esclub.searchnshop.net.NShopSearch
import co.esclub.searchnshop.util.Const
import com.bumptech.glide.Glide
import io.realm.RealmResults
import io.realm.Sort
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by tae.kim on 06/06/2017.
 */

class RecyclerAdapter(private val context: Context, val messenger: Messenger, //    var items: List<SearchItem>? = null
                      var items: RealmResults<SearchItem> = RealmManager.get().where
                      (SearchItem::class.java).findAll()) : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {
    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(viewGroup.context).inflate(R.layout.card_item3, null))
    }

    val checkedItemIds = ArrayList<String>()
    var deleteMode = false;
    var SDF = SimpleDateFormat("yyyy.MM.dd hh:mm a", Locale.getDefault())
    override fun onBindViewHolder(viewHolder: ViewHolder, pos: Int) {
        Log.d("###", "onBindViewHolder pos[${pos}]")
        val position = pos
        val item = items[position]
        val title = "${item.keyWord},${item.mallName}"
        var content = context.getString(R.string.no_result)
        val date = SDF.format(item.lastSearchTime)
        viewHolder.itemView.tag = item
        if (!deleteMode) {
            viewHolder.deleteOverlay.visibility = View.INVISIBLE
            viewHolder.deleteCheck = false
        }
        item.items?.let {
            if (it.size > 0) {
                viewHolder.thumbImage.visibility = View.VISIBLE
                Glide.with(context).load(Uri.parse(it[0].image)).into(viewHolder.thumbImage)
                viewHolder.itemView.findViewById(R.id.thumbImage).visibility = View.VISIBLE
                content = "["
                for (i in it) {
                    content += i.position
                    content += ", "
                }
                content = content.dropLast(2)
                content += "]"
            } else {
                viewHolder.thumbImage.visibility = View.GONE
            }
        }

        viewHolder.textTitle.text = title
        viewHolder.textContent.text = content
        viewHolder.textDate.text = date

        viewHolder.imgButtonUpdate.tag = viewHolder
        viewHolder.imgButtonUpdate.setOnClickListener { v ->
            val searchItems = ArrayList<SearchItem>()
            searchItems.add(SearchItem(item.keyWord, item.mallName))
            NShopSearch.search(searchItems, object : NShopSearch.Listener {
                override fun onPrepare() {
                    viewHolder.itemView?.findViewById(R.id.imgButtonUpdate)
                            ?.startAnimation(AnimationUtils.loadAnimation(context, R.anim.rotate))
                }

                override fun onComplete(results: List<SearchItem>?) {
                    viewHolder.itemView?.findViewById(R.id.imgButtonUpdate)?.clearAnimation()
                    results?.let {
                        val realm = RealmManager.get()
                        realm.beginTransaction()
                        if (it.isNotEmpty()) {
                            for (item in it) {
                                realm.copyToRealmOrUpdate(item)
                            }
                        }
                        realm.commitTransaction()
                    }
                }

            })
        }


        viewHolder.itemView.setOnClickListener {
            if (deleteMode) {
                viewHolder.deleteCheck()
            } else {
                if (item?.items?.size ?: 0 > 0) {
                    val intent = Intent(context, DetailActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.putExtra(Const.KEY_ID, item?.id)
                    context.startActivity(intent)
                }
            }
        }

        viewHolder.itemView.setOnLongClickListener {
            if (!deleteMode) {
                deleteMode = true
                checkedItemIds.clear()
                messenger.send(Message.obtain(null, Const.MESSAGE_CHANGE_DELETE_MODE))
                viewHolder.deleteCheck()
            }
            true
//            val builder = AlertDialog.Builder(context)
//            builder.setTitle(String.format("삭제[%s]", item?.id)).setMessage("삭제하시겠습니까?")
//                    .setPositiveButton("확인", { dialog: DialogInterface, _: Int ->
//                        val message = Message.obtain(null, Const.MESSAGE_DELETE)
//                        val data = Bundle();
//                        data.putString(Const.KEY_ID, item?.id);
//                        message.data = data;
//                        messenger.send(message)
//                        dialog.dismiss()
//                    })
//                    .setNegativeButton("취소", { dialog: DialogInterface, _: Int ->
//                        dialog.dismiss()
//                    }).show()
//            false
        }

    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var deleteCheck = false
        val deleteOverlay = itemView.findViewById(R.id.deleteOverlay)
        val textTitle = itemView.findViewById(R.id.textTitle) as TextView
        val textContent = itemView.findViewById(R.id.textContent) as TextView
        val textDate = itemView.findViewById(R.id.textDate) as TextView
        val imgButtonUpdate = itemView.findViewById(R.id.imgButtonUpdate) as ImageButton
        val thumbImage = itemView.findViewById(R.id.thumbImage) as ImageView

        fun deleteCheck() {
            val id = (itemView.tag as SearchItem).id
            if (deleteCheck) {
                deleteCheck = false
                deleteOverlay.visibility = View.INVISIBLE
                checkedItemIds.remove(id)
            } else {
                deleteCheck = true
                deleteOverlay.visibility = View.VISIBLE
                checkedItemIds.add(id)
            }
        }
    }

    var sort: Sort? = null
    fun changeSort() {
        when (sort) {
            null -> sort = Sort.ASCENDING
            Sort.ASCENDING -> sort = Sort.DESCENDING
            Sort.DESCENDING -> sort = Sort.ASCENDING
        }
        items = items.sort("keyWord", sort)
        notifyDataSetChanged()
    }

    fun disableDeleteMode() {
        deleteMode = false
        notifyDataSetChanged()
    }


}
