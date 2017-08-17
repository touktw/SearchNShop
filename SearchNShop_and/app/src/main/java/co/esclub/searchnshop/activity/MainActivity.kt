package co.esclub.searchnshop.activity

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LifecycleRegistry
import android.arch.lifecycle.LifecycleRegistryOwner
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.CheckBox
import co.esclub.searchnshop.BuildConfig
import co.esclub.searchnshop.R
import co.esclub.searchnshop.databinding.ActivityMainBinding
import co.esclub.searchnshop.model.db.SearchItemRealmManager
import co.esclub.searchnshop.util.AdManager
import co.esclub.searchnshop.viewmodel.MainViewModel
import io.realm.Realm
import io.realm.RealmChangeListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity(), RealmChangeListener<Realm>, LifecycleRegistryOwner {
    val lifecycleRegistry = LifecycleRegistry(this)
    override fun getLifecycle(): LifecycleRegistry {
        return lifecycleRegistry
    }

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
            R.id.action_add -> if (!model.promptShowed) model.addNew()
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

//        swipeRefreshLayout.setColorSchemeResources(R.color.refresh_1,
//                R.color.refresh_2,
//                R.color.refresh_3)
        SearchItemRealmManager.addChangeListener(this)
        adControl()
    }

    override fun onResume() {
        super.onResume()
        model.onResume()
        showNewVersionNotice()
    }

    override fun onPause() {
        super.onPause()
        model.onPause()
    }

    private val REQ_INTRO = 1;
    fun startIntro() {
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        if (!pref.getBoolean("is_intro_showed", false)) {
            startActivityForResult(Intent(this, IntroActivity::class.java), REQ_INTRO)
            pref.edit().putBoolean("is_intro_showed", true).apply()
        } else {
            model.showPrompt({
                showNewVersionNotice()
            })
        }
    }

    fun showNewVersionNotice() {
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        if (!pref.getBoolean("isShowed127Notice", false)) {
            val rootView = LayoutInflater.from(this).inflate(R.layout.checkbox, null)
            val checkBox = rootView.findViewById(R.id.checkbox) as CheckBox
            val padding = (resources.displayMetrics.density * 8 + 0.5f).toInt()
            checkBox.setText(R.string.dont_show_again)
            AlertDialog.Builder(this)
                    .setTitle(R.string.update_notice_title)
                    .setMessage(R.string.update_notice)
                    .setView(rootView)
                    .setPositiveButton(android.R.string.ok, { dialog, _ ->
                        pref.edit().putBoolean("isShowed127Notice", checkBox.isChecked).apply()
                        dialog.dismiss()
                    })
                    .show()

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQ_INTRO -> {
                model.showPrompt({
                    showNewVersionNotice()
                })
            }
        }
    }


}
