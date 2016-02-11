package com.example.levi.watchdog;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Levi on 05/02/2016.
 */
public class PairedDevicesAdapter extends ArrayAdapter<BluetoothDevice> {

    private static final String tag = "CountryArrayAdapter";


    private Context context;

    private ImageView deviceIcon;
    private TextView deviceName;
    private List<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();

    public PairedDevicesAdapter(Context context, int resource, ArrayList<BluetoothDevice> objects) {
        super(context, resource, objects);

        this.context = context;
        this.devices = objects;
    }

    public int getCount() {
        return this.devices.size();
    }


    public BluetoothDevice getItem(int index) {
        return this.devices.get(index);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        if (row == null) {
            // ROW INFLATION
            LayoutInflater inflater = (LayoutInflater) this.getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.devices_list_item, parent, false);

        }

        // Get item
        BluetoothDevice device = getItem(position);

        // Get reference to ImageView
        deviceIcon = (ImageView) row.findViewById(R.id.deviceIcon);

        // Get reference to TextView - country_name
        deviceName = (TextView) row.findViewById(R.id.deviceName);


        //Set country name
        deviceName.setText(device.getName());

        // Set country icon usign File path
       // String imgFilePath = ASSETS_DIR + country.resourceId;

           /* Bitmap bitmap = BitmapFactory.decodeStream(this.context.getResources().getAssets()
                    .open(imgFilePath));*/
        deviceIcon.setImageResource(R.drawable.bluetooth);



        return row;
    }

}
