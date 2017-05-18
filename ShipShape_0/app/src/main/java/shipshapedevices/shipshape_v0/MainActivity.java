package shipshapedevices.shipshape_v0;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private String userName;
    private int mId=1;
    private DatabaseReference fireDataBase;

    // Linking views
    @BindView(R.id.addPckgBtn) Button addPckgBtn;
    @BindView(R.id.myPckgsBtn) Button myPckgsBtn;
    @BindView(R.id.statBtn) Button statBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Link layout
        setContentView(R.layout.activity_main);
        // Bind views
        ButterKnife.bind(this);

        userName = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Add button listeners
        // Add Package Option:
        addPckgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //change to add parcel activity
                Intent i = new Intent(getApplicationContext(), AddPckgActivity.class);
                //include the sync state
                Log.d(TAG,"Entering add package activity.");
                startActivity(i);
            }
        });

        // My Packages Option:
        myPckgsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //change to my parcels activity
                Intent i = new Intent(getApplicationContext(), MyPackagesActivity.class);
                //include the sync state
                Log.d(TAG,"Entering my packages activity.");
                startActivity(i);
            }
        });

        // TODO: 5/17/2017 add statistics activity
        // Package Statistics Option:
//        statBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                //change to statistics activity
//                Intent i = new Intent(getApplicationContext(), StatisticsActivity.class);
//                //include the sync state
//                Log.d(TAG,"Entering graphing activity.");
//                startActivity(i);
//            }
//        });

        // Get instance off of firebase database
        fireDataBase= FirebaseDatabase.getInstance().getReference("users"); //
        fireDataBase.child(userName).addChildEventListener(new ChildEventListener() {
            @Override

            // when a child is added notify the user a package has shipped
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "On child added listener");
                dataSnapshot.getValue();
                CreateNotification("Create Notification Test String");
                //TODO startActivity(new Intent(getApplicationContext(), MyPackagesActivity.class));
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "The updated post title is: " + dataSnapshot.getValue());
                CreateNotification("Packaged Received");
                //TODO startActivity(new Intent(getApplicationContext(), MyPackagesActivity.class));
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

        }); // End on Childe Event listener
    }

    private void CreateNotification(String notifyText) {

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                        .setContentTitle("Notification Title Test")
                        .setContentText(notifyText);
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, MainActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(mId, mBuilder.build());

    } // end create notification
}
