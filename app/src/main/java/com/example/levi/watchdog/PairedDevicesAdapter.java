package com.example.levi.watchdog;

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
public class PairedDevicesAdapter extends ArrayAdapter<DeviceItem> {

    private static final String tag = "CountryArrayAdapter";
    private static final String ASSETS_DIR = "images/";
    private Context context;

    private ImageView deviceIcon;
    private TextView deviceName;
    private List<DeviceItem> devices = new ArrayList<DeviceItem>();

    public PairedDevicesAdapter(Context context, int resource, ArrayList<DeviceItem> objects) {
        super(context, resource, objects);

        this.context = context;
        this.devices = objects;
    }

    public int getCount() {
        return this.devices.size();
    }


    public DeviceItem getItem(int index) {
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
        DeviceItem device = getItem(position);

        // Get reference to ImageView
        deviceIcon = (ImageView) row.findViewById(R.id.deviceIcon);

        // Get reference to TextView - country_name
        deviceName = (TextView) row.findViewById(R.id.deviceName);


        //Set country name
        deviceName.setText(device.getDeviceName());

        // Set country icon usign File path
       // String imgFilePath = ASSETS_DIR + country.resourceId;

           /* Bitmap bitmap = BitmapFactory.decodeStream(this.context.getResources().getAssets()
                    .open(imgFilePath));*/
            deviceIcon.setImageResource(R.drawable.ic_drawer);


        return row;
    }

}
