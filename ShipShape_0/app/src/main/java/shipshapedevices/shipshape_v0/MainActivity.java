package shipshapedevices.shipshape_v0;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.my_packages_button)
    Button my_pack_butt;

    @BindView(R.id.add_package_button)
    Button add_pack_butt;

    @BindView(R.id.overall_statistics_button)
    Button stat_butt;

    static int count = 0;
    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);
        ButterKnife.bind(this);
    }

    @Override
    protected void onStart(){
        super.onStart();
    }

    @Override
    protected void onResume(){
        super.onResume();

        // Set up click listener for the My Packages Button
        my_pack_butt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ctrl2mypac = new Intent(MainActivity.this,MyPackagesActivity.class);
                startActivity(ctrl2mypac);
            }
        });

        // Set up click listener for the Add Package Button
        add_pack_butt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent ctrl2addpac = new Intent(MainActivity.this,AddPackagesActivity.class);
                //startActivity(ctrl2addpac);
                realm = Realm.getDefaultInstance();
                realm.beginTransaction();
                Parcel p = new Parcel(Integer.toString(count));
                Toast.makeText(MainActivity.this, Integer.toString(count),Toast.LENGTH_SHORT).show();
                p.setShipperID("Matthew");
                realm.copyToRealmOrUpdate(p);
                realm.commitTransaction();
                count++;
                Toast.makeText(MainActivity.this, "Update this button press to incorporate Luke's stuff", Toast.LENGTH_SHORT).show();
            }
        });

        // Set up click listener for the Overall Statistics Button
        stat_butt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ctrl2stat = new Intent(MainActivity.this,GraphingActivity.class);
                startActivity(ctrl2stat);
            }
        });
    }
}
