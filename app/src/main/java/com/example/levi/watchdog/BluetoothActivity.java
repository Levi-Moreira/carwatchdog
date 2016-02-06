package com.example.levi.watchdog;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
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
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class BluetoothActivity extends AppCompatActivity {
    private static BluetoothAdapter BTAdapter;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    private static ArrayList<DeviceItem> deviceItemList = new ArrayList<DeviceItem>();;
    private static ArrayList<DeviceItem> deviceSearched = new ArrayList<>();

    private static PairedDevicesAdapter mPairedAdapter;
    private static ArrayAdapter<DeviceItem> mDiscoveredAdapter;

    private ProgressDialog progressBar;
    private  AlertDialog.Builder foundDevicesDialog;

    public static int REQUEST_BLUETOOTH = 1;

    public BroadcastReceiver bReciever;


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
        Set<BluetoothDevice> pairedDevices = BTAdapter.getBondedDevices();
        if ((pairedDevices.size() != mPairedAdapter.getCount())&&(pairedDevices.size()>0)) {
            mPairedAdapter.clear();
            for (BluetoothDevice device : pairedDevices) {
                DeviceItem newDevice= new DeviceItem(device.getName(),device.getAddress(),"false");
                deviceItemList.add(newDevice);

            }
        }


        ListView listView = (ListView) findViewById(R.id.devices_list);
        listView.setAdapter(mPairedAdapter);

    }


    public void addDevice(View view)
    {
        progressBar = new ProgressDialog(this);
        bReciever = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action))
                {
                    progressBar.show();
                }
                else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
                {
                    progressBar.cancel();
                    foundDevicesDialog.show();
                }
                if (BluetoothDevice.ACTION_FOUND.equals(action))
                {
                    progressBar.cancel();


                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    // Create a new device item
                    DeviceItem newDevice = new DeviceItem(device.getName(), device.getAddress(), "false");
                    // Add it to our adapter
                    deviceSearched.add(newDevice);
                    mDiscoveredAdapter.notifyDataSetChanged();

                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(bReciever, filter);
        mPairedAdapter.clear();


        //cancel any prior BT device discovery
        if (BTAdapter.isDiscovering()){
            BTAdapter.cancelDiscovery();
        }
        BTAdapter.startDiscovery();
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.setTitle("Searching Devices...");


        foundDevicesDialog = new AlertDialog.Builder(BluetoothActivity.this);
        foundDevicesDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                unregisterReceiver(bReciever);
                BTAdapter.cancelDiscovery();
                Toast.makeText(getApplicationContext(), "Discovery Unregistered", Toast.LENGTH_SHORT).show();
            }
        });
        LayoutInflater inflater = getLayoutInflater();
        View convertView = (View) inflater.inflate(R.layout.discovered_devices, null);
        foundDevicesDialog.setView(convertView);

        foundDevicesDialog.setTitle(R.string.bt_dialog_message);
        ListView devicesList = (ListView) convertView.findViewById(R.id.discovered_d);

        mDiscoveredAdapter = new ArrayAdapter<DeviceItem>(this,R.layout.devices_list_item,deviceSearched);

       /*DeviceItem di = new DeviceItem("Levi","Levi","levi");
        mDiscoveredAdapter.add(di);*/
        devicesList.setAdapter(mDiscoveredAdapter);

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

}
