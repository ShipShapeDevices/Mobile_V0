package shipshapedevices.shipshape_v0;

/**
 * Created by Luke on 5/17/2017.
 */

import java.util.ArrayList;

import io.realm.RealmList;

/**
 * Created by Luke on 4/17/2017.
 */

public class TempParcel {

    private String parcelID;
    private String firebaseID;
    private String shipperID, receiverID;
    private String shipDate, receiveDate;
    private ArrayList<TempImpactEvent> impactEvents = new ArrayList<>();
    private ArrayList<Data> tempLog = new ArrayList<>();
    private ArrayList<Data> humidLog = new ArrayList<>();

    public TempParcel(){};

    // Conversion to Parcel function
    public Parcel convertForRealm(){
        Parcel p = new Parcel(parcelID);
        p.setFirebaseID(firebaseID);
        p.setShipperID(shipperID);
        p.setReceiverID(receiverID);
        p.setShipDate(shipDate);
        p.setReceiveDate(receiveDate);
        //impact event logs
        if(!impactEvents.isEmpty()) {
            for (int i = 0; i < impactEvents.size(); i++) {
                TempImpactEvent ie = impactEvents.get(i);
                RealmList<Data> rl = new RealmList<>();
                rl.addAll(ie.getAccelLog());
                ImpactEvent event = new ImpactEvent(ie.getImpactTime(), rl);
                p.getImpactEvents().add(event);
            }
        }
        //other data logs
        if(!tempLog.isEmpty()){ p.getTempLog().addAll(tempLog); }
        if(!humidLog.isEmpty()){ p.getHumidLog().addAll(humidLog); }
        return p;
    }

    // Getters & setters for private data:
    public String getParcelID() {
        return parcelID;
    }
    public void setParcelID(String parcelID) {
        this.parcelID = parcelID;
    }

    public String getFirebaseID() {
        return firebaseID;
    }
    public void setFirebaseID(String firebaseID) {
        this.firebaseID = firebaseID;
    }

    public String getShipperID() {
        return shipperID;
    }
    public void setShipperID(String shipperID) {
        this.shipperID = shipperID;
    }

    public String getReceiverID() {
        return receiverID;
    }
    public void setReceiverID(String receiverID) {
        this.receiverID = receiverID;
    }

    public String getShipDate() {
        return shipDate;
    }
    public void setShipDate(String shipDate) {
        this.shipDate = shipDate;
    }

    public String getReceiveDate() {
        return receiveDate;
    }
    public void setReceiveDate(String receiveDate) {
        this.receiveDate = receiveDate;
    }

    public ArrayList<TempImpactEvent> getImpactEvents() {
        return impactEvents;
    }
    public void setImpactEvents(ArrayList<TempImpactEvent> impactEvents) {
        this.impactEvents = impactEvents;
    }

    public ArrayList<Data> getTempLog() {
        return tempLog;
    }
    public void setTempLog(ArrayList<Data> tempLog) {
        this.tempLog = tempLog;
    }

    public ArrayList<Data> getHumidLog() {
        return humidLog;
    }
    public void setHumidLog(ArrayList<Data> humidLog) {
        this.humidLog = humidLog;
    }
}


