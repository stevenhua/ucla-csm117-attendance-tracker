package edu.ucla.csm117.bluetoothattendance;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.os.Handler;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import android.os.Message;
import android.content.ContentValues;
/**
 * Created by matthew on 5/6/15.
 */
public class HostActivity extends ActionBarActivity{

    BluetoothManager btManager;
    ArrayList<String> listItems = new ArrayList<String>();
    ArrayAdapter<String> adapter;
    Handler message_handler;
    HistoryDatabase registrations;
    SQLiteDatabase db;
    ArrayList<String> used_devices=new ArrayList<String>();
    Boolean hosting_screen=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_host);

        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        final String eventDate = dateFormat.format(date);

        final EditText hostname_widget = (EditText)findViewById(R.id.hostname);

        registrations = new HistoryDatabase(this);
        db = registrations.getWritableDatabase();

        message_handler=new Handler() {
            @Override
            public void handleMessage(Message msg){
                switch(msg.what){
                    case 1:
                        String data=msg.obj.toString();
                        int start_index=data.indexOf("Address: ");
                        String data_address=data.substring(start_index);
                        //if(!used_devices.contains(data_address)) {
                            used_devices.add(data_address);
                            listItems.add(data);
                            adapter.notifyDataSetChanged();
                            // add items to databse
                            ContentValues values = new ContentValues();
                            values.put("Person", data);
                            values.put("EventHost", hostname_widget.getText().toString());
                            values.put("EventTime", eventDate);
                            db.insert("People", null, values);
                            //  break
                      //  }
                }
                super.handleMessage(msg);
            }
        };

        // request bluetooth as soon as host activity opens up
        btManager = new BluetoothManager(this,message_handler);

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

    @Override
    public void onBackPressed(){
        if(hosting_screen==true){
            AlertDialog.Builder builder=new AlertDialog.Builder(this);
            builder.setTitle("Really Exit?");
            builder.setMessage("Exiting will end the hosting period");
            builder.setPositiveButton(android.R.string.no, null);
            builder.setNeutralButton("Rehost",new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    btManager.enable_discovery();
                }
            });
            builder.setNegativeButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    hosting_screen = false;
                    HostActivity.super.onBackPressed();
                }
            });
            builder.show();
        }else{
            super.onBackPressed();
        }

    }


    public void onStartHosting(View view) {

        String hostName = ((EditText)(findViewById(R.id.hostname))).getText().toString();
        hosting_screen=true;

        if(!hostName.equals("") && btManager.ready() && btManager.setName(hostName) && btManager.server()) {
            // we are hosting successfully

            setContentView(R.layout.hosting_host);

            adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1,
                    listItems);

            final ListView listView = (ListView) findViewById(R.id.attendanceListView);
            listView.setAdapter(adapter);

            listItems.add("Waiting for attendees....");
            adapter.notifyDataSetChanged();

            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle("Success!");
            alertDialog.setMessage("Host \"" + hostName + "\" is now waiting for attendees to register");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
            //btManager.keep_running=false;

        } else {
            // failed to host
            // throw up an error message
            hosting_screen=false;
            if (hostName.equals("")) {
                AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                alertDialog.setTitle("Error");
                alertDialog.setMessage("Host name cannot be empty.  Please input a valid host name.");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            } else {
                AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                alertDialog.setTitle("Error");
                alertDialog.setMessage("Failed to setup the host.  Please try again.");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            }
        }
    }

}

