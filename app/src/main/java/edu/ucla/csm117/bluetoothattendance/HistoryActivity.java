package edu.ucla.csm117.bluetoothattendance;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.lang.reflect.Array;
import java.security.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by matthew on 5/25/15.
 */
public class HistoryActivity extends ActionBarActivity {

    private ArrayList<String> listEvents = new ArrayList<String>();
    private ArrayAdapter<String> HistoryAdapter;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history_list);

        HistoryAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                listEvents);

        final ListView listView = (ListView) findViewById(R.id.eventsListView);
        listView.setAdapter(HistoryAdapter);

        listEvents.add("Past Event Rosters");

        // load in the saved past rosters from db
        HistoryDatabase rosters = new HistoryDatabase(this);
        SQLiteDatabase db = rosters.getReadableDatabase();

        String[] columns = {"Timestamp", "Person", "EventHost", "EventTime"};

        String[] event = {"EventTime","EventHost"};

        Cursor event_times = db.query(true, "People", event, null, null, null, null, null, null);

        Cursor results = db.query("People", columns, null, null, "Timestamp", null, null);


        if (event_times.moveToFirst()) {
            do {
                String eventTime = event_times.getString(event_times.getColumnIndex("EventTime"));
                String eventHost = event_times.getString(event_times.getColumnIndex("EventHost"));
                if (results.moveToFirst() ){
                    listEvents.add("Event:\n  Host: "+eventHost+"\n  Time: "+eventTime);
                    String[] columnNames = results.getColumnNames();

                    do {
                        String row = "";
                        if (results.getString(results.getColumnIndex("EventTime")).equals(eventTime) && results.getString(results.getColumnIndex("EventHost")).equals(eventHost)) {
                            // same event time and host so include in this section
                            for (String name: columnNames) {
                                if (name.equals("Person") || name.equals("Timestamp"))
                                    if (name.equals("Timestamp"))
                                        row += String.format("Time: %s\n", results.getString(results.getColumnIndex(name)));
                                    else
                                        row += String.format("%s", results.getString(results.getColumnIndex(name)));
                        }
                        }
                        if (!row.equals(""))
                            listEvents.add(row);
                    } while (results.moveToNext());
                    if (!event_times.isLast())
                        listEvents.add("---");
                }
            } while (event_times.moveToNext());
            }

        HistoryAdapter.notifyDataSetChanged();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_history, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_clear) {
            HistoryDatabase rosters = new HistoryDatabase(this);
            SQLiteDatabase db = rosters.getReadableDatabase();

            db.delete("People", null, null);

            db.close();

            listEvents.clear();

            listEvents.add("Past Event Rosters");

            HistoryAdapter.notifyDataSetChanged();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
