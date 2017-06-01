package shipshapedevices.shipshape_v0;

import android.app.Dialog;
import android.content.Context;
import android.nfc.Tag;
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

import android.content.Intent;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.util.List;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.widget.EditText;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import shipshapedevices.shipshape_v0.barcode.BarcodeCaptureActivity;


public class AddPckgActivity extends AppCompatActivity {

    private static final String LOG_TAG = AddPckgActivity.class.getSimpleName();
    private static final int BARCODE_READER_REQUEST_CODE = 1;

    String delimiter = ", ";
    String[] QRArray = new String[3];
    String[] DataString = new String[4];
    //String networkSSID = "ShipShapeWIFI";
    String currentSSID;
    WifiManager wifiManager;
    AsyncConnection myAsync;
    ConnectionHandler myConnectionHandler;
    BroadcastReceiver broadcastReceiver;
    IntentFilter intentFilter;
    String LOG = "MainActivity";
    boolean firstConnect = true;
    boolean firstReConnect = true;
    boolean connectionReady = false;
    boolean gettingPackage = false;
    boolean parseData = false;
    boolean loggingData = false;
    //String packageID = "testPackage";
    //String URL = "192.168.4.1";

    String networkSSID, URL, packageID;



    private static final String TAG = "AddPckgActivity";
    private Realm realm;
    private String userName;
    private FirebaseDatabase fireDB;
    private DatabaseReference parcelRef;
    private DatabaseReference userRef;
    private String updateID;

    // Dummy variables for testing // TODO: 5/17/2017 delete after testing
    private RealmList<Data> impactOne;
    private RealmList<Data> impactTwo;
    private RealmList<Data> tempLog;
    private RealmList<Data> humidLog;

    // Items that will be received in the notification upon shipment
//    private String receiverID;
//    private String shipperID;
//    private String shipDate;
//    private String parcelID;

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

        //set up wifi connection
        initWifi();
        registerReceiver(broadcastReceiver, intentFilter);




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

        if (loggingData){
            startStopLogging();
            loggingData = false;
            Log.d(LOG, "Started Logging ");

        }



    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"destroying activity");
        //close realm instance
        realm.close();
        unregisterReceiver(broadcastReceiver);

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
        String receiverID = p.getReceiverID();
        //get a key from Firebase for the new parcel
        String newKey = parcelRef.push().getKey();
        //updated package id to send to device
        packageID = newKey;
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
        userRef.child(userName).child(newKey).setValue(new ParcelReference(newKey,"Shipper","Shipped")).addOnCompleteListener(
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
        userRef.child(receiverID).child(newKey).setValue(new ParcelReference(newKey,"Receiver","Shipped")).addOnCompleteListener(
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
        Log.d(TAG,"looking locally for parcel Id: " + p.getParcelID());
        Parcel existingP = realm.where(Parcel.class).equalTo("parcelID",p.getParcelID()).findFirst();
        if(existingP != null) {
            Log.d(TAG,"linked package found");
            p.setShipDate(existingP.getShipDate());
            p.setShipperID(existingP.getShipperID());
            p.setFirebaseID(existingP.getFirebaseID());
            realm.copyToRealmOrUpdate(p);
        }
        else{
            Log.d(TAG,"no linked package found");
            Toast toast = Toast.makeText(getApplicationContext(),"Package Does Not Exist", Toast.LENGTH_SHORT);
            toast.show();
        }
//        RealmResults<Parcel> copies = realm.where(Parcel.class).equalTo("parcelID",p.getParcelID()).findAll();
//        if(copies.isEmpty()){
//            //send warning that package doesn't exist
//            Log.d(TAG,"no linked package found");
//            Toast toast = Toast.makeText(getApplicationContext(),"Package Does Not Exist", Toast.LENGTH_SHORT);
//            toast.show();
//        }
//        else{
//            //copy existing data from existing parcel
//            Log.d(TAG,"linked package found");
//            Parcel existingP = copies.get(0);
//            p.setShipDate(existingP.getShipDate());
//            p.setShipperID(existingP.getShipperID());
//            p.setFirebaseID(existingP.getFirebaseID());
//            realm.copyToRealmOrUpdate(p);
//        }
        //close the transaction with the realm db
        realm.commitTransaction();

        return p;
    }

    private void updateParcelsInFirebase(Parcel p){
        //update the parcel in firebase using its key
        String updateID = p.getFirebaseID();
        String shipperID = p.getShipperID();
        Log.d(TAG,"writing parcel to Firebase with fb Id: " + updateID);
        parcelRef.child(updateID).setValue(p).addOnCompleteListener(
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
        userRef.child(userName).child(updateID).setValue(new ParcelReference(p.getFirebaseID(),"Receiver","Received")).addOnCompleteListener(
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG,"Added to receiver");
                        }
                        else{
                            //display error toast
                            Toast toast = Toast.makeText(getApplicationContext(),"Firebase parcel ref write error", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    }
                }
        );
        userRef.child(shipperID).child(updateID).setValue(new ParcelReference(p.getFirebaseID(),"Shipper","Received")).addOnCompleteListener(
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG,"Added to shipper");
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
                    String receiverID = createRecIDEntry.getText().toString();
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
                        //TODO test for scanning
                        Intent intent = new Intent(getApplicationContext(), BarcodeCaptureActivity.class);
                        startActivityForResult(intent, BARCODE_READER_REQUEST_CODE);
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
    } // end create dialog




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
                        p.setReceiveDate(currentTime);
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
     /* ***************************************************************************************
    !!!!!!!!!!!!!!!!!!!!!!!!!! WIFI and Scanning functions,!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    ***************************************************************************************** */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BARCODE_READER_REQUEST_CODE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    try {
                        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                        r.play();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Point[] p = barcode.cornerPoints;
                    QRArray = barcode.displayValue.split(delimiter);
                    networkSSID = QRArray[0];
                    URL = QRArray[1];
                    //packageID = QRArray[2];
                    loggingData = true;
                    if(networkSSID.equals("ShipShapeWIFI")) {
                        Toast.makeText(this, networkSSID, Toast.LENGTH_SHORT).show();
                    }
                    if(URL.equals("192.168.4.1")) {
                        Toast.makeText(this, URL, Toast.LENGTH_SHORT).show();
                    }
                    Toast.makeText(this, packageID, Toast.LENGTH_SHORT).show();
                } else {
                    //do nothing
                }
            } else Log.e(LOG_TAG, String.format(getString(R.string.barcode_error_format),
                    CommonStatusCodes.getStatusCodeString(resultCode)));
        } else super.onActivityResult(requestCode, resultCode, data);
    }


    public void startStopLogging(){


        Log.d(LOG, "Clicked" );
        //set package id and url before connecting
        //packageID= "my ship package";
        //URL = "192.168.4.1";
        connectShipShape( );

    }

    public  void initWifi(){
        Log.d(LOG, "Wifi is Initialized " );

        //set connection filters
        intentFilter =new

                IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        //create broadcast receiver
        broadcastReceiver =new

                BroadcastReceiver() {
                    @Override
                    public void onReceive (Context context, Intent intent){
                        ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                        NetworkInfo networkInfo = conMan.getActiveNetworkInfo();
                        if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                            if (networkInfo != null) {
                                // Wifi is connected
                                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                                //get ssid
                                Log.d(LOG, "Wifi is connected: " + String.valueOf(networkInfo));
                                boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
                                String ssid = wifiInfo.getSSID();
                                if (ssid.equals("\"" + networkSSID + "\"") && firstConnect && connectionReady) {
                                    Log.e(LOG, " --SHip SHape --- " + " SSID " + ssid);
                                    myAsync = new AsyncConnection(URL, 80,packageID, 100000, myConnectionHandler);
                                    myAsync.execute();
                                    // do subroutines here
                                    firstConnect = false;
                                    firstReConnect = true;


                                } else if (!connectionReady) {
                                    //do nothing
                                } else {
                                    Log.e(LOG, " -- Wifi connected now --- ");
                                    firstConnect = true;
                                }

                                if ((ssid.equals(currentSSID) ||  ssid.equals("eduroam"))&& firstReConnect ) {
                                    Log.e(LOG, " --REConnected Update Firebase --- " + " SSID " + ssid);
                                    //TODO update firebase call
                                    // do subroutines here
                                    firstReConnect = false;


                                }else{
                                    firstReConnect = true;

                                }


                                Log.e(LOG, " -- Wifi connected now --- " + " SSID " + ssid);
                            }
                        }


                    }
                };
// TODO Auto-generated method stub
        myConnectionHandler = new ConnectionHandler() {
            @Override
            public void didReceiveData(String data) {
                if (data != null) {
                    Log.d(LOG, "We CONNECTED and got: " + data);

                    if (data.toString().contains("end")) {
                        Log.d(LOG, "End Connection " + data);
                        parseData = false;
                        gettingPackage = false;
                        myAsync.disconnect();
                        disconnectShipShape();
                        firstConnect = true;
                    }
                    if (parseData){
                        Log.d(LOG, "We CONNECTED and got: " + data);
                        DataString = data.split(delimiter);
                        float curr = Float.parseFloat(DataString[0]);
                        //TODO save to realm
                    }

                    if (gettingPackage){
                        //TODO  package is data value
                        //packageID = data;
                        Log.d(LOG, "package ID: " + data);

                        gettingPackage = false;
                    }
                    if (data.toString().contains("packageID")) {
                        gettingPackage = true;
                        parseData = false;
                    }
                    if (data.toString().contains("start")) {
                        parseData = true;
                        gettingPackage = false;
                        Log.d(LOG, "getting package: " + data);

                    }


                }

            }

            @Override
            public void didDisconnect(Exception error) {
                Log.d(LOG, "DisCONNECTED");
            }

            @Override
            public void didConnect() {
                Log.d(LOG, "We CONNECTED");
            }
        };


    }
    public void disconnectShipShape() {
        wifiManager = (WifiManager) AddPckgActivity.this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
        //Log.i("list", "connected" + list);
        // reconnect to old network
        for (WifiConfiguration i : list) {
            if (i.SSID != null && i.SSID.equals(currentSSID)) {
                wifiManager.disconnect();
                wifiManager.enableNetwork(i.networkId, true);
                wifiManager.reconnect();
                Log.i(LOG, "Stanford connected");
                firstReConnect = true;
                break;
            }else
            {
                wifiManager.disconnect();

            }
        }
        connectionReady = false;
    }

    public void connectShipShape() {
        wifiManager = (WifiManager) AddPckgActivity.this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiConfiguration conf = new WifiConfiguration();
        //get info for current wifi
        WifiInfo wifiInfo;
        wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo.getSupplicantState() == SupplicantState.COMPLETED) {

            currentSSID = wifiInfo.getSSID();
            Log.i(LOG, "got current ssid:" + currentSSID);

        }


        conf.SSID = "\"" + networkSSID + "\"";
        conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);


        wifiManager.addNetwork(conf);

        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
        //Log.i("list", "connected" + list);

        for (WifiConfiguration i : list) {
            if (i.SSID != null && i.SSID.equals("\"" + networkSSID + "\"")) {
                wifiManager.disconnect();
                wifiManager.enableNetwork(i.networkId, true);
                wifiManager.reconnect();
                Log.i(LOG, "connected");
                connectionReady = true;
                break;

            }
        }
    }

}
