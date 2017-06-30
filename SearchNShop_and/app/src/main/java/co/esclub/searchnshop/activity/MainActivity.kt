package co.esclub.searchnshop.activity

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Messenger
import android.preference.PreferenceManager
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.CheckBox
import android.widget.EditText
import co.esclub.searchnshop.BuildConfig
import co.esclub.searchnshop.R
import co.esclub.searchnshop.adapter.RecyclerAdapter
import co.esclub.searchnshop.model.db.SearchItemRealmManager
import co.esclub.searchnshop.model.item.SearchItem
import co.esclub.searchnshop.model.repository.SearchItemRepository
import co.esclub.searchnshop.net.NShopSearch
import co.esclub.searchnshop.net.StoreVersionChecker
import co.esclub.searchnshop.net.VersionCheckTask
import co.esclub.searchnshop.ui.PromptProvider
import co.esclub.searchnshop.util.AdManager
import co.esclub.searchnshop.util.Const
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.RealmResults
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener, RealmChangeListener<Realm> {
    var adapter: RecyclerAdapter? = null
    var promptShowed = false

    override fun onChange(realm: Realm?) {
        adapter?.notifyDataSetChanged()
    }


    override fun onRefresh() {
        search(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initUI()
        startIntro()
    }

    override fun onDestroy() {
        adManager?.destroy()
        super.onDestroy()

        SearchItemRealmManager.removeChangeListener(this)
    }

    override fun onBackPressed() {
        if (isDeleteMode) {
            changeDeleteMode(false)
            return
        }
        super.onBackPressed()
    }

    fun checkStoreVersion(context: Context) {
        StoreVersionChecker.getVersion(packageName, object : VersionCheckTask.Listener {
            override fun onGetVersion(storeVersion: String?) {
                Log.d("###", "onGetVersion storeVersion:" + storeVersion + " currentVersion:" +
                        BuildConfig.VERSION_NAME)
                if (BuildConfig.VERSION_NAME != storeVersion) {
                    AlertDialog.Builder(context).setTitle(R.string.you_need_update)
                            .setMessage(R.string.you_need_update_desc)
                            .setPositiveButton(android.R.string.ok, { _: DialogInterface, _: Int ->
                                try {
                                    startActivity(Intent(Intent.ACTION_VIEW,
                                            Uri.parse("market://details?id=" + packageName)))
                                } catch (anfe: android.content.ActivityNotFoundException) {
                                    startActivity(Intent(Intent.ACTION_VIEW,
                                            Uri.parse("https://play.google.com/store/apps/details?id=" + packageName)))
                                }


                            })
                            .setNegativeButton(android.R.string.cancel, null)
                            .show()
                }
            }

        })
    }

    var isDeleteMode = false
    fun changeDeleteMode(isDeleteMode: Boolean) {
        this.isDeleteMode = isDeleteMode
        toolbar.title = resources.getString(
                if (isDeleteMode) R.string.delete_mode else R.string.app_name)
        if (!isDeleteMode) {
            adapter?.disableDeleteMode()
        }

        invalidateOptionsMenu()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(if (isDeleteMode) R.menu.menu_delete else R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
            R.id.action_settings -> startActivity(Intent(this, SettingsActivity::class.java))
            R.id.action_add -> if (!promptShowed) createDialog()
            R.id.action_sort -> if (!promptShowed) adapter?.changeSort()
            R.id.action_delete -> if (adapter?.checkedItemIds?.size ?: 0 > 0) showDeleteDialog(false)
            R.id.action_delete_all -> showDeleteDialog(true)
            R.id.action_test -> startActivity(Intent(this, MacroActivity::class.java))
        }

        return true
    }

    var adManager: AdManager? = null
    fun adControl() {
        if (adManager == null) {
            adManager = AdManager(AdManager.AdProvider.AD_MOB, this)
            layoutAds.addView(adManager?.adView)
            adManager?.load()
        }
    }


    private fun initUI() {
        setSupportActionBar(toolbar)

        swipeRefreshLayout.setOnRefreshListener(this)
        swipeRefreshLayout.setColorSchemeResources(R.color.refresh_1,
                R.color.refresh_2,
                R.color.refresh_3)

        SearchItemRealmManager.addChangeListener(this)
        adapter = RecyclerAdapter(this, messenger)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = adapter

        adControl()
    }

    fun search(onlyFirst: Boolean) {
        val searchItems: RealmResults<SearchItem> =
                SearchItemRepository.getAll() as RealmResults<SearchItem>
        val target = ArrayList<SearchItem>()
        for (searchItem in searchItems) {
            if (onlyFirst && searchItem.lastSearchTime > 0) {
                continue
            }
            target.add(SearchItem(searchItem.keyWord, searchItem.mallName))
        }
        NShopSearch.search(target, object : NShopSearch.Listener {
            override fun onPrepare() {
                if (!swipeRefreshLayout.isRefreshing) {
                    swipeRefreshLayout.isRefreshing = true
                }
            }

            override fun onComplete(results: List<SearchItem>?) {
                swipeRefreshLayout.isRefreshing = false
                results?.let {
                    SearchItemRepository.saveAll(results)
                }
            }

        })
    }


    fun showDeleteDialog(deleteAll: Boolean) {
        AlertDialog.Builder(this).setTitle(R.string.dialog_title_delete)
                .setMessage(if (deleteAll)
                    getString(R.string.dialog_message_delete_all)
                else
                    getString(R.string.dialog_message_delete, adapter?.checkedItemIds?.size))
                .setPositiveButton(android.R.string.ok, DialogInterface.OnClickListener {
                    dialog, _ ->
                    if (deleteAll) SearchItemRepository.deleteAll()
                    else adapter?.checkedItemIds?.let {
                        SearchItemRepository.deleteAll(it)
                    }
                    changeDeleteMode(false)
                    dialog.dismiss()
                })
                .setNegativeButton(android.R.string.cancel, DialogInterface.OnClickListener { dialog, _ ->
                    changeDeleteMode(false)
                    dialog.dismiss()
                })
                .show()
    }

    fun createDialog() {
        val preference = PreferenceManager.getDefaultSharedPreferences(this)
        val view = LayoutInflater.from(this).inflate(R.layout.add_item, null)
        val editKeyWord: EditText = view.findViewById(R.id.editKeyWord) as EditText
        val editMallName: EditText = view.findViewById(R.id.editMallName) as EditText
        val checkBoxRemindMallName = view.findViewById(R.id.checkBoxRemindMallName) as CheckBox
        checkBoxRemindMallName.isChecked = preference.getBoolean("remind_mall_name", false)
        checkBoxRemindMallName.setOnCheckedChangeListener { _, isChecked ->
            preference.edit().putBoolean("remind_mall_name", isChecked).apply()
        }

        if (checkBoxRemindMallName.isChecked) {
            editMallName.hint = PreferenceManager.getDefaultSharedPreferences(this).getString("mall_name", "")
        }
        AlertDialog.Builder(this)
                .setTitle(getString(R.string.new_item))
                .setMessage(getString(R.string.input_keyword_mall))
                .setView(view)
                .setPositiveButton(R.string.ok, { _: DialogInterface, _: Int ->
                    if (!editKeyWord.text.toString().isNullOrEmpty() &&
                            (!editMallName.text.toString().isNullOrEmpty() ||
                                    !editMallName.hint.isNullOrEmpty())) {
                        val keywordText = editKeyWord.text.toString()
                        val keywords = keywordText.split(",")
                        var mallName = editMallName.text.toString()
                        if (mallName.isNullOrEmpty()) {
                            mallName = editMallName.hint.toString()
                        }
                        if (keywords.isNotEmpty() && mallName.isNotEmpty()) {
                            for (keyword in keywords) {
                                val item = SearchItem(keyword.trim(), mallName)
                                Log.d("###", "save, id:" + item.id)
                                if (SearchItemRepository.get(item.id) == null) {
                                    Log.d("###", "save")
                                    SearchItemRepository.save(item)

                                    Log.d("###", "size:" + SearchItemRealmManager.getAll()?.size)
                                    SearchItemRepository.dump()
                                }
                            }
                            if (checkBoxRemindMallName.isChecked) {
                                preference.edit().putString("mall_name", mallName).apply()
                            }

                            search(true)
//                            Search(true, applicationContext).execute()
                            adapter?.notifyDataSetChanged()
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show()
    }

    val messenger = Messenger(Handler({ msg ->
        when (msg.what) {
            Const.MESSAGE_DELETE -> SearchItemRepository.delete(msg.data?.getString(Const.KEY_ID))
            Const.MESSAGE_CHANGE_DELETE_MODE -> changeDeleteMode(true)

//            Const.MESSAGE_UPDATE -> updateOne(msg.data?.getString(Const.KEY_ID),
//                    msg.data?.getInt(Const.KEY_POSITION))
        }
        false
    }))

    var syncTimeInMin = 0
    val syncHandler = Handler()
    val syncRunnable = Runnable {
        search(false)
        triggeringSync()
    }

    fun triggeringSync() {
        Log.d("###", "triggeringSync time[${syncTimeInMin}]")
        if (syncTimeInMin > 0) {
            syncHandler.postDelayed(syncRunnable, DateUtils.MINUTE_IN_MILLIS * syncTimeInMin)
        }
    }

    override fun onResume() {
        super.onResume()
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("auto_sync", false)) {
            val timeInMin = PreferenceManager.getDefaultSharedPreferences(this).
                    getString("sync_frequency", "0")
            if (TextUtils.isDigitsOnly(timeInMin)) {
                timeInMin.toInt().let {
                    syncTimeInMin = it
                    triggeringSync()
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        syncHandler.removeCallbacks(syncRunnable)
    }

    private val REQ_INTRO = 1;
    fun startIntro() {
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        if (!pref.getBoolean("is_intro_showed", false)) {
//            val realm = RealmManager.get()
//            realm.beginTransaction()
//            val item = SearchItem("남자4부반바지", "와이스토리지")
//            if (realm.where(SearchItem::class.java).equalTo(SearchItem.KEY_ID, item.id)
//                    .findAll().size == 0) {
//                realm.copyToRealm(item)
//            }
//            realm.commitTransaction()
//            adapter?.update(item.id)
            startActivityForResult(Intent(this, IntroActivity::class.java), REQ_INTRO)
            pref.edit().putBoolean("is_intro_showed", true).apply()
        } else {
            showPrompt()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQ_INTRO -> {
                showPrompt()
            }
        }
    }

    fun showPrompt() {
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        if (!pref.getBoolean("is_prompt_showed", false)) {
            promptShowed = true
            Handler().postDelayed({
                val prompts = LinkedList<MaterialTapTargetPrompt.Builder>()
                prompts.add(PromptProvider.get(this@MainActivity, R.id.action_add,
                        R.string.prompt_title_add, R.string.prompt_desc_add,
                        R.drawable.ic_add_white_24dp, R.color.textPrimary, null))
                prompts.add(PromptProvider.get(this@MainActivity, R.id.action_sort,
                        R.string.prompt_title_sort, R.string.prompt_desc_sort,
                        R.drawable.ic_sort_by_alpha_white_24dp, R.color.textPrimary, null))
                prompts.add(PromptProvider.get(this@MainActivity, R.id.action_sort,
                        R.string.prompt_title_item, R.string.prompt_desc_item,
                        null, null, null).setTarget(recyclerView.width / 2F,
                        toolbar.height + recyclerView.height / 2F))
                PromptProvider.show(prompts.iterator(), object : PromptProvider.OnEndPromptListener {
                    override fun onEnd() {
                        promptShowed = false
                        pref.edit().putBoolean("is_prompt_showed", true).apply()
                    }
                })
            }, 500)
        } else {
            checkStoreVersion(this)
        }

    }
}
