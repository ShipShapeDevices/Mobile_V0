package shipshapedevices.shipshape_v0;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.moonmonkeylabs.realmrecyclerview.RealmRecyclerView;
import io.realm.Realm;
import io.realm.RealmBasedRecyclerViewAdapter;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import io.realm.RealmViewHolder;
import io.realm.Sort;

public class MyPackagesActivity extends RealmBaseActivity {

    private PackageRecyclerViewAdapter packageAdapter;
    private Realm realm;
    private DatabaseReference firebase;

    @BindView(R.id.realm_recycler_view)
    RealmRecyclerView realmRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_packages);
        ButterKnife.bind(this);
        realm = Realm.getDefaultInstance();
        RealmResults<Parcel> realmResults = realm.where(Parcel.class).findAll();
        firebase = FirebaseDatabase.getInstance().getReference();

        //clear realm
        realm.beginTransaction();
        realmResults.deleteAllFromRealm();
        realm.commitTransaction();


        firebase.child("parcels").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Parcel data = dataSnapshot.getValue(Parcel.class);
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

        RealmResults<Parcel> parcels = realm.where(Parcel.class).findAllSorted("parcelID", Sort.ASCENDING);
        packageAdapter = new PackageRecyclerViewAdapter(getBaseContext(), parcels);
        realmRecyclerView.setAdapter(packageAdapter);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        realm.close();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Toast.makeText(MyPackagesActivity.this, "Or change this?", Toast.LENGTH_SHORT).show();
        //return super.onOptionsItemSelected(item);
        return true;
    }

    public class PackageRecyclerViewAdapter extends RealmBasedRecyclerViewAdapter<Parcel, PackageRecyclerViewAdapter.ViewHolder> {

        public PackageRecyclerViewAdapter(
                Context context,
                RealmResults<Parcel> realmResults) {
            super(context, realmResults, true, true, false, "parcelID");
        }

        public class ViewHolder extends RealmViewHolder {
            public FrameLayout container;
            public TextView shipperTextView;
            public TextView parcelTextView;

            public ViewHolder(FrameLayout container) {
                super(container);
                this.container = container;
                this.parcelTextView = (TextView) container.findViewById(R.id.row_parcel_id);
                this.shipperTextView = (TextView) container.findViewById(R.id.row_shipper_id);
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
            viewHolder.parcelTextView.setText(parcel.getParcelID());
            viewHolder.shipperTextView.setText(parcel.getShipperID());
            viewHolder.itemView.setOnClickListener(
                    new View.OnClickListener(){
                        @Override
                        public void onClick(View v){
                            Intent mypac2stat = new Intent(MyPackagesActivity.this,GraphingActivity.class);
                            mypac2stat.putExtra("CurrentParcelID", parcel.getParcelID());
                            startActivity(mypac2stat);
                            Toast.makeText(MyPackagesActivity.this, parcel.getParcelID(), Toast.LENGTH_SHORT).show();
                        }
                    }
            );
        }
    }
}