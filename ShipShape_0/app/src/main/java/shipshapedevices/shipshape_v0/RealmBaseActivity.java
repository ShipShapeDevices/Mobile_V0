package shipshapedevices.shipshape_v0;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public abstract class RealmBaseActivity extends AppCompatActivity {

    private RealmConfiguration realmConfiguration;

    protected RealmConfiguration getRealmConfig() {
        if (realmConfiguration == null) {
            realmConfiguration = new RealmConfiguration
                    .Builder()
                    .deleteRealmIfMigrationNeeded()
                    .build();
        }
        return realmConfiguration;
    }

    protected void resetRealm(){
        Realm.deleteRealm(getRealmConfig());
    }
}

