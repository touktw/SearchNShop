package co.esclub.searchnshop.model

import android.util.Log
import io.realm.*

/**
 * Created by tae.kim on 09/06/2017.
 */

object RealmManager {
    val SCHEME_VERSION = 2;
    val config = RealmConfiguration.Builder().schemaVersion(SCHEME_VERSION.toLong())
            .migration(Migration).deleteRealmIfMigrationNeeded().build()

    fun get(): Realm {
        return Realm.getInstance(config)
    }

    object Migration : RealmMigration {
        override fun migrate(realm: DynamicRealm?, oldVersion: Long, newVersion: Long) {
            Log.d("###", "migrate old[${oldVersion}] new[${newVersion}]")
            val schema = realm?.schema as RealmSchema
            val old = oldVersion
            if (old == 1L || old == 0L) {
                val objectSchema = schema.get("SearchItem")
                objectSchema.addField("lastSearchTime", Long::class.java)
            }
        }

    }
}

