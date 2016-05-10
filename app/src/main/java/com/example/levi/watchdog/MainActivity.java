package com.example.levi.watchdog;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.logging.Handler;

public class MainActivity extends AppCompatActivity {
    private TextView tvMessage;
    private Button btStart;
    private Button btPause;
    private Button btStop;

    private static BluetoothAdapter BTAdapter = BluetoothAdapter.getDefaultAdapter();
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private static BluetoothDevice mConnectedDevice;
    private static ConnectThread connectionService;
    private static ConnectedThread connectionManager;

    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;

    private static byte[] Cmd = {0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00,0X00};
    SharedPreferences preferences;

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        tvMessage = (TextView)findViewById(R.id.tvMessage);
        btStart = (Button)findViewById(R.id.button_start_watch);
        btPause = (Button)findViewById(R.id.button_pause_watch);
        btStop = (Button)findViewById(R.id.button_stop_watch);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    public void bluetoothActivityStart(View view)
    {
        Intent intent = new Intent(this, BluetoothActivity.class);
        startActivity(intent);

    }


    public void startWatch(View view)
    {
        if(connectionManager!=null)
        {
            String password = preferences.getString(getString(R.string.password_pref_key),getString(R.string.pref_setting_password_default));

            Cmd[1] = 0X03;
            Cmd[2] = (byte)( Integer.parseInt(password)>>8);
            Cmd[3] = (byte)( Integer.parseInt(password));

           connectionManager.write(Cmd);
            btPause.setEnabled(true);
            btStop.setEnabled(true);
            btStart.setEnabled(false);

        }
    }

    public void pauseWatch(View view)
    {
        if(connectionManager!=null)
        {
            String password = preferences.getString(getString(R.string.password_pref_key),getString(R.string.pref_setting_password_default));

            Cmd[1] = 0X04;
            Cmd[2] = (byte)( Integer.parseInt(password)>>8);
            Cmd[3] = (byte)( Integer.parseInt(password));

            connectionManager.write(Cmd);
            btPause.setEnabled(false);
            btStart.setEnabled(true);

        }
    }

    public void stopWatch(View view)
    {
        if(connectionManager!=null)
        {
            String password = preferences.getString(getString(R.string.password_pref_key),getString(R.string.pref_setting_password_default));

            Cmd[1] = 0X05;
            Cmd[2] = (byte)( Integer.parseInt(password)>>8);
            Cmd[3] = (byte)( Integer.parseInt(password));

            connectionManager.write(Cmd);
            btStart.setEnabled(true);
            btPause.setEnabled(false);
            btStop.setEnabled(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mConnectedDevice = getIntent().getParcelableExtra("connectedDeviceAddress");

        if(mConnectedDevice!=null)
        {
            tvMessage.setText("Connected to Device: "+mConnectedDevice.getName());
            connectionService = new ConnectThread(mConnectedDevice,BTAdapter);
            if(connectionService!=null)
            {
                connectionManager = connectionService.getManageConnection();
                btStart.setEnabled(true);
            }
        }

    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpBluetooth();

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

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement


        if (id == R.id.action_settings) {
            /*Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);*/
            Intent intent = new Intent(this,SettingsActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
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


    private void setUpBluetooth()
    {

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

    }

}
