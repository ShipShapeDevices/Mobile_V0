package shipshapedevices.shipshape_v0;

import android.content.Context;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import com.github.mikephil.charting.data.realm.implementation.RealmBarDataSet;
import com.github.mikephil.charting.data.realm.implementation.RealmLineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.RealmSchema;


public class GraphingActivity extends AppCompatActivity {

    @BindView(R.id.accel_graph)
    LineChart accelChart;
    @BindView(R.id.humid_graph)
    LineChart humidChart;
    @BindView(R.id.temp_graph)
    BarChart tempChart;
    private Realm realm;
    private RealmResults realmResults;
    private RealmList realmList;
    private RealmResults<Data> tempData;
    private RealmResults<ImpactEvent> impactEvents;
    private RealmResults<Data> accelData;
    private RealmResults<Data> humidData;
    private RealmBarDataSet<Data> barDataSet;
    private RealmLineDataSet<Data> lineDataSet;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graphing);
        // Butterknife bind
        ButterKnife.bind(this);
        //get realm
        realm = Realm.getDefaultInstance();

        //myPackagesActivity
        Bundle bundle = getIntent().getExtras();
        String myParcel = bundle.getString("message");

        // load  data from Realm database  -KkKULGxnrO1pB3y-ldQ
//        //get parcel Id
//        String myParcel = "-KkKULGxnrO1pB3y-ldQ";
//        //search for parcel
        //String myParcel = getParcelID; ///"parcelID",
        //Parcel parcel = realm.where(Parcel.class).equalTo("parcelID", myParcel).findFirst();
        Parcel parcel = realm.where(Parcel.class).equalTo("parcelID", myParcel).findFirst();

        //Parcel parcel = realm.where(Parcel.class).findFirst();

        //get tempdata
         tempData = parcel.getTempLog().where().findAll();
         humidData = parcel.getHumidLog().where().findAll();
         impactEvents = parcel.getImpactEvents().where().findAll();
         accelData = impactEvents.first().accelLog.where().findAll();


        // create a DataSet and specify fields, MPAndroidChart-Realm does the rest
        barDataSet = new RealmBarDataSet<>(tempData, "time", "value");


        //set label
        barDataSet.setLabel("Temp Data");
        // create a data object with the dataset
        BarData barTempData = new BarData(barDataSet);

        tempChart.setData(barTempData);
        tempChart.getDescription().setText("Temperature");
        tempChart.animateXY(200, 200);


        // create a DataSet and specify fields, MPAndroidChart-Realm does the rest
        lineDataSet = new RealmLineDataSet<>(humidData, "time", "value");

        //set label
        lineDataSet.setLabel("Humid Data");
        // create a data object with the dataset
        LineData lineHumidData = new LineData(lineDataSet);

        humidChart.setData(lineHumidData);
        humidChart.getDescription().setText("Humidity");
        humidChart.animateXY(200, 200);


        // create a DataSet and specify fields, MPAndroidChart-Realm does the rest
        lineDataSet = new RealmLineDataSet<>(accelData, "time", "value");

        //set label
        lineDataSet.setLabel("Accel Data");
        // create a data object with the dataset
        LineData lineAccelData = new LineData(lineDataSet);

        accelChart.setData(lineAccelData);
        accelChart.getDescription().setText("Acceleration");
        accelChart.animateXY(200, 200);

        //send data to charts
        accelChart.invalidate(); // refresh
        humidChart.invalidate(); // refresh
        tempChart.invalidate(); // refresh






    }




    @Override
    protected void onDestroy() {
        super.onDestroy();
        //close realm
        realm.close();

    }

    private class MyValueFormatter implements IValueFormatter {

        private DecimalFormat mFormat;

        public MyValueFormatter() {
            mFormat = new DecimalFormat("###,###,##0.0"); // use one decimal
        }

        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            // write your logic here
            return mFormat.format(value) + " $"; // e.g. append a dollar-sign
            //return mFormat.format(value).; // change to float

        }
    }

}



