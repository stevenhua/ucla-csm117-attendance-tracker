package edu.ucla.csm117.bluetoothattendance;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.content.BroadcastReceiver;
import android.widget.Toast;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;
//import android.content.Context;
//import java.lang.String;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.Context;
import android.widget.ArrayAdapter;


/**
 * Created by matthew on 5/6/15.
 */

public class GuestActivity extends ActionBarActivity {
    BluetoothManager btManager;
    ArrayList<BluetoothDevice> device_list=new ArrayList<BluetoothDevice>();
    ArrayList<String> listDevices = new ArrayList<String>();
    boolean discovered=false;
    public final String TAG="GuestActivity";
    ArrayAdapter<String> BTadapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_guest);

        // request bluetooth as soon as guest activity opens up
        btManager = new BluetoothManager(this);
    }

    @Override
    public void onDestroy()
    {
        unregisterReceiver(mReceiver);
        super.onDestroy();
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private final BroadcastReceiver mReceiver=new BroadcastReceiver()
     {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            //Log.d(TAG, "onReceive");
            Toast.makeText(getApplicationContext(),"onReceive",Toast.LENGTH_LONG).show();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
              //  Log.d(TAG,"ACTION_FOUND");
                //Toast.makeText(getApplicationContext(),"action_found",Toast.LENGTH_LONG).show();
               BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                device_list.add(device);
                BTadapter.add(device.getName() + "\n" + device.getAddress());
                BTadapter.notifyDataSetChanged();


                // Add the name and address to an array adapter to show in a ListView
                // adapter.add(device.getName() + "\n" + device.getAddress());
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action))
            {
               Toast.makeText(getApplicationContext(),"discovery start",Toast.LENGTH_LONG).show();
              //  Log.d(TAG, "DISCOVERY_STARTED");
            }
            else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
            {
                Toast.makeText(getApplicationContext(),"discovery finished",Toast.LENGTH_LONG).show();
                //Log.d(TAG, "DISCOVERY FINISHED");
                process_devices();
            }


        }
    };

    public void process_devices(){
        if(discovered==false)
        {
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle("Error");
            alertDialog.setMessage("Failure to start Discovering");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
        }

        boolean check=false;
            for (int i = 0; i < device_list.size(); i++) {
                btManager.client(device_list.get(i));
                check=btManager.valid_socket;
                if (check == true) {
                    //we connected to a host, we can now send data
                    AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                    alertDialog.setTitle("Alert");
                    alertDialog.setMessage("We have connected to host");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                    break;
                }
            }
        if(check==false)
        {
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle("Error");
            alertDialog.setMessage("Host was not found");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
        }
    }



    public void findHost(View view) {

        final String name = ((EditText)(findViewById(R.id.name))).getText().toString();
        //final String studentid=((EditText)(findViewById(R.id.name))).getText().toString();
        //&&studentid.length()==9
        if (!name.equals("") && btManager.ready()) {
         if(true)
         {

             setContentView(R.layout.guest_search);
             BTadapter=new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                     listDevices);

             final ListView listView = (ListView) findViewById(R.id.devices_discovered);
             listView.setAdapter(BTadapter);

             listDevices.add("Waiting for attendees....");
             BTadapter.notifyDataSetChanged();


            IntentFilter filter = new IntentFilter();
            filter.addAction(BluetoothDevice.ACTION_FOUND);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            this.registerReceiver(mReceiver, filter);


            if (btManager.adapter.isDiscovering()) {
                //already discovering
                btManager.adapter.cancelDiscovery();
            }


                try {
                   discovered= btManager.adapter.startDiscovery();
                } catch(Exception e)
                {

                    AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                    alertDialog.setTitle("Error");
                    alertDialog.setMessage("error discovering");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                }


        }
        else {
             if (name.equals("")) {
                 AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                 alertDialog.setTitle("Error");
                 alertDialog.setMessage("Name cannot be empty. Please input a valid name");
                 alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                         new DialogInterface.OnClickListener() {
                             public void onClick(DialogInterface dialog, int which) {
                                 dialog.dismiss();
                             }
                         });
                 alertDialog.show();
             } /*else if (studentid.length() != 9) {

                 AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                 alertDialog.setTitle("Error");
                 alertDialog.setMessage("Student id must be 9 digits. Please input a valid student id");
                 alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                         new DialogInterface.OnClickListener() {
                             public void onClick(DialogInterface dialog, int which) {
                                 dialog.dismiss();
                             }
                         });

             }*/

            }
        }
    }
}