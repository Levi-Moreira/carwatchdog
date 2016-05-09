package com.example.levi.watchdog;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;
import java.util.logging.Handler;

/**
 * Created by Vini on 12/02/2016.
 */
public class ConnectThread extends Thread  {
    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;
    private static UUID myUUID =  UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static ConnectedThread manageConnection;
    private static BluetoothAdapter BTAdapter;
    private static int mConnectionStatus = 0;


    public static int getmConnectionStatus() {
        return mConnectionStatus;
    }

    public BluetoothDevice getMmDevice() {
        return mmDevice;
    }

    public static ConnectedThread getManageConnection() {
        return manageConnection;
    }

    public ConnectThread(BluetoothDevice device, BluetoothAdapter BTAdapter) {
        // Use a temporary object that is later assigned to mmSocket,
        // because mmSocket is final
        //Log.d("ConnectThread", "Entered Connect Thread");
        mConnectionStatus = 0;
        BluetoothSocket tmp = null;
        mmDevice = device;

        this.BTAdapter = BTAdapter;

        // Get a BluetoothSocket to connect with the given BluetoothDevice
        try {
            // MY_UUID is the app's UUID string, also used by the server code
            tmp = device.createRfcommSocketToServiceRecord(myUUID);
        } catch (IOException e) { mConnectionStatus = 2;}
        mmSocket = tmp;
    }

    public void run() {
        // Cancel discovery because it will slow down the connection
        BTAdapter.cancelDiscovery();

        try {
            // Connect the device through the socket. This will block
            // until it succeeds or throws an exception
            mmSocket.connect();
        } catch (IOException connectException) {
            // Unable to connect; close the socket and get out
            mConnectionStatus = 2;
            try {
                mmSocket.close();
            } catch (IOException closeException) { }
            return;
        }
        Log.d("ConnectThread","Socket Created");
        // Do work to manage the connection (in a separate thread)
        mConnectionStatus = 1;
        manageConnection = new ConnectedThread(mmSocket);
        manageConnection.start();

    }

    /** Will cancel an in-progress connection, and close the socket */
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) { }
    }
}