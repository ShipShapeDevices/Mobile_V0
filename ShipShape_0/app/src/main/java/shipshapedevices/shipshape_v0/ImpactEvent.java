package shipshapedevices.shipshape_v0;

import java.util.Date;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by Luke on 5/16/2017.
 */


// An impact event
public class ImpactEvent extends RealmObject {
    private RealmList<Data> accelLog;
    private String impactTime; // TODO: 5/16/2017 make use of impact time as primary key

    // Empty constructor for Realm
    public ImpactEvent(){}

    public ImpactEvent(RealmList<Data> accelLog) {
        this.accelLog = accelLog;
    }
    
    public ImpactEvent(String impactTime, RealmList<Data> accelLog) {
        this.impactTime = impactTime;
        this.accelLog = accelLog;
    }

    public RealmList<Data> getAccelLog() {
        return accelLog;
    }

    public void setAccelLog(RealmList<Data> accelLog) {
        this.accelLog = accelLog;
    }

    public String getImpactTime() {
        return impactTime;
    }

    public void setImpactTime(String impactTime) {
        this.impactTime = impactTime;
    }
}
