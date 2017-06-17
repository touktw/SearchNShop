package co.esclub.searchnshop;

import android.app.Application;

import io.realm.Realm;

/**
 * Created by tae.kim on 09/06/2017.
 */

public class NApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
    }
}
