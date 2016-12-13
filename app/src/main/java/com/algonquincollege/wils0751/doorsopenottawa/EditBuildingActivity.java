package com.algonquincollege.wils0751.doorsopenottawa;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.MenuInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.algonquincollege.wils0751.doorsopenottawa.MainActivity;
import com.algonquincollege.wils0751.doorsopenottawa.model.Building;
import com.algonquincollege.wils0751.doorsopenottawa.parsers.BuildingJSONParser;
import com.algonquincollege.wils0751.doorsopenottawa.HttpManager;
import com.algonquincollege.wils0751.doorsopenottawa.parsers.HttpMethod;
import com.algonquincollege.wils0751.doorsopenottawa.parsers.RequestPackage;

/**
 * EditBuildingActivity Class
 *
 * Allows user to edit/update and PUT to the server
 *
 * This class is activated when a user long clicks on a list item
 * This class gets the buildingId as well as the buildingName from the listview
 * From this, the user can then edit the building address and description for that buildingId
 *
 * @author Geemakun Storey (Stor0095)
 */

public class EditBuildingActivity extends Activity {

    //URL to API
    public static final String REST_URI = "https://doors-open-ottawa-hurdleg.mybluemix.net/buildings/";
    // Instance Variables
    private EditText mbuildingDescription;
    private EditText mBuildingAddress;


    public String sBuildingAddress;
    public String sBuildingDescription;
    public Integer buildingId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edituilding_activity);


        // Reference textviews
        mBuildingAddress = (EditText) findViewById(R.id.editText);
        mbuildingDescription = (EditText) findViewById(R.id.editAddress);


        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String buildingNameFromMainActivity = bundle.getString("Name");
            Integer buildingIDFromMainActivity = bundle.getInt("buildingId");

            buildingId = buildingIDFromMainActivity;
            // Set title
            setTitle(buildingNameFromMainActivity);
            Log.e("BUILDINGID:", String.valueOf(buildingId));
            //Toast.makeText(this, , Toast.LENGTH_LONG).show();

        }
    }

    // Send user back to main page
    public void cancelButton (View target) {
        finish();
    }

    public void editSaveButton (View target) {
        sBuildingAddress = mBuildingAddress.getText().toString();
        sBuildingDescription = mbuildingDescription.getText().toString();

        if (sBuildingAddress.isEmpty() || sBuildingDescription.isEmpty()) {
            Toast.makeText(this, "Edit something before trying to save", Toast.LENGTH_LONG).show();
            return;
        } else  {
            updateBuilding(REST_URI + buildingId);
            mBuildingAddress.setText("");
            mbuildingDescription.setText("");
            Toast.makeText(this, "Building Edited", Toast.LENGTH_LONG).show();

        }
    }
    public void deleteButton (View target){
        Toast.makeText(this, "BUILDING DELETED", Toast.LENGTH_LONG).show();
        deleteBuilding(REST_URI);
        finish();
    }


    private void deleteBuilding(String uri){
        RequestPackage pkg = new RequestPackage();
        pkg.setMethod(HttpMethod.DELETE);
        pkg.setUri(uri + buildingId);
        EditBuildingActivity.DoTask deleteTask = new EditBuildingActivity.DoTask();
        deleteTask.execute(pkg);
    }


    private void updateBuilding(String uri) {

        Building building = new Building();
        building.setBuildingId(0);
        building.setDescription(sBuildingDescription);
        building.setAddress(sBuildingAddress);

        RequestPackage pkg = new RequestPackage();
        pkg.setMethod(HttpMethod.PUT);
        pkg.setUri(uri);
        pkg.setParam("description", building.getDescription());
        pkg.setParam("address", building.getAddress());

        EditBuildingActivity.DoTask postTask = new DoTask();
        postTask.execute(pkg);
    }

    private class DoTask extends AsyncTask<RequestPackage, String, String> {
        @Override
        protected void onPreExecute() {
            //  pb.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(RequestPackage ... params) {
            String content = HttpManager.getData(params[0], "stor0095", "password" );
            return content;
        }

        @Override
        protected void onPostExecute(String result) {
            //  pb.setVisibility(View.INVISIBLE);
            if (result == null) {
                Toast.makeText(EditBuildingActivity.this, "Web service not available", Toast.LENGTH_LONG).show();
                return;
            }
        }
    }
}
