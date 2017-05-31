package com.shipshapedevicesgmail.wifitesting;

        import java.io.BufferedReader;
        import java.io.BufferedWriter;
        import java.io.DataInputStream;
        import java.io.DataOutputStream;
        import java.io.IOException;
        import java.io.InputStreamReader;
        import java.io.OutputStreamWriter;
        import java.net.InetSocketAddress;
        import java.net.Socket;
        import java.net.UnknownHostException;
        import java.util.List;

        import android.app.Activity;
        import android.content.Context;
        import android.net.wifi.SupplicantState;
        import android.net.wifi.WifiConfiguration;
        import android.net.wifi.WifiInfo;
        import android.net.wifi.WifiManager;
        import android.os.Bundle;
        import android.os.StrictMode;
        import android.support.v7.app.AppCompatActivity;
        import android.util.Log;
        import android.view.View;
        import android.widget.Button;
        import android.widget.EditText;
        import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    EditText textOut;
    TextView textIn;
    int i = 0;
    String networkSSID = "ShipShapeWIFI";
    String currentSSID;
    WifiManager wifiManager;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textOut = (EditText) findViewById(R.id.textout);
        Button buttonSend = (Button) findViewById(R.id.send);
        textIn = (TextView) findViewById(R.id.textin);
        buttonSend.setOnClickListener(buttonSendOnClickListener);
        connectShipShape();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disconnectShipShape();
    }

    Button.OnClickListener buttonSendOnClickListener
            = new Button.OnClickListener() {

        @Override
        public void onClick(View arg0) {
            // TODO Auto-generated method stub
            ConnectionHandler myConnectionHandler = new ConnectionHandler() {
                @Override
                public void didReceiveData(String data) {
                    if(data != null) {
                        Log.d("Ya boi", "We CONNECTED and got: " + data);
                    }

                }

                @Override
                public void didDisconnect(Exception error) {

                }

                @Override
                public void didConnect() {
                    Log.d("Ya boi", "We CONNECTED");
                }
            };
            //Socket socket = null;
            //DataOutputStream dataOutputStream = null;
            //DataInputStream dataInputStream = null;
            Log.d("Ya boi", "Clicked");
            //connectShipShape();
            AsyncConnection myAsync = new AsyncConnection("192.168.4.1", 80, 100000, myConnectionHandler);
            myAsync.execute();
            /*try {
                AsyncConnection myAsync = new AsyncConnection("202.202.1.1", 202, 1000, myConnectionHandler);
                //socket = new Socket("202.202.1.1", 202);
                //dataOutputStream = new DataOutputStream(socket.getOutputStream());
                //dataInputStream = new DataInputStream(socket.getInputStream());
                //dataOutputStream.writeUTF(textOut.getText().toString());
                //textIn.setText(dataInputStream.readUTF());
            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            finally{
                if (socket != null){
                    try {
                        socket.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

                if (dataOutputStream != null){
                    try {
                        dataOutputStream.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

                if (dataInputStream != null){
                    try {
                        dataInputStream.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }*/
        }
    };

    public void disconnectShipShape() {
        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
        Log.i("list", "connected" + list);
        // reconnect to old network
        for (WifiConfiguration i : list) {
            if (i.SSID != null && i.SSID.equals(currentSSID)) {
                wifiManager.disconnect();
                wifiManager.enableNetwork(i.networkId, true);
                wifiManager.reconnect();
                Log.i("main", "Stanford connected");
                break;
            }
        }
    }

    public void connectShipShape() {
        wifiManager = (WifiManager) MainActivity.this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiConfiguration conf = new WifiConfiguration();
        //get info for current wifi
        WifiInfo wifiInfo;
        wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo.getSupplicantState() == SupplicantState.COMPLETED) {
            currentSSID = wifiInfo.getSSID();
            Log.i("list", "got current ssid:" + currentSSID);

        }


        conf.SSID = "\"" + networkSSID + "\"";
        conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);


        wifiManager.addNetwork(conf);

        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
        Log.i("list", "connected" + list);

        for (WifiConfiguration i : list) {
            if (i.SSID != null && i.SSID.equals("\"" + networkSSID + "\"")) {
                wifiManager.disconnect();
                wifiManager.enableNetwork(i.networkId, true);
                wifiManager.reconnect();
                Log.i("main", "connected");
                break;
            }
        }
    }
}