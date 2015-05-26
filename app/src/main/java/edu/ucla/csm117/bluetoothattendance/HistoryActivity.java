package edu.ucla.csm117.bluetoothattendance;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by matthew on 5/25/15.
 */
public class HistoryActivity extends ActionBarActivity {

    private ArrayList<String> listEvents = new ArrayList<String>();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history_list);

        ArrayAdapter<String> HistoryAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                listEvents);

        final ListView listView = (ListView) findViewById(R.id.eventsListView);
        listView.setAdapter(HistoryAdapter);

        listEvents.add("Past Event Rosters");
        HistoryAdapter.notifyDataSetChanged();

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

}
