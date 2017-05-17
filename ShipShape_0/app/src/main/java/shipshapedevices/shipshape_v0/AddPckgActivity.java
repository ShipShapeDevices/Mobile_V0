package shipshapedevices.shipshape_v0;

import android.app.Dialog;
import android.content.Context;
import android.icu.util.Calendar;
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

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

public class AddPckgActivity extends AppCompatActivity {

    private static final String TAG = "AddPckgActivity";
    private Realm realm;
    private String userName = "test_user";
    private FirebaseDatabase fireDB;
    private DatabaseReference parcelRef;

    // Dummy variables for testing // TODO: 5/17/2017 delete after testing
    private RealmList<Data> impactOne;
    private RealmList<Data> impactTwo;
    private RealmList<Data> tempLog;
    private RealmList<Data> humidLog;

    // Linking views
    @BindView(R.id.scanBtn) Button scanBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Link layout
        setContentView(R.layout.activity_addpckg);
        // Bind views
        ButterKnife.bind(this);

        //get the current username
        // TODO: 5/17/2017  userName = FirebaseAuth.getInstance().getCurrentUser().getUid();
        //get reference to Firebase database;
        fireDB = FirebaseDatabase.getInstance();
        parcelRef = fireDB.getReference().child("parcels");
    }

    @Override
    protected void onResume() {
        super.onResume();

        //initialize realm for the activity
        realm = Realm.getDefaultInstance();

        // Add button listeners
        // Scan Package Button:
        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start the scanning process
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
            writeToFirebase(p); //also sets the FbKey for ride
            Log.d(TAG,"ride written to firebase with key: "+p.getFirebaseID());
        }
        //add new item to Realm
        writeToRealm(p);
    }

    private void writeToRealm(Parcel p){
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

    private void writeToFirebase(Parcel p){
        //get a key from Firebase for the new ride
        String newKey = parcelRef.push().getKey();
        //store the key locally
        p.setFirebaseID(newKey);
        //add the new ride to firebase using the key
        parcelRef.child(newKey).setValue(p).addOnCompleteListener(
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            //do nothing
                        }
                        else{
                            //display error toast
                            Toast toast = Toast.makeText(getApplicationContext(),"Firebase write error", Toast.LENGTH_SHORT);
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
            setTitle("Scan new package");
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
                        // TODO: 5/16/2017
                        // Pull the data from the dummy files
                        createDummyLogs();
                        // Create a new parcel (realm object)
                        Parcel p = new Parcel(dataTag);
                        p.setShipperID("test_shipper");
                        p.setReceiverID(userName);
                        p.setRecieveDate(Calendar.getInstance().getTime());
                        // Add the data logs to the parcel
                        p.writeImpactEvent(impactOne);
                        p.writeImpactEvent(impactTwo);
                        p.writeTempLog(tempLog);
                        p.writeHumidLog(humidLog);
                        // Add the parcel to Realm & Firebase
                        addToParcels(p);
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
