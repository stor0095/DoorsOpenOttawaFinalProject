package com.algonquincollege.wils0751.doorsopenottawa;

import android.app.DialogFragment;
import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.SearchView;
import android.app.SearchableInfo;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.algonquincollege.wils0751.doorsopenottawa.model.Building;
import com.algonquincollege.wils0751.doorsopenottawa.HttpManager;
import com.algonquincollege.wils0751.doorsopenottawa.parsers.HttpMethod;
import com.algonquincollege.wils0751.doorsopenottawa.parsers.RequestPackage;
import com.algonquincollege.wils0751.doorsopenottawa.parsers.BuildingJSONParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;



/**
 * Main activity class handles all the network requests and listview data that
 * is displayed on the main view
 *
 * @author Geemakun Storey (Stor0095)
 */
public class MainActivity extends ListActivity implements SwipeRefreshLayout.OnRefreshListener, AdapterView.OnItemClickListener /*implements AdapterView.OnItemClickListener*/ {

    // URL to my RESTful API Service hosted on my Bluemix account.
    public static final String IMAGES_BASE_URL = "https://doors-open-ottawa-hurdleg.mybluemix.net/";
    public static final String REST_URI = "https://doors-open-ottawa-hurdleg.mybluemix.net/buildings";

    // Global declarations
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ListView mListView;
    private BuildingAdapter mAdapter;
    private ProgressBar pb;
    private List<MyTask> tasks;
    private List<Building> buildingList;
    private EditText inputSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up Listview from user and data interaction
        mListView = (ListView) findViewById(android.R.id.list);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
        // Set up swiperefresh for user interaction
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        // Progress bar for loading
        pb = (ProgressBar) findViewById(R.id.progressBar);
        pb.setVisibility(View.INVISIBLE);
        // Reference input search
        inputSearch = (EditText) findViewById(R.id.inputSearch);


        tasks = new ArrayList<>();
        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        // ON ITEM CLICK
        // Send the following data to the detail activity
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Building theSelectedBuilding = buildingList.get(position);
                Intent intent = new Intent(getApplicationContext(), DetailActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("Name", theSelectedBuilding.getName());
                intent.putExtra("Description", theSelectedBuilding.getDescription());
                intent.putExtra("Address", theSelectedBuilding.getAddress());
                intent.putExtra("Date", theSelectedBuilding.getDate());

                startActivity(intent);
            }
        });


        // ON ITEM LONG CLICK
        // When a user longclicks on a list item to edit, send the following
        // data to the edit activity
        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id){
                Building theSelectedBuilding = buildingList.get(position);
                Intent intent = new Intent(getApplicationContext(), EditBuildingActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("Name", theSelectedBuilding.getName());
//                intent.putExtra("Description", theSelectedBuilding.getDescription());
//                intent.putExtra("Address", theSelectedBuilding.getAddress());
                intent.putExtra("buildingId", theSelectedBuilding.getBuildingId());

                startActivity(intent);

                return false;
            }

        });
        if (isOnline()) {
            // Request data to fill listview
            requestData(REST_URI);
        } else {
            // Error if connection fails
            Toast.makeText(this, "Network isn't available", Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

    // Add a building by pressing the add icon, takes you to new building activity
        if (item.getItemId() == R.id.addBuilding) {
            if (isOnline()) {
                startActivity(new Intent(this, NewBuildingActivity.class));
            } else {
                Toast.makeText(this, "No connection to add a building at this time.", Toast.LENGTH_LONG).show();
            }
            return true;
        }
        // Sorting the list alphabectically
        // Code taken from course modules
        // https://algonquin.instructure.com/courses/501991/pages/sorting-list-data?module_item_id=10367395
        if ( item.isCheckable() ) {
            // Leave if the list is null
            if ( buildingList == null ) {
                return true;
            }
            switch( item.getItemId() ) {
                // Sort buildings from A-Z
                case R.id.action_sort_name_asc:
                    Collections.sort( buildingList, new Comparator<Building>() {
                        @Override
                        public int compare( Building lhs, Building rhs ) {
                            return lhs.getName().compareTo( rhs.getName() );
                        }
                    });
                    break;
                // Sort building from Z-A
                case R.id.action_sort_name_dsc:
                    Collections.sort( buildingList, Collections.reverseOrder(new Comparator<Building>() {
                        @Override
                        public int compare( Building lhs, Building rhs ) {
                            return lhs.getName().compareTo( rhs.getName() );
                        }
                    }));
                    break;
            }
            // Remember which sort option the user picked
            item.setChecked( true );
            // Re-fresh the list to show the sort order
            ((ArrayAdapter)getListAdapter()).notifyDataSetChanged();
        } // END if item.isChecked()

        return true;

    }

    private void requestData(String uri) {

        // Send a new Http GET RequestPackage to retrieve data from server
        RequestPackage getPackage = new RequestPackage();
        getPackage.setMethod( HttpMethod.GET );
        getPackage.setUri(uri);
        MyTask getTask = new MyTask();
        getTask.execute( getPackage );

    }

    protected void updateDisplay() {
        //Use BuildingAdapter to display data
        final BuildingAdapter adapter = new BuildingAdapter(this, R.layout.item_building, buildingList);
        setListAdapter(adapter);

        // Set listener to search input field
        inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                // Filter the list based on what the user types in
                adapter.filter(cs.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                          int arg3) {
                // TODO Auto-generated method stub
            }
            @Override
            public void afterTextChanged(Editable arg0) {
                // TODO Auto-generated method stub
            }
        });

    }



    // Check whether a user is connected to a wifi or cellular network
    protected boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        } else {
            return false;
        }
    }
    // Swipe down to refresh
    @Override
    public void onRefresh() {
        // If user has a connection, make another request to refresh
        if (isOnline()) {
            requestData(REST_URI);
            // End refreshing animation
            mSwipeRefreshLayout.setRefreshing(false);
        } else {
            // If no connection, display error, also end refreshing
            Toast.makeText(this, "Network isn't available", Toast.LENGTH_LONG).show();
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

    }


    private class MyTask extends AsyncTask<RequestPackage, String, List<Building>> {

        @Override
        protected void onPreExecute() {
            if (tasks.size() == 0) {
                pb.setVisibility(View.VISIBLE);
            }
            tasks.add(this);
        }
        @Override
        protected List<Building> doInBackground(RequestPackage... params) {
        // When getting data, send my username and password to be authenticated to retrieve data
            String content = HttpManager.getData(params[0], "stor0095", "password");
            // Reponse that will fill the building list view
            buildingList = BuildingJSONParser.parseFeed(content);
            return buildingList;
        }

        @Override
        protected void onPostExecute(List<Building> result) {
            tasks.remove(this);
            if (tasks.size() == 0) {
                pb.setVisibility(View.INVISIBLE);
            }
            if (result == null) {
                Toast.makeText(MainActivity.this, "Web service not available", Toast.LENGTH_LONG).show();
                return;
            }
            // If no netowrk error, let buildingList = result, now update the list display
            buildingList = result;
            updateDisplay();

        }
    }
    private class DoTask extends AsyncTask<RequestPackage, String, String> {
        @Override
        protected void onPreExecute() {
              pb.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(RequestPackage ... params) {
            //String content = HttpManager.getData(String.valueOf(params[0]),"stor0095", "password" );
            //HttpManager.getData(params[0]);
            String content = HttpManager.getData(params[0], "stor0095", "password" );
            return content;
        }

        @Override
        protected void onPostExecute(String result) {
            //  pb.setVisibility(View.INVISIBLE);
            if (result == null) {
                Toast.makeText(MainActivity.this, "Web service not available", Toast.LENGTH_LONG).show();
                return;
            }
        }
    }

}
