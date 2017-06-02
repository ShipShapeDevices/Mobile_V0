package shipshapedevices.shipshape_v0;

/**
 * Created by Luke on 5/16/2017.
 */

import android.app.Application;

import com.google.firebase.database.DatabaseReference;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class ShipShapeApplication extends Application {

    private Realm realm;

    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize Realm db within the application
        Realm.init(this);
        // Configure the Realm db
        RealmConfiguration realmConfig = new RealmConfiguration.Builder()
                .name(Realm.DEFAULT_REALM_NAME)
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(realmConfig);
        // Killing all realm data up-front for testing // TODO: 5/17/2017 remove this when needed
        //realm = Realm.getDefaultInstance();
        //realm.beginTransaction();
        //realm.deleteAll();
        //realm.commitTransaction();

    }
}

