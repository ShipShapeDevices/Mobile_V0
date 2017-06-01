package shipshapedevices.shipshape_v0;

import java.util.ArrayList;
import java.util.Date;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by Luke on 5/16/2017.
 */

// An impact event
public class TempImpactEvent{
    private ArrayList<Data> accelLog;
    private String impactTime; // TODO: 5/16/2017 make use of impact time as primary key

    public TempImpactEvent(){}

    public TempImpactEvent(String impactTime, ArrayList<Data> accelLog) {
        this.impactTime = impactTime;
        this.accelLog = accelLog;
    }

    public ArrayList<Data> getAccelLog() {
        return accelLog;
    }

    public void setAccelLog(ArrayList<Data> accelLog) {
        this.accelLog = accelLog;
    }

    public String getImpactTime() {
        return impactTime;
    }

    public void setImpactTime(String impactTime) {
        this.impactTime = impactTime;
    }
}
