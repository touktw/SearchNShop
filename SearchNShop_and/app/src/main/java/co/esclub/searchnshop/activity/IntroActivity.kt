package co.esclub.searchnshop.activity

import android.os.Build
import android.support.annotation.ColorRes
import co.esclub.searchnshop.R
import co.esclub.searchnshop.fragments.LastSlideFragment
import com.github.paolorotolo.appintro.AppIntro
import com.github.paolorotolo.appintro.AppIntroFragment

/**
 * Created by tae.kim on 13/06/2017.
 */

class IntroActivity : AppIntro() {
    companion object {

    }

    fun getColour(@ColorRes colorRes: Int): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            return resources.getColor(colorRes, this.theme)
        else
            return resources.getColor(colorRes)
    }

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        val bgColor = getColour(R.color.colorPrimary)
        val txtColor = getColour(R.color.textPrimary)

        // find your item from naver shopping
        addSlide(AppIntroFragment.newInstance(
                resources.getString(R.string.slide_title_1),
                resources.getString(R.string.slide_desc_1),
                R.drawable.intro_1, bgColor, txtColor, txtColor))
        // input keyword and mall name
        addSlide(com.github.paolorotolo.appintro.AppIntroFragment.newInstance(
                resources.getString(R.string.slide_title_2),
                resources.getString(R.string.slide_desc_2),
                R.drawable.intro_2, bgColor, txtColor, txtColor))
        // check your mall's ranking
        addSlide(com.github.paolorotolo.appintro.AppIntroFragment.newInstance(
                resources.getString(R.string.slide_title_3),
                resources.getString(R.string.slide_desc_3),
                R.drawable.intro_3, bgColor, txtColor, txtColor))
        addSlide(LastSlideFragment() as android.support.v4.app.Fragment)

        setDoneText(resources.getString(R.string.done))
        showSkipButton(false)

        setSeparatorColor(txtColor)
        setColorDoneText(txtColor)
        setNextArrowColor(txtColor)
        setIndicatorColor(txtColor, getColour(R.color.drGrey))
    }

    override fun onSkipPressed(currentFragment: android.support.v4.app.Fragment?) {
        super.onSkipPressed(currentFragment)
        showSkipButton(false)
        finish()
    }

    override fun onDonePressed(currentFragment: android.support.v4.app.Fragment?) {
        super.onDonePressed(currentFragment)
        finish()
    }

    override fun onSlideChanged(oldFragment: android.support.v4.app.Fragment?, newFragment: android.support.v4.app.Fragment?) {
        super.onSlideChanged(oldFragment, newFragment)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }
}