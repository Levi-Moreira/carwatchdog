package com.example.levi.watchdog;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * Created by Vini on 12/02/2016.
 */
public class ConnectedThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;

    public ConnectedThread(BluetoothSocket socket) {
        Log.d("ConnectThread", "Connected");
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        // Get the input and output streams, using temp objects because
        // member streams are final
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) { }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    public void run() {
        char[] buffer = new char[16];  // buffer store for the stream
        int bytes = 0; // bytes returned from read()
        int offset = 0;
        int buffCount = 16;

        // Keep listening to the InputStream until an exception occurs
        while (true) {
            try {


                BufferedReader r = new BufferedReader(new InputStreamReader(mmInStream));
                bytes= r.read(buffer,offset,buffCount);


                    if(buffer[1]==0x01)
                    {
                        Log.d("ConnectedThread","Received Keep Alive Command");
                    }

                // Send the obtained bytes to the UI activity
                    /*mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();*/
            } catch (IOException e) {
                break;
            }
        }
    }

    /* Call this from the main activity to send data to the remote device */
    public void write(byte[] bytes) {
        try {
            mmOutStream.write(bytes);
        } catch (IOException e) { }
    }

    /* Call this from the main activity to shutdown the connection */
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) { }
    }
}