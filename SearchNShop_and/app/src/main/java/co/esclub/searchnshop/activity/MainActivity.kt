package co.esclub.searchnshop.activity

import android.content.DialogInterface
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import co.esclub.searchnshop.R
import co.esclub.searchnshop.databinding.ActivityMainBinding
import co.esclub.searchnshop.databinding.AddItemBinding
import co.esclub.searchnshop.model.db.SearchItemRealmManager
import co.esclub.searchnshop.model.item.SearchItem
import co.esclub.searchnshop.model.repository.SearchItemRepository
import co.esclub.searchnshop.net.NShopSearch
import co.esclub.searchnshop.util.AdManager
import co.esclub.searchnshop.viewmodel.AddItemModel
import co.esclub.searchnshop.viewmodel.MainViewModel
import io.realm.Realm
import io.realm.RealmChangeListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity(), RealmChangeListener<Realm> {
    val TAG = MainActivity::class.java.simpleName
    val model = MainViewModel(this)

    override fun onChange(realm: Realm?) {
        SearchItemRealmManager.getAll()?.let {
            model.adapter.addAll(it)
            model.adapter.notifyDataSetChanged()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)

        binding.model = model

        initUI()
        startIntro()
        model.onCreate()
    }

    override fun onDestroy() {
        adManager?.destroy()
        model.onDestroy()
        super.onDestroy()

        SearchItemRealmManager.removeChangeListener(this)
    }

    override fun onBackPressed() {
        if (model.isDeleteMode.get()) {

            model.isDeleteMode.set(false)
            return
        }
        super.onBackPressed()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(if (model.isDeleteMode.get())
            R.menu.menu_delete
        else
            R.menu.menu_main,
                menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
            R.id.action_settings -> startActivity(Intent(this, SettingsActivity::class.java))
            R.id.action_add -> if (!model.promptShowed) createDialog()
            R.id.action_sort -> if (!model.promptShowed) model.adapter.changeSort()
            R.id.action_delete -> model.showDeleteDialog(false)
            R.id.action_delete_all -> model.showDeleteDialog(true)
//            R.id.action_test -> startActivity(Intent(this, MacroActivity::class.java))
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

        swipeRefreshLayout.setColorSchemeResources(R.color.refresh_1,
                R.color.refresh_2,
                R.color.refresh_3)
        SearchItemRealmManager.addChangeListener(this)
        adControl()
    }

    fun searchNew(target: List<SearchItem>) {
        NShopSearch.search(target, object : NShopSearch.Listener {
            override fun onPrepare() {
                model.isRefreshing.set(true)
            }

            override fun onComplete(results: List<SearchItem>?) {
                model.isRefreshing.set(false)
                results?.let {
                    SearchItemRepository.saveAll(results)
                }
            }

        })
    }

    fun createDialog() {
        val view = LayoutInflater.from(this).inflate(R.layout.add_item, null)

        val model = AddItemModel(this, object : AddItemModel.Listener {
            override fun onSubmitted(target: List<SearchItem>) {
                searchNew(target)
                model.adapter.notifyDataSetChanged()
            }

        })
        val binding = DataBindingUtil.bind<AddItemBinding>(view)
        binding.model = model
        AlertDialog.Builder(this)
                .setTitle(getString(R.string.new_item))
                .setMessage(getString(R.string.input_keyword_mall))
                .setView(view)
                .setPositiveButton(R.string.ok, { _: DialogInterface, _: Int ->
                    model.submit(binding.editKeyWord.text.toString(),
                            binding.editMallName.text.toString())
                })
                .setNegativeButton(R.string.cancel, null)
                .show()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    private val REQ_INTRO = 1;
    fun startIntro() {
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        if (!pref.getBoolean("is_intro_showed", false)) {
            startActivityForResult(Intent(this, IntroActivity::class.java), REQ_INTRO)
            pref.edit().putBoolean("is_intro_showed", true).apply()
        } else {
            model.showPrompt()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQ_INTRO -> {
                model.showPrompt()
            }
        }
    }


}
