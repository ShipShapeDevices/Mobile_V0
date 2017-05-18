package shipshapedevices.shipshape_v0;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

public class AddPckgActivity extends AppCompatActivity {

    private static final String TAG = "AddPckgActivity";
    private Realm realm;
    private String userName;
    private FirebaseDatabase fireDB;
    private DatabaseReference parcelRef;
    private DatabaseReference userRef;

    // Dummy variables for testing // TODO: 5/17/2017 delete after testing
    private RealmList<Data> impactOne;
    private RealmList<Data> impactTwo;
    private RealmList<Data> tempLog;
    private RealmList<Data> humidLog;

    // Items that will be received in the notification upon shipment
    private String receiverID;
    private String shipperID;
    private String shipDate;
    private String parcelID;

    // Linking views
    @BindView(R.id.createBtn) Button createBtn;
    @BindView(R.id.scanBtn) Button scanBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Link layout
        setContentView(R.layout.activity_addpckg);
        // Bind views
        ButterKnife.bind(this);

        //get the current username
        userName = FirebaseAuth.getInstance().getCurrentUser().getUid();
        //get reference to Firebase items
        fireDB = FirebaseDatabase.getInstance();
        parcelRef = fireDB.getReference().child("parcels");
        userRef = fireDB.getReference().child("users");
    }

    @Override
    protected void onResume() {
        super.onResume();

        //initialize realm for the activity
        realm = Realm.getDefaultInstance();

        // Add button listeners
        // Create Package Button:
        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Open a dialog for data upload
                CreateDialog dialog = new CreateDialog(AddPckgActivity.this);
                dialog.show();
                // Start the NFC write process
                // TODO: 5/17/2017
            }
        });

        // Scan Package Button:
        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start the NFC read process
                // TODO: 5/16/2017
                // Open a dialog for data upload
                ScanDialog dialog = new ScanDialog(AddPckgActivity.this);
                dialog.show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"destroying activity");
        //close realm instance
        realm.close();
    }

    private void addToParcels(Parcel p){
        //add new item to Firebase
        if (fireDB != null) {
            writeNewParcelToFirebase(p); //also sets the FbKey for parcel
            Log.d(TAG,"parcel written to firebase with key: "+p.getFirebaseID());
        }
        //add new item to Realm
        writeNewParcelToRealm(p);
    }

    private void writeNewParcelToRealm(Parcel p){
        //open a new transaction with the realm db
        realm.beginTransaction();
        //check to confirm it doesn't already exist
        RealmResults<Parcel> copies = realm.where(Parcel.class).equalTo("parcelID",p.getParcelID()).findAll();
        if(copies.isEmpty()){
            //add it to realm
            realm.copyToRealm(p);
        }
        else{
            Toast toast = Toast.makeText(getApplicationContext(),"Package Already Exists", Toast.LENGTH_SHORT);
            toast.show();
        }
        //close the transaction with the realm db
        realm.commitTransaction();
    }

    private void writeNewParcelToFirebase(Parcel p){
        //get a key from Firebase for the new parcel
        String newKey = parcelRef.push().getKey();
        //store the key locally
        p.setFirebaseID(newKey);
        //add the new parcel to firebase using the key
        parcelRef.child(newKey).setValue(p).addOnCompleteListener(
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            //do nothing
                        }
                        else{
                            //display error toast
                            Toast toast = Toast.makeText(getApplicationContext(),"Firebase parcel object write error", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    }
                }
        );
        //add parcel ID to users
        userRef.child(userName).push().setValue(new ParcelReference(newKey,"Shipper","Shipped")).addOnCompleteListener(
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            //do nothing
                        }
                        else{
                            //display error toast
                            Toast toast = Toast.makeText(getApplicationContext(),"Firebase parcel ref write error", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    }
                }
        );
        userRef.child(receiverID).push().setValue(new ParcelReference(newKey,"Receiver","Shipped")).addOnCompleteListener(
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            //do nothing
                        }
                        else{
                            //display error toast
                            Toast toast = Toast.makeText(getApplicationContext(),"Firebase parcel ref write error", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    }
                }
        );
    }

    private void updateParcels(Parcel p){
        //update item in Realm
        Parcel updatedP = updateParcelsInRealm(p);
        //update item in Firebase
        if (fireDB != null) {
            updateParcelsInFirebase(updatedP); //also sets the FbKey for parcel
            Log.d(TAG,"parcel updated in firebase with key: "+p.getFirebaseID());
        }
    }

    private Parcel updateParcelsInRealm(Parcel p){
        //open a new transaction with the realm db
        realm.beginTransaction();
        //check to confirm it doesn't already exist
        RealmResults<Parcel> copies = realm.where(Parcel.class).equalTo("parcelID",p.getParcelID()).findAll();
        if(copies.isEmpty()){
            //send warning that package doesn't exist
            Log.d(TAG,"no linked package found");
            Toast toast = Toast.makeText(getApplicationContext(),"Package Does Not Exist", Toast.LENGTH_SHORT);
            toast.show();
        }
        else{
            //copy existing data from existing parcel
            Log.d(TAG,"linked package found");
            Parcel existingP = copies.get(0);
            p.setShipDate(existingP.getShipDate());
            p.setShipperID(existingP.getShipperID());
            p.setFirebaseID(existingP.getFirebaseID());
            realm.copyToRealmOrUpdate(p);
        }
        //close the transaction with the realm db
        realm.commitTransaction();

        return p;
    }

    private void updateParcelsInFirebase(Parcel p){
        //update the parcel in firebase using its key
        parcelRef.child(p.getFirebaseID()).setValue(p).addOnCompleteListener(
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            //display error toast
                            Toast toast = Toast.makeText(getApplicationContext(),"Parcel updated", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                        else{
                            //display error toast
                            Toast toast = Toast.makeText(getApplicationContext(),"Firebase parcel object write error", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    }
                }
        );
        //update parcel status to users // TODO: 5/17/2017 update instead of overwriting parcel references
        userRef.child(userName).setValue(new ParcelReference(p.getFirebaseID(),"Receiver","Received")).addOnCompleteListener(
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            //do nothing
                        }
                        else{
                            //display error toast
                            Toast toast = Toast.makeText(getApplicationContext(),"Firebase parcel ref write error", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    }
                }
        );
        userRef.child(shipperID).setValue(new ParcelReference(p.getFirebaseID(),"Shipper","Received")).addOnCompleteListener(
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            //do nothing
                        }
                        else{
                            //display error toast
                            Toast toast = Toast.makeText(getApplicationContext(),"Firebase parcel ref write error", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    }
                }
        );
    }

    // TODO: 5/17/2017 delete this after testing
    private void createDummyLogs(){
        impactOne = new RealmList<>();
        impactOne.add(new Data(1.5,12));
        impactOne.add(new Data(2.5,13));
        impactOne.add(new Data(5,14));
        impactOne.add(new Data(3,15));
        impactOne.add(new Data(1.7,16));

        impactTwo = new RealmList<>();
        impactTwo.add(new Data(1.5,12));
        impactTwo.add(new Data(2.5,13));
        impactTwo.add(new Data(5,14));
        impactTwo.add(new Data(3,15));
        impactTwo.add(new Data(1.7,16));

        tempLog = new RealmList<>();
        tempLog.add(new Data(0.71,1));
        tempLog.add(new Data(0.73,2));
        tempLog.add(new Data(0.72,3));
        tempLog.add(new Data(0.72,4));
        tempLog.add(new Data(0.72,5));
        tempLog.add(new Data(0.71,6));
        tempLog.add(new Data(0.73,7));
        tempLog.add(new Data(0.72,8));
        tempLog.add(new Data(0.72,9));
        tempLog.add(new Data(0.72,10));
        tempLog.add(new Data(0.71,11));
        tempLog.add(new Data(0.73,12));
        tempLog.add(new Data(0.72,13));
        tempLog.add(new Data(0.72,14));
        tempLog.add(new Data(0.72,15));
        tempLog.add(new Data(0.71,16));
        tempLog.add(new Data(0.73,17));
        tempLog.add(new Data(0.72,18));
        tempLog.add(new Data(0.72,19));
        tempLog.add(new Data(0.72,20));
        tempLog.add(new Data(0.71,21));
        tempLog.add(new Data(0.73,22));
        tempLog.add(new Data(0.72,23));
        tempLog.add(new Data(0.72,24));
        tempLog.add(new Data(0.72,25));

        humidLog = new RealmList<>();
        humidLog.add(new Data(0.71,1));
        humidLog.add(new Data(0.73,2));
        humidLog.add(new Data(0.72,3));
        humidLog.add(new Data(0.72,4));
        humidLog.add(new Data(0.72,5));
        humidLog.add(new Data(0.71,6));
        humidLog.add(new Data(0.73,7));
        humidLog.add(new Data(0.72,8));
        humidLog.add(new Data(0.72,9));
        humidLog.add(new Data(0.72,10));
        humidLog.add(new Data(0.71,11));
        humidLog.add(new Data(0.73,12));
        humidLog.add(new Data(0.72,13));
        humidLog.add(new Data(0.72,14));
        humidLog.add(new Data(0.72,15));
        humidLog.add(new Data(0.71,16));
        humidLog.add(new Data(0.73,17));
        humidLog.add(new Data(0.72,18));
        humidLog.add(new Data(0.72,19));
        humidLog.add(new Data(0.72,20));
        humidLog.add(new Data(0.71,21));
        humidLog.add(new Data(0.73,22));
        humidLog.add(new Data(0.72,23));
        humidLog.add(new Data(0.72,24));
        humidLog.add(new Data(0.72,25));
    }

    // Dialog for data writing/upload interaction
    public class CreateDialog extends Dialog {
        // Link views
        @BindView(R.id.createRecIDEntry) EditText createRecIDEntry;
        @BindView(R.id.createTagEntry) EditText createTagEntry;
        @BindView(R.id.createEnterButton) Button createEnterButton;
        @BindView(R.id.createCancelButton) Button createCancelButton;

        public CreateDialog(@NonNull Context context) {
            super(context);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setTitle("Create new package");
            setCancelable(true);
            // Link the dialog layout
            setContentView(R.layout.dialog_create);
            // Bind views
            ButterKnife.bind(this);

            // If user clicks "Enter" button in dialog =>
            createEnterButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    receiverID = createRecIDEntry.getText().toString();
                    String dataTag = createTagEntry.getText().toString();
                    // If a valid ID & label has been entered
                    if(!receiverID.equals("")){
                        // Create a new parcel (realm object)
                        Parcel p = new Parcel(dataTag);
                        p.setShipperID(userName);
                        p.setReceiverID(receiverID); // TODO: 5/17/2017 send notification to receiver
                        String currentTime = DateFormat.getDateTimeInstance().format(new Date());
                        p.setShipDate(currentTime);
                        // Add the parcel to Realm & Firebase
                        addToParcels(p);
                        // Dismiss the dialog
                        dismiss();
                    }
                    //if an invalid ID
                    else{
                        // Populate toast with warning
                        String unlockToastText = "Enter a receiver for the package";
                        // Show toast
                        Toast toast = Toast.makeText(getApplicationContext(), unlockToastText, Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
            });

            //If user clicks the "Cancel" button in the dialog =>
            createCancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //dismiss the dialog
                    dismiss();
                }
            });
        }
    }
    
    // Dialog for data scanning/upload interaction
    public class ScanDialog extends Dialog {
        // Link views
        @BindView(R.id.scanTagEntry) EditText scanTagEntry;
        @BindView(R.id.scanEnterButton) Button scanEnterButton;
        @BindView(R.id.scanCancelButton) Button scanCancelButton;

        public ScanDialog(@NonNull Context context) {
            super(context);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setTitle("Scan received package");
            setCancelable(true);
            // Link the dialog layout
            setContentView(R.layout.dialog_scan);
            // Bind views
            ButterKnife.bind(this);

            // If user clicks "Enter" button in dialog =>
            scanEnterButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String dataTag = scanTagEntry.getText().toString();
                    // If a valid ID has been entered
                    if(!dataTag.equals("")){
                        // Begin scanning
                        // TODO: 5/16/2017
                        // Pull the data from the dummy files
                        createDummyLogs(); // TODO: 5/17/2017 delete after testing 
                        // Create a new parcel (realm object)
                        Parcel p = new Parcel(dataTag);
                        p.setReceiverID(userName); // TODO: 5/17/2017 confirm receiver ID on scan
                        String currentTime = DateFormat.getDateTimeInstance().format(new Date());
                        p.setRecieveDate(currentTime);
                        // Add the data logs to the parcel
                        p.writeImpactEvent(impactOne);
                        p.writeImpactEvent(impactTwo);
                        p.writeTempLog(tempLog);
                        p.writeHumidLog(humidLog);
                        // Update the parcel in Realm & Firebase
                        updateParcels(p);
                        // Dismiss the dialog
                        dismiss();
                    }
                    //if an invalid ID
                    else{
                        // Populate toast with warning
                        String unlockToastText = "Enter a label for the package.";
                        // Show toast
                        Toast toast = Toast.makeText(getApplicationContext(), unlockToastText, Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
            });

            //If user clicks the "Cancel" button in the dialog =>
            scanCancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //dismiss the dialog
                    dismiss();
                }
            });
        }
    }
}
