package shipshapedevices.shipshape_v0;

import java.util.ArrayList;
import java.util.Date;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Created by Luke on 4/17/2017.
 */

public class Parcel extends RealmObject {

    @PrimaryKey
    @Required
    private String parcelID;
    private String firebaseID;
    private String shipperID, receiverID;
    private String shipDate, recieveDate;
    private RealmList<ImpactEvent> impactEvents;
    private RealmList<Data> tempLog;
    private RealmList<Data> humidLog;

    // Need an empty constructor for realm
    public Parcel() {
    }

    // General package constructor
    public Parcel(String temp_id) {
        parcelID = temp_id; // TODO: 5/16/2017 get global unique ID for firebase
        impactEvents = new RealmList<>();
        tempLog = new RealmList<>();
        humidLog = new RealmList<>();
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

    public String getRecieveDate() {
        return recieveDate;
    }
    public void setRecieveDate(String recieveDate) {
        this.recieveDate = recieveDate;
    }

    public RealmList<ImpactEvent> getImpactEvents() {
        return impactEvents;
    }

    public void setImpactEvents(RealmList<ImpactEvent> impactEvents) {
        this.impactEvents = impactEvents;
    }

    public RealmList<Data> getTempLog() {
        return tempLog;
    }

    public void setTempLog(RealmList<Data> tempLog) {
        this.tempLog = tempLog;
    }

    public RealmList<Data> getHumidLog() {
        return humidLog;
    }

    public void setHumidLog(RealmList<Data> humidLog) {
        this.humidLog = humidLog;
    }

    // Data log modification functions
    public int getNumImpacts(){
        return impactEvents.size();
    }
    public void writeImpactEvent(RealmList<Data> log){
        ImpactEvent ie = new ImpactEvent(log); // TODO: 5/16/2017 change constructor to add impact time, as well
        impactEvents.add(ie);
    }
    public RealmList<Data> readImpactEvent(int eventIndex){
        return impactEvents.get(eventIndex).accelLog; // TODO: 5/16/2017 query based on time instead of position
    }

    public void writeTempLog(RealmList<Data> log){
        tempLog = log;
    }
    public RealmList<Data> readTempLog(){
        return tempLog;
    }

    public void writeHumidLog(RealmList<Data> log){
        humidLog = log;
    }
    public RealmList<Data> readHumidLog(){
        return humidLog;
    }
}

