package edu.ucla.csm117.bluetoothattendance;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;

/**
 * Created by matthew on 5/9/15.
 */
public class BluetoothManager {

    BluetoothAdapter adapter;
    Activity activity;

    final int REQUEST_ENABLE_BT = 1;

    public BluetoothManager(Activity activity) {
        // upon construction, get the bluetooth device

        this.adapter = BluetoothAdapter.getDefaultAdapter();
        this.activity = activity;

        // prep bluetooth device
        if (adapter != null && !adapter.isEnabled()) {
            // if adapter isn't enabled, request for it to be turned on
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    public boolean ready() {

        if (adapter == null || !adapter.isEnabled()) {
            // if the bluetooth device is not ready
            // throw out an error dialog

            AlertDialog alertDialog = new AlertDialog.Builder(this.activity).create();
            alertDialog.setTitle("Error");
            alertDialog.setMessage("Could not access Bluetooth on this device.  Please enable Bluetooth.");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();

            return false;
        }

        // device is fine
        return true;
    }
}
