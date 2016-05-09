package com.example.levi.watchdog;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * Created by Vini on 12/02/2016.
 */
public class ConnectedThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    char[] mReceiveBuffer = new char[16];  // mReceiveBuffer store for the stream
    int mReceiveBytes = 0; // mReceiveBytes returned from read()

    public char[] getReceivedData() {
        return mReceiveBuffer;
    }

    public int getReceivedDataAmount() {
        return mReceiveBytes;
    }

    public void setmReceiveBytes(int mReceiveBytes) {
        this.mReceiveBytes = mReceiveBytes;
    }

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
        mReceiveBuffer = new char[18];  // mReceiveBuffer store for the stream
        mReceiveBytes = 0; // mReceiveBytes returned from read()
        int offset = 0;
        int buffCount = 18;

        // Keep listening to the InputStream until an exception occurs
        while (true) {
          /*  try {

               mReceiveBytes = 0;
                BufferedReader r = new BufferedReader(new InputStreamReader(mmInStream));
                mReceiveBytes = r.read(mReceiveBuffer,offset,buffCount);


                    if(mReceiveBuffer[1]==0x01)
                    {
                        Log.d("ConnectedThread","Received Keep Alive Command");
                    }

                    if(mReceiveBuffer[1]==0x02)
                    {
                        Log.d("ConnectedThread","AckOK");
                    }
            } catch (IOException e) {
                break;
            }*/
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

    public char[] read()
    {
        mReceiveBuffer = new char[18];  // mReceiveBuffer store for the stream
        mReceiveBytes = 0; // mReceiveBytes returned from read()
        int offset = 0;
        int buffCount = 18;

        try {
            mReceiveBytes = 0;
            BufferedReader r = new BufferedReader(new InputStreamReader(mmInStream));
            mReceiveBytes = r.read(mReceiveBuffer,offset,buffCount);
            return mReceiveBuffer;
        }catch (IOException e) { return null;}
    }
}