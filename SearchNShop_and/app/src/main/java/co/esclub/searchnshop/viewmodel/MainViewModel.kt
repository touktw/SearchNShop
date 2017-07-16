package co.esclub.searchnshop.viewmodel

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.databinding.Observable
import android.databinding.ObservableField
import android.net.Uri
import android.os.Handler
import android.preference.PreferenceManager
import android.support.v7.app.AlertDialog
import android.widget.Toast
import co.esclub.searchnshop.BuildConfig
import co.esclub.searchnshop.R
import co.esclub.searchnshop.adapter.RecyclerAdapter
import co.esclub.searchnshop.model.firebase.FirebaseDBManager
import co.esclub.searchnshop.model.item.SearchItem
import co.esclub.searchnshop.model.repository.SearchItemRepository
import co.esclub.searchnshop.net.NShopSearch
import co.esclub.searchnshop.net.StoreVersionChecker
import co.esclub.searchnshop.net.VersionCheckTask
import co.esclub.searchnshop.ui.PromptProvider
import co.esclub.searchnshop.util.Const
import co.esclub.searchnshop.util.LogCat
import co.esclub.searchnshop.util.Utils
import io.realm.RealmResults
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by tae.kim on 16/07/2017.
 */

class MainViewModel(activity: Activity) : ActivityViewModel(activity) {
    val TAG = MainViewModel::class.java.simpleName
    val isRefreshing = ObservableField<Boolean>(false)
    val isDeleteMode = ObservableField<Boolean>(false)
    val adapter = RecyclerAdapter(activity, this)
    val deleteItemIds = ArrayList<String>()
    override fun onCreate() {
        LogCat.d(TAG, "onCreate")
    }

    override fun onResume() {
        LogCat.d(TAG, "onResume")
    }

    override fun onPause() {
        LogCat.d(TAG, "onPause")
    }

    override fun onDestroy() {
        LogCat.d(TAG, "onDestroy")
    }

    var promptShowed: Boolean = false

    fun showPrompt() {
        val pref = PreferenceManager.getDefaultSharedPreferences(activity)
        if (!pref.getBoolean("is_prompt_showed", false)) {
            promptShowed = true
            Handler().postDelayed({
                val prompts = LinkedList<MaterialTapTargetPrompt.Builder>()
                prompts.add(PromptProvider.get(activity, R.id.action_add,
                        R.string.prompt_title_add, R.string.prompt_desc_add,
                        R.drawable.ic_add_white_24dp, R.color.textPrimary, null))
                prompts.add(PromptProvider.get(activity, R.id.action_sort,
                        R.string.prompt_title_sort, R.string.prompt_desc_sort,
                        R.drawable.ic_sort_by_alpha_white_24dp, R.color.textPrimary, null))
                prompts.add(PromptProvider.get(activity, R.id.action_sort,
                        R.string.prompt_title_item, R.string.prompt_desc_item,
                        null, null, null).setTarget(activity.recyclerView.width / 2F,
                        activity.toolbar.height + activity.recyclerView.height / 2F))
                PromptProvider.show(prompts.iterator(), object : PromptProvider.OnEndPromptListener {
                    override fun onEnd() {
                        promptShowed = false
                        pref.edit().putBoolean("is_prompt_showed", true).apply()
                    }
                })
            }, 500)
        } else {
            checkStoreVersion()
        }

    }

    fun checkStoreVersion() {
        StoreVersionChecker.getVersion(activity.packageName, object : VersionCheckTask.Listener {
            override fun onGetVersion(storeVersion: String?) {
                LogCat.d(Utils.TAG, "onGetVersion storeVersion:" + storeVersion + " currentVersion:" +
                        BuildConfig.VERSION_NAME)
                if (BuildConfig.VERSION_NAME != storeVersion) {
                    AlertDialog.Builder(activity).setTitle(R.string.you_need_update)
                            .setMessage(R.string.you_need_update_desc)
                            .setPositiveButton(android.R.string.ok, { _: DialogInterface, _: Int ->
                                try {
                                    activity.startActivity(Intent(Intent.ACTION_VIEW,
                                            Uri.parse("market://details?id=" + activity.packageName)))
                                } catch (anfe: android.content.ActivityNotFoundException) {
                                    activity.startActivity(Intent(Intent.ACTION_VIEW,
                                            Uri.parse("https://play.google" +
                                                    ".com/store/apps/details?id=" + activity.packageName)))
                                }


                            })
                            .setNegativeButton(android.R.string.cancel, null)
                            .show()
                }
            }

        })
    }

    fun showDeleteDialog(deleteAll: Boolean) {
        if (!deleteAll && deleteItemIds.size == 0) return
        AlertDialog.Builder(activity).setTitle(R.string.dialog_title_delete)
                .setMessage(if (deleteAll)
                    activity.getString(R.string.dialog_message_delete_all)
                else
                    activity.getString(R.string.dialog_message_delete, deleteItemIds.size))
                .setPositiveButton(android.R.string.ok, DialogInterface.OnClickListener {
                    dialog, _ ->
                    if (deleteAll) SearchItemRepository.deleteAll()
                    else SearchItemRepository.deleteAll(deleteItemIds)

                    isDeleteMode.set(false)
                    dialog.dismiss()
                })
                .setNegativeButton(android.R.string.cancel, DialogInterface.OnClickListener { dialog, _ ->
                    isDeleteMode.set(false)
                    dialog.dismiss()
                })
                .show()
    }

    fun onRefresh() {
        search()
    }

    fun search() {
        Toast.makeText(activity, R.string.sync_only_before_10min, Toast.LENGTH_LONG).show()
        val searchItems: RealmResults<SearchItem> =
                SearchItemRepository.getAll() as RealmResults<SearchItem>
        val target = ArrayList<SearchItem>()
        val currTime = System.currentTimeMillis()
        for (searchItem in searchItems) {
            if (currTime - searchItem.lastSearchTime > Const.SYNC_TIMEOUT_MILLIS) {
                target.add(SearchItem(searchItem.keyWord, searchItem.mallName))
            }
        }
        LogCat.d(TAG, "target size:${target.size}")
        for(item in target) {
            if(!item.keyWord.isNullOrEmpty() && !item.mallName.isNullOrEmpty()) {
                FirebaseDBManager.queryDataFromNaver(item.keyWord!!, item.mallName!!)
            }
        }
        NShopSearch.search(target, object : NShopSearch.Listener {
            override fun onPrepare() {
                isRefreshing.set(true)
            }

            override fun onComplete(results: List<SearchItem>?) {
                isRefreshing.set(false)
                results?.let {
                    SearchItemRepository.saveAll(results)
                }
            }

        })
    }

    fun onDeleteItemSelect(searchItemViewModel: SearchItemViewModel) {
        val isDeleteItem = searchItemViewModel.deleteChecked.get()
        searchItemViewModel.deleteChecked.set(!searchItemViewModel.deleteChecked.get())
    }

    fun onItemLongClick(searchItemViewModel: SearchItemViewModel): Boolean {
        LogCat.d(TAG, "onItemLongClick isDeleteMode:${isDeleteMode.get()}")
        deleteItemIds.clear()
        isDeleteMode.set(true)
        searchItemViewModel.deleteChecked.set(true)
        return true
    }

    fun onDeleteSelected(isChecked: Boolean, model: SearchItemViewModel) {
        LogCat.d(TAG, "onDeleteSelected isChecked:${isChecked}")
        model.item.id?.let {
            if (isChecked) {
                deleteItemIds.add(it)

            } else {
                deleteItemIds.remove(it)
            }
        }
    }

    init {
        isDeleteMode.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(p0: Observable?, p1: Int) {
                LogCat.d(TAG, "delete mode change[${(p0 as ObservableField<Boolean>).get()}]")
                val deleteMode = (p0 as ObservableField<Boolean>).get()
                if(!deleteMode) {
                    adapter.notifyDataSetChanged()
                }
                activity.invalidateOptionsMenu()
            }

        })
    }

}