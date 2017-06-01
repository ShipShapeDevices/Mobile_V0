package shipshapedevices.shipshape_v0;


import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.moonmonkeylabs.realmrecyclerview.RealmRecyclerView;
import io.realm.Realm;
import io.realm.RealmBasedRecyclerViewAdapter;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.RealmViewHolder;
import io.realm.Sort;
import shipshapedevices.shipshape_v0.barcode.BarcodeCaptureActivity;

public class MyPackagesActivity extends RealmBaseActivity
        implements NavigationView.OnNavigationItemSelectedListener  {

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
    boolean dataReady = false;
    //String packageID = "testPackage";
    //String URL = "192.168.4.1";

    String networkSSID, URL, packageID;
    boolean gettingPackageID = false;

    private RealmList<Data> impactOne;
    private RealmList<Data> tempLog;
    private RealmList<Data> humidLog;

    private String userName;
    private static final String TAG="MyPackagesActivity";
    private PackageRecyclerViewAdapter packageAdapter;
    private Realm realm;
    private DatabaseReference firebase;
    private DatabaseReference userRef;
    private DatabaseReference packagesRef;
    private String addPackageParcelID;
    @BindView(R.id.realm_recycler_view)
    RealmRecyclerView realmRecyclerView;
    @BindView(R.id.addPckgBtn)
    FloatingActionButton addPckgBtn;
    private boolean PackageExists;
    private static boolean PackageLinked=false;

    /****************************************************************************************
     !!!!!!!!!!!!!!!!!!!!!!!!!! ACTIVITY: ON CREATE,!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    ***************************************************************************************** */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav);
        ButterKnife.bind(this);

        // Navigation view things
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Get instance off of firebase database
        userRef= FirebaseDatabase.getInstance().getReference("users"); //
        packagesRef= FirebaseDatabase.getInstance().getReference("parcels");
        realm = Realm.getDefaultInstance();
        RealmResults<Parcel> realmResults = realm.where(Parcel.class).findAll();
        firebase = FirebaseDatabase.getInstance().getReference();
        userName = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace('.',',');

        //clear realm
        //        realm.beginTransaction();
        //        realmResults.deleteAllFromRealm();
        //        realm.commitTransaction();

        //  Add button listeners
        // Add Package Option:


        RealmResults<Parcel> parcels = realm.where(Parcel.class).findAllSorted("parcelID", Sort.ASCENDING);
        packageAdapter = new PackageRecyclerViewAdapter(getBaseContext(), parcels);
        realmRecyclerView.setAdapter(packageAdapter);

        userRef.child(userName).addChildEventListener(new ChildEventListener() {
            @Override

            // when a child is added notify receivers that the package has shipped
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "The added child was: "+dataSnapshot.getValue());
                dataSnapshot.getValue();

                //TODOCreateNotification("Create Notification Test String");
                //TODO startActivity(new Intent(getApplicationContext(), MyPackagesActivity.class));
            }

            // when a package has been changed notify the shippers it was reveived
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "The updated post title is: " + dataSnapshot.getValue());
                //TODO CreateNotification("Packaged Received");
            }

            // if a child is removed all we need to do is update UI
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }

        }); // End on Child Event listener


        // add package button queries to see if a package currently exists
        addPckgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //start the code scan
                startQRscan();
            } // end on add pckg button clicked
        }); // end on add pckg button click listener


        /*firebase.child("parcels").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                //copy data to buffer object (RealmList work-around)
                TempParcel buffer = dataSnapshot.getValue(TempParcel.class);
                Parcel data = buffer.convertForRealm();
                //save to realm
                Log.d("Here",data.getParcelID());
                realm.beginTransaction();
                realm.copyToRealmOrUpdate(data);
                realm.commitTransaction();

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        */


        //set up wifi connection
        initWifi();
        registerReceiver(broadcastReceiver, intentFilter);

    } // END ACITVITY ON CREATE



    /****************************************************************************************
    !!!!!!!!!!!!!!!!!!!!!!!!!! ACTIVITY: ON DESTROY,!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    ***************************************************************************************** */

    @Override
    protected void onDestroy(){
        super.onDestroy();
        realm.close();
        unregisterReceiver(broadcastReceiver);

    } // end activity on destroy

    /****************************************************************************************
    !!!!!!!!!!!!!!!!!!!!!!!!!! ACTIVITY: ON RESUME !!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    ***************************************************************************************** */
    @Override
    protected void onResume() {
        super.onResume();

        if (gettingPackageID){
            //clear flag
            gettingPackageID = false;
            //get from scan
            addPackageParcelID=packageID;//addPckgText.getText().toString();
            Log.d(TAG, "Parcel ID Searching for: " + addPackageParcelID);
        // check to see if package already exists. if it does than we are the receiver. if not than we are shipper
        packagesRef.orderByChild("parcelID").equalTo(addPackageParcelID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "Parcel Found: " + dataSnapshot.getValue());

                // if package doesnt exist create package
                if (dataSnapshot.getValue() == null) {
                    Log.d(TAG, "Package doesnt exist finished query. ");
                    // create package


                    CreateDialog dialog = new CreateDialog(MyPackagesActivity.this);
                    dialog.show();


                }

                // else package already exists
                else {
                    Log.d(TAG, "Package exists finished query. ");

                    //start wifi and send data to pull in data
                    if (loggingData) {
                        startStopLogging();
                        loggingData = false;
                        Log.d(LOG, "Started Logging ");

                    }

                }
            } // end on data changed


            @Override
            public void onCancelled(DatabaseError databaseError) {
            }

        }); // end value event listener


    }

    }

    /****************************************************************************************
     !!!!!!!!!!!!!!!!!!!!!!!!!! NAVIGATION DRAWER STUFF !!!!!!!!!!!!!!!!!!!!!!!!!!!!!
     ***************************************************************************************** */
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_scan) {
            // Handle the QR scan action
            startQRscan();
        } else if (id == R.id.nav_settings) {
            //dummy button for now

        } else if (id == R.id.nav_logout) {
            //sign out
            FirebaseAuth  mAuthUsers = FirebaseAuth.getInstance();
            mAuthUsers.signOut();
            // start Login activity
            Intent i = new Intent(MyPackagesActivity.this, LoginActivity.class);
            startActivity(i);
            // finish this activity
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /****************************************************************************************
    !!!!!!!!!!!!!!!!!!!!!!!!!! FUNCTIONS TO CREATE NEW PARCELS !!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    ***************************************************************************************** */
    // Dialog for data writing/upload interaction
    public class CreateDialog extends Dialog {
        // Link views
        @BindView(R.id.createRecIDEntry) EditText createRecIDEntry;
        @BindView(R.id.createTagEntry) TextView createTagEntry;
        @BindView(R.id.createEnterButton) Button createEnterButton;
        @BindView(R.id.createCancelButton) Button createCancelButton;

        public CreateDialog(@NonNull Context context) {
            super(context);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setTitle("Create new package");
            //createRecIDEntry.setText("hello"); TODO check the receiver to see if itsa  valid user
            setCancelable(true);
            // Link the dialog layout
            setContentView(R.layout.dialog_create);
            // Bind views
            ButterKnife.bind(this);

            // If user clicks "Enter" button in dialog =>
            createEnterButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final String receiverID = createRecIDEntry.getText().toString();
                    final String dataTag = createTagEntry.getText().toString();
                    // If a valid ID & label has been entered

                    // check to see if it is a valid user
                    userRef.orderByChild("userName").equalTo(receiverID).addListenerForSingleValueEvent(new ValueEventListener(){
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot){
                            Log.d(TAG, "User Found: " + dataSnapshot.getValue());
                            // if not a vaild user create a toast
                            if(dataSnapshot.getValue()==null){
                                Log.d(TAG, "Not a vaild User. ");
                                /// Populate toast with warning
                                String unlockToastText = "Please Enter a valid receiver for the package";
                                // Show toast
                                Toast toast = Toast.makeText(getApplicationContext(), unlockToastText, Toast.LENGTH_SHORT);
                                toast.show();
                            }

                            // else user is valid
                            else {

                                    Log.d(TAG, "Receiver Exists creating parcel. ");
                                    // Create a new parcel (realm object)
                                    Parcel p = new Parcel(dataTag);
                                    p.setShipperID(userName);
                                    p.setReceiverID(receiverID); // TODO: 5/17/2017 send notification to receiver
                                    String currentTime = DateFormat.getDateTimeInstance().format(new Date());
                                    p.setShipDate(currentTime);
                                    p.setParcelID(addPackageParcelID);
                                    // Add the parcel to Realm & Firebase

                                    addToParcels(p);

                                //start wifi and send data to start logging
                                if (loggingData){
                                    startStopLogging();
                                    loggingData = false;
                                    Log.d(LOG, "Started Logging ");

                                }

                                    //Dismiss the dialog
                                    dismiss();

                                }


                        } // end on data changed




                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }

                    }); // end value event listener

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



    private void addToParcels(Parcel p){
        //add new item to Firebase
        if (firebase != null) {
            writeNewParcelToFirebase(p); //also sets the FbKey for parcel
            Log.d(TAG,"parcel written to firebase with key: "+p.getFirebaseID());
        }
        //add new item to Realm
        writeNewParcelToRealm(p);
    }


    private void writeNewParcelToRealm(Parcel p){
        Log.d(TAG,"Write to Realm: ");

        //open a new transaction with the realm db
        realm.beginTransaction();
        //check to confirm it doesn't already exist

        //realm.copyToRealmOrUpdate(p);

        RealmResults<Parcel> copies = realm.where(Parcel.class).equalTo("parcelID",p.getParcelID()).findAll();
        if(copies.isEmpty()){
            //add it to realm
            realm.copyToRealmOrUpdate(p);
        }
        else{
            Toast toast = Toast.makeText(getApplicationContext(),"Package Already Exists in Realm", Toast.LENGTH_SHORT);
            toast.show();
        }
        //close the transaction with the realm db
        realm.commitTransaction();
    }

    private void writeNewParcelToFirebase(Parcel p){
        String receiverID = p.getReceiverID().replace('.',',');
        //get a key from Firebase for the new parcel
        String newKey = packagesRef.push().getKey();
        //store the key locally
        //updated package id to send to device


        p.setFirebaseID(newKey);
        //add the new parcel to firebase using the key
        packagesRef.child(newKey).setValue(p).addOnCompleteListener(
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




/****************************************************************************************
   !!!!!!!!!!!!!!!!!!!!!!!!!! RECYCLER VIEW STUFF !!!!!!!!!!!!!!!!!!!!!!!!!!!!!
  ***************************************************************************************** */

    public class PackageRecyclerViewAdapter extends RealmBasedRecyclerViewAdapter<Parcel, PackageRecyclerViewAdapter.ViewHolder> {

        public PackageRecyclerViewAdapter(
                Context context,
                RealmResults<Parcel> realmResults) {
            super(context, realmResults, true, true, false, "parcelID");
        }

        public class ViewHolder extends RealmViewHolder {
            public FrameLayout container;
            public TextView parcelTextView;
            public TextView partnerTextView;

            public ViewHolder(FrameLayout container) {
                super(container);
                // TODO: 6/1/2017 use Butterknife here instead
                this.container = container;
                this.partnerTextView = (TextView) container.findViewById(R.id.row_partner_id);
                this.parcelTextView = (TextView) container.findViewById(R.id.row_parcel_id);
            }
        }

        @Override
        public ViewHolder onCreateRealmViewHolder(ViewGroup viewGroup, int viewType) {
            View v = inflater.inflate(R.layout.package_row, viewGroup, false);
            return new ViewHolder((FrameLayout) v);
        }

        @Override
        public void onBindRealmViewHolder(ViewHolder viewHolder, int position){
            final Parcel parcel = realmResults.get(position);
            if(parcel.getShipperID().equals(userName)) { // check to see if you're shipper or receiver and add partner
                viewHolder.partnerTextView.setText(parcel.getReceiverID()); //TODO: 6/1/2017 check for consistency
            }
            else {
                viewHolder.partnerTextView.setText(parcel.getShipperID());
            }
            viewHolder.parcelTextView.setText(parcel.getParcelID());
            viewHolder.itemView.setOnClickListener(
                    new View.OnClickListener(){
                        @Override
                        public void onClick(View v){
                            // if data exists in parcel
                            if(!parcel.getTempLog().isEmpty()) {
                                Intent mypac2stat = new Intent(MyPackagesActivity.this, GraphingActivity.class);
                                mypac2stat.putExtra("CurrentParcelID", parcel.getParcelID());
                                startActivity(mypac2stat);
                                Toast.makeText(MyPackagesActivity.this, parcel.getParcelID(), Toast.LENGTH_SHORT).show();
                            }
                            // else if no data exists in parcel
                            else{
                                //display error toast
                                Toast toast = Toast.makeText(getApplicationContext(),"Parcel contains no data.", Toast.LENGTH_SHORT);
                                toast.show();
                            } // end else no data in parcetl

                        }
                    } // end on click listener
            ); // end set on click listener

        } // end on bind realm viewholder
    } // end  recycler view adapter

    /****************************************************************************************
    !!!!!!!!!!!!!!!!!!!!!!!!!! WIFI and Scanning functions,!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    ***************************************************************************************** */


    private void startQRscan(){
        PackageExists=false; //reset flag seeing if package exists
        //TODO test for scanning
        Intent intent = new Intent(getApplicationContext(), BarcodeCaptureActivity.class);
        startActivityForResult(intent, BARCODE_READER_REQUEST_CODE);

    }

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
                    packageID = QRArray[2];
                    loggingData = true;
                    gettingPackageID = true;
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

    public void initWifi(){
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

                                    if (dataReady) {
                                        //TODO update firebase call
                                        // do subroutines here
                                        String dataTag = packageID;
                                        // If a valid ID has been entered

                                        // Begin scanning
                                        // TODO: 5/16/2017

                                        // Create a new parcel (realm object)
                                        Parcel p = new Parcel(dataTag);
                                        p.setReceiverID(userName); // TODO: 5/17/2017 confirm receiver ID on scan
                                        String currentTime = DateFormat.getDateTimeInstance().format(new Date());
                                        p.setReceiveDate(currentTime);
                                        // Add the data logs to the parcel
                                        p.writeImpactEvent(impactOne);
                                        p.writeTempLog(tempLog);
                                        p.writeHumidLog(humidLog);
                                        // Update the parcel in Realm & Firebase
                                        updateParcels(p);


                                        dataReady = false;
                                    }
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

                    if (data.toString().contains("logging")) {
                        Log.d(LOG, "End Connection " + data);
                        parseData = false;
                        myAsync.disconnect();
                        disconnectShipShape();
                        firstConnect = true;
                    }
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
                        float time = Float.parseFloat(DataString[0]);
                        float humid = Float.parseFloat(DataString[1]);
                        float temp = Float.parseFloat(DataString[2]);
                        float accel = Float.parseFloat(DataString[3]);
                        //TODO save to realm

                        impactOne.add(new Data(accel,time));
                        tempLog.add(new Data(temp,time));
                        humidLog.add(new Data(humid,time));




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
                        dataReady = true;
                        gettingPackage = false;
                        //set buffers
                        impactOne = new RealmList<>();
                        tempLog = new RealmList<>();
                        humidLog = new RealmList<>();

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
        wifiManager = (WifiManager) MyPackagesActivity.this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

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
        wifiManager = (WifiManager) MyPackagesActivity.this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
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

    /* ***************************************************************************************
    !!!!!!!!!!!!!!!!!!!!!!!!!! ADD DATA,!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    ***************************************************************************************** */


    private void updateParcels(Parcel p){
        //update item in Realm
        Parcel updatedP = updateParcelsInRealm(p);
        //update item in Firebase
        if (firebase != null) {
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
            p.setReceiveDate(existingP.getReceiveDate());
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
        packagesRef.child(updateID).setValue(p).addOnCompleteListener(
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


} //end my packages activity