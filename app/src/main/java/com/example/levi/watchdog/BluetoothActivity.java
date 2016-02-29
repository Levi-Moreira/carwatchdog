package com.example.levi.watchdog;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Handler;


public class BluetoothActivity extends AppCompatActivity {

    private static BluetoothAdapter BTAdapter = BluetoothAdapter.getDefaultAdapter();

    SharedPreferences preferences;
    private static ArrayList<BluetoothDevice> pairedDeviceArray = new ArrayList<BluetoothDevice>();;
    private static ArrayList<BluetoothDevice> discoveredDeviceArray = new ArrayList<BluetoothDevice>();

    private static PairedDevicesAdapter mPairedAdapter;
    private static PairedDevicesAdapter mDiscoveredAdapter;
    private static BluetoothDevice deviceToConnect;

    private  static ConnectThread connectionService;
    private  static ConnectedThread connectionManager;

    private static byte[] Cmd = {0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00};

    public BroadcastReceiver bReciever = new BroadcastReceiver()
    {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action))
            {
                progressBar.show();
                discoveredDeviceArray.clear();
                mDiscoveredAdapter.clear();
            }
            else if (BluetoothDevice.ACTION_PAIRING_REQUEST.equals(action))
            {
                foundDevicesDialog.dismiss();

            }
            else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action))
            {
                Toast.makeText(getApplicationContext(), "CONNECTED!!!", Toast.LENGTH_SHORT).show();

            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
            {
                progressBar.cancel();

                if(discoveredDeviceArray.isEmpty())
                {
                    Toast.makeText(getApplicationContext(), "No New Devices Found", Toast.LENGTH_SHORT).show();
                }else
                {
                    foundDevicesDialog.show();
                }
            }
            if (BluetoothDevice.ACTION_FOUND.equals(action))
            {

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Create a new device item
                BluetoothClass classe = device.getBluetoothClass();
                int deviceType = classe.getDeviceClass();


                if((device.getBondState()!=BluetoothDevice.BOND_BONDED)&&(deviceType != BluetoothClass.Device.PHONE_CELLULAR)&&(deviceType != BluetoothClass.Device.PHONE_SMART)) {
                    // Add it to our adapter
                    if(!discoveredDeviceArray.contains(device))
                    {
                        discoveredDeviceArray.add(device);
                        mDiscoveredAdapter.notifyDataSetChanged();
                    }
                }

            }
        }
    };


    private ProgressDialog progressBar;
    private ProgressDialog connectingProgressBar;
    private AlertDialog.Builder foundDevicesBuilder ;
    private AlertDialog foundDevicesDialog;

    @Override
    protected void onResume() {
        super.onResume();


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        setContentView(R.layout.activity_bluetooth);
        inflatePairedDevicesList();


    }


    public void addDevice(View view)
    {
        progressBar = new ProgressDialog(this);


        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        registerReceiver(bReciever, filter);


        //cancel any prior BT device discovery
        if (BTAdapter.isDiscovering()){
            BTAdapter.cancelDiscovery();
        }
        BTAdapter.startDiscovery();

        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.setTitle("Searching Devices...");


        foundDevicesBuilder = new AlertDialog.Builder(BluetoothActivity.this);
        foundDevicesBuilder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                unregisterReceiver(bReciever);
                BTAdapter.cancelDiscovery();
                Toast.makeText(getApplicationContext(), "Discovery Unregistered", Toast.LENGTH_SHORT).show();
            }
        });


        LayoutInflater inflater = getLayoutInflater();
        View foundDevicesDialogView = (View) inflater.inflate(R.layout.discovered_devices, null);

        foundDevicesBuilder.setView(foundDevicesDialogView);
        foundDevicesBuilder.setTitle(R.string.bt_dialog_message);
        foundDevicesDialog = foundDevicesBuilder.create();

        ListView foundDevicesList = (ListView) foundDevicesDialogView.findViewById(R.id.discovered_d);



        foundDevicesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {


                deviceToConnect = mDiscoveredAdapter.getItem(position);
                pairDevice(deviceToConnect);


            }
        });

        mDiscoveredAdapter = new PairedDevicesAdapter(this,R.layout.devices_list_item, discoveredDeviceArray);
        foundDevicesList.setAdapter(mDiscoveredAdapter);

    }


    private void pairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private void inflatePairedDevicesList() {

       mPairedAdapter = new PairedDevicesAdapter(this,R.layout.devices_list_item, pairedDeviceArray);
       connectingProgressBar= new ProgressDialog(this);

        ListView pairedDevicesList = (ListView) findViewById(R.id.devices_list);
        pairedDevicesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                deviceToConnect = mPairedAdapter.getItem(position);
                connectionService = new ConnectThread(deviceToConnect,BTAdapter);
                connectionService.start();

                connectingProgressBar.setTitle("Connecting to Device: " + deviceToConnect.getName());
                new ConnectTask().execute();

            }
        });

        pairedDevicesList.setAdapter(mPairedAdapter);

        Set<BluetoothDevice> pairedDevices = BTAdapter.getBondedDevices();
        if ((pairedDevices.size() != mPairedAdapter.getCount())&&(pairedDevices.size()>0)) {
            mPairedAdapter.clear();
            for (BluetoothDevice device : pairedDevices) {
                BluetoothClass classe = device.getBluetoothClass();
                int deviceClass = classe.getDeviceClass();

                if((deviceClass != BluetoothClass.Device.PHONE_CELLULAR)&&(deviceClass != BluetoothClass.Device.PHONE_SMART))
                {

                    pairedDeviceArray.add(device);
                    mPairedAdapter.notifyDataSetChanged();
                }

            }
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    public class ConnectTask extends AsyncTask <Void,Void,Void>
    {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            connectingProgressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            connectingProgressBar.show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(connectionService.getmConnectionStatus()==1)
            {

                connectingProgressBar.dismiss();

                connectionManager = connectionService.getManageConnection();
                String password = preferences.getString(getString(R.string.password_pref_key),getString(R.string.pref_setting_password_default));
                Cmd[1] = 0X10;
                Cmd[2] = (byte)( Integer.parseInt(password)>>8);
                Cmd[3] = (byte)( Integer.parseInt(password));
                connectionManager.write(Cmd);

                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                intent.putExtra("connectedDeviceAddress", connectionService.getMmDevice());
                startActivity(intent);


            }
            else if(connectionService.getmConnectionStatus()==2)
            {
                Toast.makeText(getApplicationContext(), "Connection Error", Toast.LENGTH_SHORT).show();
                connectingProgressBar.dismiss();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {


            while((connectionService.getmConnectionStatus()==0)&&(connectionService.getmConnectionStatus()!=2))
            {

            }
            return null;
        }
    }
}
