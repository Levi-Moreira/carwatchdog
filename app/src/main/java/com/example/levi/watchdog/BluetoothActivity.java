package com.example.levi.watchdog;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class BluetoothActivity extends AppCompatActivity {

    private static BluetoothAdapter BTAdapter;
    public static int REQUEST_BLUETOOTH = 1;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    private static UUID myUUID =  UUID.fromString("60b02ec4-c5a6-4eb4-bc25-02b69c361073");
    private static ArrayList<BluetoothDevice> deviceItemList = new ArrayList<BluetoothDevice>();;
    private static ArrayList<BluetoothDevice> deviceSearched = new ArrayList<>();

    private static ConnectThread connectDevice;

    private static PairedDevicesAdapter mPairedAdapter;
    private static PairedDevicesAdapter mDiscoveredAdapter;
    private static BluetoothDevice deviceToConnect;

    public BroadcastReceiver bReciever = new BroadcastReceiver()
    {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action))
            {
                progressBar.show();
            }
            else if (BluetoothDevice.ACTION_PAIRING_REQUEST.equals(action))
            {
                foundDevicesDialog.dismiss();

            }
            else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action))
            {

                inflatePairedDevicesList();

            }
            else if (BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED.equals(action))
            {
                Toast.makeText(getBaseContext(),"CONNECTED",
                        Toast.LENGTH_SHORT).show();


            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
            {
                progressBar.cancel();
                foundDevicesDialog.show();


            }
            if (BluetoothDevice.ACTION_FOUND.equals(action))
            {

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Create a new device item
                BluetoothClass classe = device.getBluetoothClass();
                int deviceType = classe.getDeviceClass();


                if((deviceType != BluetoothClass.Device.PHONE_CELLULAR)&&(deviceType != BluetoothClass.Device.PHONE_SMART)) {
                    // Add it to our adapter
                    deviceSearched.add(device);
                    mDiscoveredAdapter.notifyDataSetChanged();
                }

            }
        }
    };


    private ProgressDialog progressBar;
    private AlertDialog.Builder foundDevicesBuilder = new AlertDialog.Builder(BluetoothActivity.this);;
    private AlertDialog foundDevicesDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check?
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant location access so this app can detect beacons.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener()
                                             {
                                                 @Override
                                                 public void onDismiss(DialogInterface dialog)
                                                 {
                                                     requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                                                 }
                                             }
                );
                builder.show();
            }
            }

        setContentView(R.layout.activity_bluetooth);
        setUpBluetooth();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("PERMISSION BLUETOOTH", "coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                return;
            }
        }
    }


    public void addDevice(View view)
    {
        progressBar = new ProgressDialog(this);


        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
        filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);

        registerReceiver(bReciever, filter);


        //cancel any prior BT device discovery
        if (BTAdapter.isDiscovering()){
            BTAdapter.cancelDiscovery();
        }
        BTAdapter.startDiscovery();

        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.setTitle("Searching Devices...");



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

        mDiscoveredAdapter = new PairedDevicesAdapter(this,R.layout.devices_list_item,deviceSearched);
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

    private void setUpBluetooth()
    {
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);

        BTAdapter = bluetoothManager.getAdapter();
        // Phone does not support Bluetooth so let the user know and exit.
        if (BTAdapter == null) {
            new AlertDialog.Builder(this)
                    .setTitle("Not compatible")
                    .setMessage("Your phone does not support Bluetooth")
                    .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            System.exit(0);
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }

        if (!BTAdapter.isEnabled()) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);

        }


        mPairedAdapter = new PairedDevicesAdapter(this,R.layout.devices_list_item,deviceItemList);
        inflatePairedDevicesList();


        ListView pairedDevicesList = (ListView) findViewById(R.id.devices_list);
        pairedDevicesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                deviceToConnect = mPairedAdapter.getItem(position);

            }
        });

        pairedDevicesList.setAdapter(mPairedAdapter);
    }

    private void inflatePairedDevicesList() {
        Set<BluetoothDevice> pairedDevices = BTAdapter.getBondedDevices();
        if ((pairedDevices.size() != mPairedAdapter.getCount())&&(pairedDevices.size()>0)) {
            mPairedAdapter.clear();
            for (BluetoothDevice device : pairedDevices) {
                BluetoothClass classe = device.getBluetoothClass();
                int deviceClass = classe.getDeviceClass();

                if((deviceClass != BluetoothClass.Device.PHONE_CELLULAR)&&(deviceClass != BluetoothClass.Device.PHONE_SMART))
                {

                    deviceItemList.add(device);
                    mPairedAdapter.notifyDataSetChanged();
                }

            }
        }
    }


}
