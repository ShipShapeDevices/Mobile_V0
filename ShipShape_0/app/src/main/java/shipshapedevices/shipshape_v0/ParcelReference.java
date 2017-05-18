package shipshapedevices.shipshape_v0;

import io.realm.RealmObject;

/**
 * Created by Luke on 5/17/2017.
 */

public class ParcelReference extends RealmObject {

    private String parcelID;
    private String parcelType;
    private String status;

    public ParcelReference() {}

    public ParcelReference(String parcelID, String type, String status) {
        this.parcelID = parcelID;
        this.status = status;
    }

    public String getParcelID() {
        return parcelID;
    }

    public void setParcelID(String parcelID) {
        this.parcelID = parcelID;
    }

    public String getParcelType() {
        return parcelType;
    }

    public void setParcelType(String parcelType) {
        this.parcelType = parcelType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
