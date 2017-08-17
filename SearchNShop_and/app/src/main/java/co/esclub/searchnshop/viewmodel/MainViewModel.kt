package co.esclub.searchnshop.viewmodel

import android.app.Activity
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleRegistry
import android.arch.lifecycle.OnLifecycleEvent
import android.content.DialogInterface
import android.content.Intent
import android.databinding.DataBindingUtil
import android.databinding.Observable
import android.databinding.ObservableField
import android.net.Uri
import android.os.Handler
import android.os.HandlerThread
import android.preference.PreferenceManager
import android.support.v7.app.AlertDialog
import android.text.format.DateUtils
import android.view.LayoutInflater
import co.esclub.searchnshop.BuildConfig
import co.esclub.searchnshop.R
import co.esclub.searchnshop.adapter.RecyclerAdapter
import co.esclub.searchnshop.databinding.AddItemBinding
import co.esclub.searchnshop.model.firebase.FirebaseDBManager
import co.esclub.searchnshop.model.firebase.FirebaseService
import co.esclub.searchnshop.model.item.SearchItem
import co.esclub.searchnshop.model.repository.SearchItemRepository
import co.esclub.searchnshop.ui.PromptProvider
import co.esclub.searchnshop.util.LogCat
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by tae.kim on 16/07/2017.
 */

class MainViewModel(private val activity: Activity) : ActivityViewModel() {


    val TAG = MainViewModel::class.java.simpleName
    val isDeleteMode = ObservableField<Boolean>(false)
    val adapter = RecyclerAdapter(activity, this)
    val deleteItemIds = ArrayList<String>()
    var firebaseManager: FirebaseDBManager? = null
    val updater = UpdateService()

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        LogCat.d(TAG, "onCreate")
        FirebaseService.signIn({ isSignIn ->
            if (isSignIn) {
                checkVersion()
                firebaseManager = FirebaseDBManager()
            }
        })
    }

    private fun checkVersion() {
        val lastCheckTime = PreferenceManager.getDefaultSharedPreferences(activity)
                .getLong("lastVersionCheckTime", 0)
        val currTime = System.currentTimeMillis()
        if (currTime - lastCheckTime > DateUtils.DAY_IN_MILLIS) {
            firebaseManager?.checkVersion { v ->
                LogCat.d(TAG, "onCreate version:${v}")
                if (BuildConfig.VERSION_CODE < v) {
                    LogCat.d(TAG, "need to update")
                    showNeedUpdate()
                    PreferenceManager.getDefaultSharedPreferences(activity)
                            .edit().putLong("lastVersionCheckTime", currTime).apply()
                }
            }
        }
    }

    fun showNeedUpdate() {
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

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        LogCat.d(TAG, "onResume")
        updater.run()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
        LogCat.d(TAG, "onPause")
        updater.stop()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        LogCat.d(TAG, "onDestroy")
    }

    var promptShowed: Boolean = false

    fun showPrompt(listener: (() -> Unit)?) {
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
                        listener?.invoke()
                    }
                })
            }, 500)
        }
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
                    var deleteItems: List<String> = deleteItemIds
                    if (deleteAll) {
                        SearchItemRepository.getAll()?.map { it.id!! }?.let {
                            deleteItems = it
                        }
                    }
                    SearchItemRepository.deleteAll(deleteItems)
                    firebaseManager?.delete(deleteItems)

                    isDeleteMode.set(false)
                    dialog.dismiss()
                })
                .setNegativeButton(android.R.string.cancel, DialogInterface.OnClickListener { dialog, _ ->
                    isDeleteMode.set(false)
                    dialog.dismiss()
                })
                .show()
    }

    fun onDeleteItemSelect(searchItemViewModel: SearchItemViewModel) {
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
                if (p0 === isDeleteMode) {
                    LogCat.d(TAG, "delete mode change[${p0.get()}]")
                    val deleteMode = p0.get()
                    if (!deleteMode) {
                        adapter.notifyDataSetChanged()
                    }
                    activity.invalidateOptionsMenu()
                }
            }

        })
    }

    fun addNew() {
        val view = LayoutInflater.from(activity).inflate(R.layout.add_item, null)
        val model = AddItemModel(activity, object : AddItemModel.Listener {
            override fun onSubmitted(target: List<SearchItem>) {
                firebaseManager?.add(target)
                adapter.notifyDataSetChanged()
            }

        })
        val binding = DataBindingUtil.bind<AddItemBinding>(view)
        binding.model = model
        AlertDialog.Builder(activity)
                .setTitle(activity.getString(R.string.new_item))
                .setMessage(activity.getString(R.string.input_keyword_mall))
                .setView(view)
                .setPositiveButton(R.string.ok, { _: DialogInterface, _: Int ->
                    model.submit(binding.editKeyWord.text.toString(),
                            binding.editMallName.text.toString())
                })
                .setNegativeButton(R.string.cancel, null)
                .show()
    }


    inner class UpdateService {
        val UPDATE_DELAY_MIN = DateUtils.MINUTE_IN_MILLIS
        var updateDelay = UPDATE_DELAY_MIN
        val TAG = UpdateService::class.java.simpleName
        val thread: HandlerThread
        val handler: Handler

        init {
            thread = HandlerThread(TAG)
            thread.start()
            handler = Handler(thread.looper)
        }


        val callback: Runnable = Runnable {
            run()
        }

        // 5min ~ 10min
        fun run() {
            firebaseManager?.checkUpdate()
            val delay = (60 + Random().nextInt(60)) * DateUtils.SECOND_IN_MILLIS
            LogCat.d(TAG, "run update delay ${delay}ms")
            handler.postDelayed(callback, delay)
        }

        fun stop() {
            handler.removeCallbacks(callback)
        }


    }
}