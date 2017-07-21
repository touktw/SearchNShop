package co.esclub.searchnshop

import android.app.Application
import co.esclub.searchnshop.model.db.SearchItemRealmManager
import co.esclub.searchnshop.model.firebase.FirebaseService
import co.esclub.searchnshop.util.UUIDFactory
import io.realm.Realm

/**
 * Created by tae.kim on 09/06/2017.
 */

class NApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        UUIDFactory.init(applicationContext)
        Realm.init(this)
    }

    override fun onTerminate() {
        super.onTerminate()
        SearchItemRealmManager.close()
    }

}
