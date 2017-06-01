package shipshapedevices.shipshape_v0;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by Luke on 5/17/2017.
 */

public class User extends RealmObject {
    private String userID;
    private String UserName;
    private RealmList<ParcelReference> packages;

    public User(){}

    public User(String userID){
        this.userID = userID;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String UserName) {
        this.UserName = UserName;
    }

    public RealmList<ParcelReference> getPackages() {
        return packages;
    }

    public void setPackages(RealmList<ParcelReference> packages) {
        this.packages = packages;
    }
}
