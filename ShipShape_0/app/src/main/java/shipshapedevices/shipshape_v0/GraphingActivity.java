package shipshapedevices.shipshape_v0;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.realm.implementation.RealmLineDataSet;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.DecimalFormat;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;


public class GraphingActivity extends AppCompatActivity {

    @BindView(R.id.accel_graph)
    LineChart accelChart;
    @BindView(R.id.humid_graph)
    LineChart humidChart;
    @BindView(R.id.temp_graph)
    LineChart tempChart;
    private Realm realm;
    private RealmResults realmResults;
    private RealmList realmList;
    private RealmResults<Data> tempData;
    private RealmResults<ImpactEvent> impactEvents;
    private RealmResults<Data> accelData;
    private RealmResults<Data> humidData;
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
        String myParcel = bundle.getString("CurrentParcelID");

        // load  data from Realm database  -KkKULGxnrO1pB3y-ldQ
//        //get parcel Id
//        String myParcel = "-KkKULGxnrO1pB3y-ldQ";
//        //search for parcel
        //String myParcel = getParcelID; ///"parcelID",
        Parcel parcel = realm.where(Parcel.class).equalTo("parcelID", myParcel).findFirst();

        //get tempdata
        tempData = parcel.getTempLog().where().findAll();
        humidData = parcel.getHumidLog().where().findAll();
        impactEvents = parcel.getImpactEvents().where().findAll();
        accelData = impactEvents.first().getAccelLog().where().findAll();

        // create a DataSet and specify fields, MPAndroidChart-Realm does the rest
        lineDataSet = new RealmLineDataSet<>(tempData, "time", "value");

        //set label
        lineDataSet.setLabel("Temp Data");
        // create a data object with the dataset
        LineData lineTempData = new LineData(lineDataSet);

        tempChart.setData(lineTempData);
        tempChart.getDescription().setText("Temperature");
        tempChart.animateXY(200, 200);
        YAxis tempAxis = tempChart.getAxisLeft();
        tempAxis.removeAllLimitLines(); // reset all limit lines to avoid overlapping lines
        tempAxis.setAxisMaximum(100);
        tempAxis.setAxisMinimum(50);

        // create a DataSet and specify fields, MPAndroidChart-Realm does the rest
        lineDataSet = new RealmLineDataSet<>(humidData, "time", "value");

        //set label
        lineDataSet.setLabel("Humid Data");
        // create a data object with the dataset
        LineData lineHumidData = new LineData(lineDataSet);

        humidChart.setData(lineHumidData);
        humidChart.getDescription().setText("Humidity");
        humidChart.animateXY(200, 200);
        YAxis humidAxis = humidChart.getAxisLeft();
        humidAxis.removeAllLimitLines(); // reset all limit lines to avoid overlapping lines
        humidAxis.setAxisMaximum(100);
        humidAxis.setAxisMinimum(20);

        // create a DataSet and specify fields, MPAndroidChart-Realm does the rest
        lineDataSet = new RealmLineDataSet<>(accelData, "time", "value");

        //set label
        lineDataSet.setLabel("Accel Data");
        // create a data object with the dataset
        LineData lineAccelData = new LineData(lineDataSet);

        accelChart.setData(lineAccelData);
        accelChart.getDescription().setText("Acceleration");
        accelChart.animateXY(200, 200);
        YAxis accelAxis = accelChart.getAxisLeft();
        accelAxis.removeAllLimitLines(); // reset all limit lines to avoid overlapping lines
        accelAxis.setAxisMaximum(20);
        accelAxis.setAxisMinimum(-0);

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



