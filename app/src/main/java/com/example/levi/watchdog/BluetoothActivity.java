package com.example.levi.watchdog;

import android.app.Dialog;
import android.app.DialogFragment;
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
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Set;

public class BluetoothActivity extends AppCompatActivity {
    private static  BluetoothAdapter BTAdapter;
    private static ArrayList<DeviceItem> deviceItemList;
    private static ArrayAdapter<DeviceItem> mAdapter;
    private static ArrayList<DeviceItem> deviceSearched;
    public static int REQUEST_BLUETOOTH = 1;

    private final BroadcastReceiver bReciever = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Create a new device item
                DeviceItem newDevice = new DeviceItem(device.getName(), device.getAddress(), "false");
                // Add it to our adapter
                deviceSearched.add(newDevice);
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

        deviceItemList = new ArrayList<DeviceItem>();

        Set<BluetoothDevice> pairedDevices = BTAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                DeviceItem newDevice= new DeviceItem(device.getName(),device.getAddress(),"false");
                deviceItemList.add(newDevice);
            }
        }

        mAdapter = new ArrayAdapter<DeviceItem>(this,R.layout.devices_list,deviceItemList);
        ListView listView = (ListView) findViewById(R.id.devices_list);
        listView.setAdapter(mAdapter);

    }

    public void addDevice(View view)
    {
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        mAdapter.clear();
        this.registerReceiver(bReciever, filter);
        BTAdapter.startDiscovery();
        android.support.v4.app.DialogFragment  newFragment = new DevicesFoundDialog();
        newFragment.show(getSupportFragmentManager(), "searcheddevices");


    }

    public class DevicesFoundDialog extends android.support.v4.app.DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            deviceSearched = new ArrayList<DeviceItem>();
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.bt_dialog_message)
                    .setItems(deviceSearched.toArray(new CharSequence[deviceSearched.size()]), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // The 'which' argument contains the index position
                            // of the selected item
                        }
                    });
            return builder.create();
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            getActivity().unregisterReceiver(bReciever);
            BTAdapter.cancelDiscovery();
        }
    }

}
