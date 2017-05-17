package shipshapedevices.shipshape_v0;

import io.realm.RealmObject;

/**
 * Created by Luke on 5/16/2017.
 */

// A logged data point
public class Data extends RealmObject {
    public double value;
    public long time;

    // Empty constructor for Realm
    public Data(){}

    public Data(double v,long t) {
        value = v;
        time = t;
    }
}
