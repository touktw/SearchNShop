package co.esclub.searchnshop

import android.app.Application
import co.esclub.searchnshop.model.db.SearchItemRealmManager
import io.realm.Realm

/**
 * Created by tae.kim on 09/06/2017.
 */

class NApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Realm.init(this)
    }

    override fun onTerminate() {
        super.onTerminate()
        SearchItemRealmManager.close()
    }

}
