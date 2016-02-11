package com.example.levi.watchdog; /**
 * Created by Vini on 11/02/2016.
 */
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by User on 6/3/2015.
 */
public class ConnectThread extends Thread{
    private static BluetoothAdapter BTAdapter;
    private final BluetoothDevice bTDevice;
    private final BluetoothSocket bTSocket;

    public ConnectThread(BluetoothDevice bTDevice, UUID UUID, BluetoothAdapter adapter) {
        BluetoothSocket tmp = null;
        this.bTDevice = bTDevice;
        this.BTAdapter = adapter;
        try {
            tmp = this.bTDevice.createRfcommSocketToServiceRecord(UUID);
        }
        catch (IOException e) {
            Log.d("CONNECTTHREAD", "Could not start listening for RFCOMM");
        }
        bTSocket = tmp;
    }

    public void run() {

        BTAdapter.cancelDiscovery();

        try {
            // Connect the device through the socket. This will block
            // until it succeeds or throws an exception
            bTSocket.connect();
        } catch (IOException connectException) {
            // Unable to connect; close the socket and get out
            try {
                bTSocket.close();
            } catch (IOException closeException) { }
            return;
        }

        // Do work to manage the connection (in a separate thread)
        //manageConnectedSocket(bTSocket);
    }

    public boolean cancel() {
        try {
            bTSocket.close();
        } catch(IOException e) {
            return false;
        }
        return true;
    }

}
