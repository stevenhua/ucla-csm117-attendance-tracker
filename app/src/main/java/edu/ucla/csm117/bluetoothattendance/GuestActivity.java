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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.content.BroadcastReceiver;
import android.content.SharedPreferences;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;


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
    IntentFilter filter;
    boolean finished=false;
    boolean end_early=false;
    // long start_discovering;
    ListView devices_discovered;
    String name;
    String studentid;
    public static final String PREFS_NAME="BTAttendance";
    private static final String PREF_NAME="name";
    private static final String PREF_ID="studentid";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_guest);

        // request bluetooth as soon as guest activity opens up
        btManager = new BluetoothManager(this);

        SharedPreferences prefs=getSharedPreferences(PREFS_NAME,MODE_PRIVATE);
        String restored_name=prefs.getString(PREF_NAME,null);
        String restored_id=prefs.getString(PREF_ID,null);
        if(restored_name!=null) {
            EditText guest_name=(EditText)(findViewById(R.id.name));
            guest_name.setText(restored_name);
        }

        if(restored_id!=null){
            EditText guest_id=(EditText)(findViewById(R.id.studentid));
            guest_id.setText(restored_id);
        }


    }

    @Override
    public void onDestroy()
    {
        try {
            unregisterReceiver(mReceiver);
        }catch(IllegalArgumentException e){

        }

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
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                enable_list();
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(device_list.contains(device)==false) {
                    device_list.add(device);

                    // Add the name and address to an array adapter to show in a ListView
                    //mainly used for testing, can remove later
                    BTadapter.add(device.getName() + "\n" + device.getAddress());
                    BTadapter.notifyDataSetChanged();
                }


            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action))
            {


            }
            else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action) && finished==false)
            {
                finished=true;

                discovery_finished();
                //process_devices();
            }


        }
    };

    public void discovery_finished(){
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
            alertDialog.show();
            return;
        }

        if(device_list.isEmpty()){
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle("Error");
            alertDialog.setMessage("No Devices Found");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
            return;
        }
        if(end_early==false) {
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle("Alert");
            alertDialog.setMessage("Finished Searching Please Select Host");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
        }
    }



    public void enable_list(){
        ListView lv=(ListView)findViewById(R.id.devices_discovered);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position!=0) {
                    end_early=true;
                    process_device(position-1);
                }
            }
        });

    }

    public void process_device(int list_pos){
        //test each discovered device until we find host
        // for (int i = 0; i < device_list.size(); i++) {
        btManager.name=name;
        btManager.name+=" ";
        btManager.studentid=studentid;
        boolean check= btManager.client(device_list.get(list_pos));

        if (check == true) {
            //we connected to a host, we can now send data
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle("Success");
            alertDialog.setMessage("You have been signed in");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();

            return;
        }
        // }

        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Error");
        alertDialog.setMessage("Could not connect to host");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();

    }



    public void findHost(View view) {

        name = ((EditText)(findViewById(R.id.name))).getText().toString();
        studentid=((EditText)(findViewById(R.id.studentid))).getText().toString();
        //&& studentid.length()==9


        if (!name.equals("") &&btManager.ready()) {

            SharedPreferences.Editor editor=getSharedPreferences(PREFS_NAME,MODE_PRIVATE).edit();
            editor.putString(PREF_NAME,name);
            editor.putString(PREF_ID,studentid);
            editor.commit();

            setContentView(R.layout.guest_search);
            BTadapter=new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                    listDevices);

            final ListView listView = (ListView) findViewById(R.id.devices_discovered);
            listView.setAdapter(BTadapter);

            listDevices.add("Searching for host...");
            BTadapter.notifyDataSetChanged();

            filter = new IntentFilter();
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
                alertDialog.show();
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
            }/* else if (studentid.length() != 9) {

                 AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                 alertDialog.setTitle("Error");
                 alertDialog.setMessage("Student id must be 9 digits. Please input a valid student id");
                 alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                         new DialogInterface.OnClickListener() {
                             public void onClick(DialogInterface dialog, int which) {
                                 dialog.dismiss();
                             }
                         });
                         alertDialog.show();

             }
            */
        }
    }

}

