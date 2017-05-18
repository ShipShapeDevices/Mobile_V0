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

    // Data log modification functions
    public void writeImpactLog(){
        // TODO: 5/16/2017
    }
    public float readImpactLog(){
        // TODO: 5/16/2017
        return 0;
    }

    public void writeTempLog(){
        // TODO: 5/16/2017
    }
    public float readTempLog(){
        // TODO: 5/16/2017
        return 0;
    }

    public void writeHumidLog(){
        // TODO: 5/16/2017
    }
    public float readHumidLog(){
        // TODO: 5/16/2017
        return 0;
    }
}
