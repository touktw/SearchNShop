package co.esclub.searchnshop.util

import android.content.Context
import android.view.View
import co.esclub.searchnshop.util.AdManager.AdProvider.AD_MOB
import co.esclub.searchnshop.util.AdManager.AdProvider.FACEBOOK
import com.google.android.gms.ads.AdRequest


/**
 * Created by tae.kim on 19/06/2017.
 */

class AdManager(val type: AdProvider, val context: Context) {
    private val FACEBOOK_KEY = "1350063585070996_1350068838403804"
    private val AD_MOB_KEY = "ca-app-pub-3759218081309192/1224243464"

    enum class AdProvider {
        AD_MOB,
        FACEBOOK
    }

    var adView: View? = null

    init {
        if (adView == null) {
            if (type == AD_MOB) {
                adView = com.google.android.gms.ads.AdView(context)
                (adView as com.google.android.gms.ads.AdView).adSize = com.google.android.gms.ads.AdSize.SMART_BANNER
                (adView as com.google.android.gms.ads.AdView).adUnitId = AD_MOB_KEY
            } else if (type == FACEBOOK) {
//                adView = com.facebook.ads.AdView(context, FACEBOOK_KEY,
//                        com.facebook.ads.AdSize.BANNER_HEIGHT_50)
            }
        }
    }

    fun load() {
        if (adView is com.google.android.gms.ads.AdView) {
            (adView as com.google.android.gms.ads.AdView).loadAd(AdRequest.Builder().build())
        }
// else if (adView is com.facebook.ads.AdView) {
//            (adView as com.facebook.ads.AdView).loadAd()
//        }
    }

    fun destroy() {
        if (adView is com.google.android.gms.ads.AdView) {
            (adView as com.google.android.gms.ads.AdView).destroy()
        }
//        else if (adView is com.facebook.ads.AdView) {
//            (adView as com.facebook.ads.AdView).destroy()
//        }
    }
}
