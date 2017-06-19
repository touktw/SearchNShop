package co.esclub.searchnshop.activity

import android.content.DialogInterface
import android.content.Intent
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
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import co.esclub.searchnshop.BuildConfig
import co.esclub.searchnshop.R
import co.esclub.searchnshop.adapter.RecyclerAdapter
import co.esclub.searchnshop.model.RealmManager
import co.esclub.searchnshop.model.SearchItem
import co.esclub.searchnshop.net.NShopSearch
import co.esclub.searchnshop.ui.PromptProvider
import co.esclub.searchnshop.util.Const
import com.facebook.ads.AdView
import io.realm.Realm
import io.realm.RealmChangeListener
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
        if(USE_FB) {
            (adView as com.facebook.ads.AdView).destroy()
        }
        if(USE_ADMOB) {
            (adView as com.google.android.gms.ads.AdView).destroy()
        }
        super.onDestroy()
        RealmManager.get().removeChangeListener(this)
        RealmManager.get().close()
    }

    override fun onBackPressed() {
        if (isDeleteMode) {
            changeDeleteMode(false)
            return
        }
        super.onBackPressed()
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
            R.id.action_delete -> if (adapter?.checkedItemIds?.size ?: 0 > 0) showDeleteDialog()
            else
                return super.onOptionsItemSelected(item)
        }

        return true
    }

    var adView: View? = null
    private val USE_ADMOB = true
    private val USE_FB = false

    private fun adControl() {
//        adView.loadAd(AdRequest.Builder().build()) ADMOB
        if (USE_ADMOB) {
            adView = com.google.android.gms.ads.AdView(this)
            val ad = adView as com.google.android.gms.ads.AdView
            ad.adSize = com.google.android.gms.ads.AdSize.SMART_BANNER
            ad.adUnitId = "ca-app-pub-3759218081309192/1224243464"
            layoutAds.addView(ad)
            ad.loadAd(com.google.android.gms.ads.AdRequest.Builder().build())
        }
        if (USE_FB) {
            adView = com.facebook.ads.AdView(this, "1350063585070996_1350068838403804",
                    com.facebook.ads.AdSize.BANNER_HEIGHT_50)
            layoutAds.addView(adView)

            (adView as com.facebook.ads.AdView).loadAd()
        }
    }

    private fun initUI() {
        setSupportActionBar(toolbar)

        swipeRefreshLayout.setOnRefreshListener(this)
        swipeRefreshLayout.setColorSchemeResources(R.color.refresh_1,
                R.color.refresh_2,
                R.color.refresh_3)

        RealmManager.get().addChangeListener(this)
        adapter = RecyclerAdapter(this, messenger)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = adapter

        adControl()
    }

    fun search(onlyFirst: Boolean) {
        val searchItems = RealmManager.get().where(SearchItem::class.java).findAll()
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
                    val realm = RealmManager.get()
                    realm.beginTransaction()
                    for (item in results) {
                        realm.copyToRealmOrUpdate(item)
                    }
                    realm.commitTransaction()
                }
            }

        })
    }


    fun showDeleteDialog() {
        AlertDialog.Builder(this).setTitle(R.string.dialog_title_delete)
                .setMessage(getString(R.string.dialog_message_delete, adapter?.checkedItemIds?.size))
                .setPositiveButton(android.R.string.ok, DialogInterface.OnClickListener {
                    dialog, _ ->
                    adapter?.checkedItemIds?.let {
                        val realm = RealmManager.get()
                        realm.beginTransaction()
                        for (i in it) {
                            realm.where(SearchItem::class.java).equalTo("id", i).findAll()
                                    .deleteAllFromRealm()
                        }
                        realm.commitTransaction()
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
                            val realm = RealmManager.get()
                            realm.beginTransaction()
                            for (keyword in keywords) {
                                val item = SearchItem(keyword, mallName)
                                if (realm.where(SearchItem::class.java).equalTo(SearchItem.KEY_ID, item.id)
                                        .findAll().size == 0) {
                                    realm.copyToRealm(item)
                                }
                            }
                            if (checkBoxRemindMallName.isChecked) {
                                preference.edit().putString("mall_name", mallName).apply()
                            }

                            realm.commitTransaction()
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
            Const.MESSAGE_DELETE -> delete(msg.data?.getString(Const.KEY_ID))
            Const.MESSAGE_CHANGE_DELETE_MODE -> changeDeleteMode(true)

//            Const.MESSAGE_UPDATE -> updateOne(msg.data?.getString(Const.KEY_ID),
//                    msg.data?.getInt(Const.KEY_POSITION))
        }
        false
    }))


    private fun delete(id: String?) {
        val realm = RealmManager.get()
        realm.beginTransaction()
        realm.where(SearchItem::class.java).equalTo("id", id).findAll().deleteAllFromRealm()
        realm.commitTransaction()
    }

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
        if (pref.getBoolean("is_first${BuildConfig.VERSION_NAME}", true)) {
//            val realm = RealmManager.get()
//            realm.beginTransaction()
//            val item = SearchItem("남자4부반바지", "와이스토리지")
//            if (realm.where(SearchItem::class.java).equalTo(SearchItem.KEY_ID, item.id)
//                    .findAll().size == 0) {
//                realm.copyToRealm(item)
//            }
//            realm.commitTransaction()
//            adapter?.update(item.id)
            pref.edit().putBoolean("is_first${BuildConfig.VERSION_NAME}", false).apply()
            startActivityForResult(Intent(this, IntroActivity::class.java), REQ_INTRO)
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
//            recyclerView.layoutManager.findViewByPosition(0)?.let {
//                val viewHolder = recyclerView.getChildViewHolder(it) as RecyclerAdapter.ViewHolder
//                prompts.add(PromptProvider.get(this@MainActivity, R.id.action_sort,
//                        R.string.prompt_title_item, R.string.prompt_desc_item,
//                        R.color.textPrimary, R.color.textPrimary, R.color.colorPrimaryDark,
//                        null, null, null).
//                        setTarget(viewHolder.itemView))
//            }
            PromptProvider.show(prompts.iterator(), object : PromptProvider.OnEndPromptListener {
                override fun onEnd() {
                    promptShowed = false
                }
            })
        }, 500)
    }
}
