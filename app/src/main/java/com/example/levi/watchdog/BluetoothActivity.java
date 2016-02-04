package com.example.levi.watchdog;

import android.app.Dialog;
import  android.support.v4.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

    private static ArrayList<DeviceItem> deviceItemList;
    private static ArrayList<DeviceItem> deviceSearched = new ArrayList<>();

    private static ArrayAdapter<DeviceItem> mPairedAdapter;
    private static ArrayAdapter<DeviceItem> mDiscoveredAdapter;


    public static int REQUEST_BLUETOOTH = 1;

    private  final BroadcastReceiver bReciever = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action))
            {
                Toast.makeText(getApplicationContext(), "Discovery Started", Toast.LENGTH_SHORT).show();
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
                 {
                    Toast.makeText(getApplicationContext(), "Discovery Finished", Toast.LENGTH_SHORT).show();
                 }
            if (BluetoothDevice.ACTION_FOUND.equals(action))
                      {
                            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                            // Create a new device item
                            DeviceItem newDevice = new DeviceItem(device.getName(), device.getAddress(), "false");
                            // Add it to our adapter
                            Toast.makeText(getApplicationContext(), "Discovery Found Decies", Toast.LENGTH_SHORT).show();
                            deviceSearched.add(newDevice);
                            mDiscoveredAdapter.notifyDataSetChanged();
                       }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        BTAdapter = BluetoothAdapter.getDefaultAdapter();
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
            Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBT, REQUEST_BLUETOOTH);
        }

        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);


        deviceItemList = new ArrayList<DeviceItem>();

        Set<BluetoothDevice> pairedDevices = BTAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                DeviceItem newDevice= new DeviceItem(device.getName(),device.getAddress(),"false");
                deviceItemList.add(newDevice);
            }
        }

        mPairedAdapter = new ArrayAdapter<DeviceItem>(this,R.layout.devices_list,deviceItemList);
        ListView listView = (ListView) findViewById(R.id.devices_list);
        listView.setAdapter(mPairedAdapter);

    }

    public void addDevice(View view)
    {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        registerReceiver(bReciever, filter);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        registerReceiver(bReciever, filter);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(bReciever, filter);
        mPairedAdapter.clear();


        //cancel any prior BT device discovery
        if (BTAdapter.isDiscovering()){
            BTAdapter.cancelDiscovery();
        }
        BTAdapter.startDiscovery();


        AlertDialog.Builder alertDialog = new AlertDialog.Builder(BluetoothActivity.this);
        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                unregisterReceiver(bReciever);
                BTAdapter.cancelDiscovery();
                Toast.makeText(getApplicationContext(), "Discovery Unregistered", Toast.LENGTH_SHORT).show();
            }
        });
        LayoutInflater inflater = getLayoutInflater();
        View convertView = (View) inflater.inflate(R.layout.discovered_devices, null);
        alertDialog.setView(convertView);

        alertDialog.setTitle(R.string.bt_dialog_message);
        ListView devicesList = (ListView) convertView.findViewById(R.id.discovered_d);

        mDiscoveredAdapter = new ArrayAdapter<DeviceItem>(this,R.layout.devices_list,deviceSearched);

       /*DeviceItem di = new DeviceItem("Levi","Levi","levi");
        mDiscoveredAdapter.add(di);*/
        devicesList.setAdapter(mDiscoveredAdapter);

        alertDialog.show();

    }

}
