package co.esclub.searchnshop.activity

import android.net.Uri
import android.preference.PreferenceManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import co.esclub.searchnshop.R
import co.esclub.searchnshop.ui.PromptProvider
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_detail.*
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt
import java.util.*

class DetailActivity : AppCompatActivity() {

    /**
     * The [android.support.v4.view.PagerAdapter] that will provide
     * fragments for each of the sections. We use a
     * [FragmentPagerAdapter] derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * [android.support.v4.app.FragmentStatePagerAdapter].
     */
    private var mSectionsPagerAdapter: co.esclub.searchnshop.activity.DetailActivity.SectionsPagerAdapter? = null

    /**
     * The [ViewPager] that will host the section contents.
     */
    private var mViewPager: android.support.v4.view.ViewPager? = null

    private var itemId: String = ""

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(co.esclub.searchnshop.R.layout.activity_detail)

        itemId = intent.getStringExtra(co.esclub.searchnshop.util.Const.KEY_ID)
        if (itemId.isNullOrEmpty() && savedInstanceState != null) {
            itemId = savedInstanceState.getString(ITEM_ID)
        }
        val realm = co.esclub.searchnshop.model.RealmManager.get()
        val searchItem = realm.where(co.esclub.searchnshop.model.SearchItem::class.java).equalTo("id", itemId).findFirst()

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager, searchItem)

        // Set up the ViewPager with the sections adapter.
        container.adapter = mSectionsPagerAdapter
        keyword_label.text = searchItem.keyWord
    }

    private val ITEM_ID: String? = "ITEM_ID"

    override fun onSaveInstanceState(outState: android.os.Bundle?) {
        super.onSaveInstanceState(outState)
        outState!!.putString(ITEM_ID, itemId)
    }


    override fun onCreateOptionsMenu(menu: android.view.Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(co.esclub.searchnshop.R.menu.menu_detail, menu)
        return true
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId


        if (id == co.esclub.searchnshop.R.id.action_settings) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    /**
     * A placeholder fragment containing a simple adView.
     */
    class PlaceholderFragment() : android.support.v4.app.Fragment() {
        var itemId: String? = ""
        var shopItem: co.esclub.searchnshop.model.ShopItem? = null
        var position = 0;
        var test = 0

        constructor(searchItem: co.esclub.searchnshop.model.SearchItem?, position: Int) : this() {
            this.itemId = searchItem?.id
            this.position = position
            android.util.Log.d("###", "constructor itemId[${itemId}] position[${position}]")
        }

        private val ITEM_ID: String = "ITEM_ID"
        private val POSITION: String = "POSITION"
        private val KEYWORD: String = "KEYWORD"

        override fun onCreate(savedInstanceState: android.os.Bundle?) {
            super.onCreate(savedInstanceState)
            android.util.Log.d("###", "onCreate itemId[${itemId}] position[${position}]")
            if (savedInstanceState != null) {
                itemId = savedInstanceState.getString(ITEM_ID)
                position = savedInstanceState.getInt(POSITION)
            }
            android.util.Log.d("###", "onCreate itemId[${itemId}] position[${position}]")
            val realm = co.esclub.searchnshop.model.RealmManager.get()
            val searchItem = realm.where(co.esclub.searchnshop.model.SearchItem::class.java).equalTo("id", itemId).findFirst()
            searchItem?.let {
                it.items?.let { it1 ->
                    shopItem = it1[position]
                }
            }
        }

        fun showPrompt() {
            val prompts = LinkedList<MaterialTapTargetPrompt.Builder>()
            prompts.add(PromptProvider.get(activity, R.id.position_label,
                    R.string.prompt_title_detail_ranking, R.string.prompt_desc_detail_ranking,
                    null, null, null))
            prompts.add(PromptProvider.get(activity, R.id.title_label,
                    R.string.prompt_title_detail_title, R.string.prompt_desc_detail_title,
                    null, null, null))
            prompts.add(PromptProvider.get(activity, R.id.link_label,
                    R.string.prompt_title_detail_link, R.string.prompt_desc_detail_link,
                    null, null, null))
            PromptProvider.show(prompts.iterator(), object : PromptProvider.OnEndPromptListener {
                override fun onEnd() {
                }
            })
        }

        override fun onCreateView(inflater: android.view.LayoutInflater?, container: android.view.ViewGroup?,
                                  savedInstanceState: android.os.Bundle?): android.view.View? {
            val rootView = inflater!!.inflate(co.esclub.searchnshop.R.layout.fragment_detail, container, false)
            shopItem?.let {
                val textPosition = rootView.findViewById(R.id.position_label) as android.widget.TextView
                val textTitle = rootView.findViewById(R.id.title_label) as android.widget.TextView
                val textLink = rootView.findViewById(R.id.link_label) as android.widget.TextView
                val imageThumbnail = rootView.findViewById(co.esclub.searchnshop.R.id.thumbnail_image) as android.widget.ImageView

                textPosition.text = resources.getString(co.esclub.searchnshop.R.string.detail_postion, it.position)
//                textPosition.text = String.format("순위 : %d",  it.position)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    textTitle.text = android.text.Html.fromHtml(it.title, android.text.Html.FROM_HTML_MODE_LEGACY)
                } else {
                    textTitle.text = android.text.Html.fromHtml(it.title)
                }
                textLink.text = it.link

                Glide.with(this).load(Uri.parse(it.image)).into(imageThumbnail)
            }

            return rootView
        }

        override fun onActivityCreated(savedInstanceState: android.os.Bundle?) {
            super.onActivityCreated(savedInstanceState)
            android.util.Log.d("###", "onActivityCreated itemId[${itemId}] position[${position}]")
            if (savedInstanceState != null) {
                itemId = savedInstanceState.getString(ITEM_ID)
                position = savedInstanceState.getInt(POSITION)
            }
            android.util.Log.d("###", "onActivityCreated itemId[${itemId}] position[${position}]")
            val preference = PreferenceManager.getDefaultSharedPreferences(activity)
            if (preference.getBoolean("is_first_detail", true)) {
                preference.edit().putBoolean("is_first_detail", false).apply()
                showPrompt()
            }
        }

        override fun onSaveInstanceState(outState: android.os.Bundle?) {
            super.onSaveInstanceState(outState)
            outState?.putString(ITEM_ID, itemId)
            outState?.putInt(POSITION, position)
        }

        companion object {

            /**
             * Returns a new instance of this fragment for the given section
             * number.
             */
            fun newInstance(searchItem: co.esclub.searchnshop.model.SearchItem?, position: Int): co.esclub.searchnshop.activity.DetailActivity.PlaceholderFragment {
                val fragment = co.esclub.searchnshop.activity.DetailActivity.PlaceholderFragment(searchItem, position)
                return fragment
            }
        }
    }

    /**
     * A [FragmentPagerAdapter] that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    inner class SectionsPagerAdapter(fm: android.support.v4.app.FragmentManager, val searchItem: co.esclub.searchnshop.model.SearchItem?) :
            android.support.v4.app.FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): android.support.v4.app.Fragment {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return co.esclub.searchnshop.activity.DetailActivity.PlaceholderFragment.Companion.newInstance(searchItem, position)
        }

        override fun getCount(): Int {

            return searchItem?.items!!.size
        }
    }
}
