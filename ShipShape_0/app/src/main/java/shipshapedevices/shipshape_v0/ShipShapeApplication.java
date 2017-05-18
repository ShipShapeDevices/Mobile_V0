package shipshapedevices.shipshape_v0;

import android.app.Application;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by Matthew on 5/17/2017.
 */

public class ShipShapeApplication extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        //Initialize Realm
        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder()
                .name(Realm.DEFAULT_REALM_NAME)
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(config);

    }

    @Override
    public void onTerminate(){
        super.onTerminate();
    }
}
