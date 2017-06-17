package co.esclub.searchnshop.ui

import android.app.Activity
import android.graphics.drawable.Drawable
import android.support.annotation.StringRes
import android.util.Log
import android.view.MotionEvent
import co.esclub.searchnshop.R
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt

/**
 * Created by tae.kim on 16/06/2017.
 */

object PromptProvider {

    interface OnEndPromptListener {
        fun onEnd()
    }

    fun show(iterator: MutableIterator<MaterialTapTargetPrompt.Builder>,
             onEndPromptListener: OnEndPromptListener?) {
        Log.d("###", "show iterator.hasNext[${iterator.hasNext()}]")
        if (iterator.hasNext()) {
            val prompt = iterator.next()
            prompt.setOnHidePromptListener(object : MaterialTapTargetPrompt.OnHidePromptListener {
                override fun onHidePromptComplete() {
                    show(iterator, onEndPromptListener)
                }

                override fun onHidePrompt(p0: MotionEvent?, p1: Boolean) {
                }
            }).show()
        } else {
            onEndPromptListener?.onEnd()
        }
    }


    fun get(activity: Activity, target: Int, primaryText: Any, secondaryTex: Any,
            icon: Any?, iconDrawableColourFilterRes: Int?,
            hidePromptListener: MaterialTapTargetPrompt.OnHidePromptListener?)
            : MaterialTapTargetPrompt.Builder {
        val p = MaterialTapTargetPrompt.Builder(activity)
        p.setTarget(target)

        if (primaryText is String) {
            p.setPrimaryText(primaryText)
        } else if (primaryText is @StringRes Int) {
            p.setPrimaryText(primaryText)
        }
        if (secondaryTex is String) {
            p.setSecondaryText(secondaryTex)
        } else if (secondaryTex is @StringRes Int) {
            p.setSecondaryText(secondaryTex)
        }
        p.setPrimaryTextColourFromRes(R.color.promptText)
        p.setSecondaryTextColourFromRes(R.color.promptText)
        p.setBackgroundColourFromRes(R.color.promptBackGround)

        if (icon != null) {
            if (icon is Drawable) {
                p.setIconDrawable(icon)
            } else if (icon is Int) {
                p.setIcon(icon)
            }
        }
        if (iconDrawableColourFilterRes != null) {
            p.setIconDrawableColourFilterFromRes(iconDrawableColourFilterRes)
        }
        if (hidePromptListener != null) {
            p.setOnHidePromptListener(hidePromptListener)
        }

        return p
    }
}