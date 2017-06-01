package shipshapedevices.shipshape_v0;

import io.realm.RealmObject;

/**
 * Created by Luke on 5/16/2017.
 */

// A logged data point
public class Data extends RealmObject {
    private float value;
    private float time;

    // Empty constructor for Realm
    public Data(){}

    public Data(double v,float t) {
        value = (float) v;
        time =  t;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public float getTime() {
        return time;
    }

    public void setTime(float time) {
        this.time = time;
    }
}
