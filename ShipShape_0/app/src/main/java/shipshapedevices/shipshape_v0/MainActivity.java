package shipshapedevices.shipshape_v0;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    // Linking views
    @BindView(R.id.addPckgBtn) Button addPckgBtn;
    @BindView(R.id.transitPckgBtn) Button transitPckgBtn;
    @BindView(R.id.existPckgBtn) Button existPckgBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Link layout
        setContentView(R.layout.activity_main);
        // Bind views
        ButterKnife.bind(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Add button listeners
        // Add Package Option:
        addPckgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //change to ride history activity
                Intent i = new Intent(getApplicationContext(), AddPckgActivity.class);
                //include the sync state
                Log.d(TAG,"Entering add package activity.");
                startActivity(i);
            }
        });

        // In-Transit Packages Option:
        transitPckgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //change to ride history activity
                Intent i = new Intent(getApplicationContext(), AddPckgActivity.class);
                //include the sync state
                Log.d(TAG,"Entering in-transit packages activity.");
                startActivity(i);
            }
        });

        // Existing Packages Option:
        existPckgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //change to ride history activity
                Intent i = new Intent(getApplicationContext(), AddPckgActivity.class);
                //include the sync state
                Log.d(TAG,"Entering existing packages activity.");
                startActivity(i);
            }
        });

    }
}
