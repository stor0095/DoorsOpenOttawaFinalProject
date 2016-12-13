package com.algonquincollege.wils0751.doorsopenottawa;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import com.squareup.picasso.Picasso;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.SyncStateContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

import static com.algonquincollege.wils0751.doorsopenottawa.R.id.editText;

/**
 * NewBuildingActivity class
 *
 * This class lets a user POST a new building to the server
 *
 * Users enter buildingName, Address, and Description
 * Users are also allowed to upload a photo to the server, however, only the image FileName is being sent, not the
 * actual photo data
 *
 * @author Geemakun Storey (Stor0095)
 */

public class NewBuildingActivity extends Activity {

    // URL to API
    public static final String REST_URI = "https://doors-open-ottawa-hurdleg.mybluemix.net/buildings";
    public static final String PHOT0_URI = "http://doors-open-ottawa-hurdleg.mybluemix.net/buildings/";
    // Global declarations and instance variables
    private EditText mBuildingName;
    private EditText mBuildingAddress;
    private EditText mBuildingDescription;
    public String sBuildingName;
    public String sBuildingAddress;
    public String sBuildingDescription;
    public String buildingImage;
    ImageView buildingImageOverlay;
    // Image request code
    private int PICK_IMAGE_REQUEST = 1;

    // Bitmap to get image from gallery
    private Bitmap bitmap;
    // Storage permission code
    private static final int STORAGE_PERMISSION_CODE = 123;

    //Uri to store the image uri
    private Uri filePath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.newbuildingactivity_detail);
        // Set title
        setTitle(R.string.addNewBuilding);
        // Ask for storage permission
        requestStoragePermission();
        // Reference textviews
        mBuildingName = (EditText) findViewById(R.id.nameOfBuilding);
        mBuildingAddress = (EditText) findViewById(R.id.buildingAddress);
        mBuildingDescription = (EditText) findViewById(R.id.buildingDescription);

        // Reference imageview
        buildingImageOverlay = (ImageView) findViewById(R.id.img_buildingImageOverlay);

        // Allow user to select an image when they tap on the imageview
        buildingImageOverlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
            }

        });

    }
    /**
     * ALLOWING A USER TO SELECT A PHOTO FROM GALLERY
     * AS WELL AS THE PERMISSIONS INVOLVED
     * RETRIEVING THE FILE PATH FROM SELECTED IMAGE
     *
     * The follwing functions: onActivityResult, getPath, requestStoragePermission, onRequestPermissionResult
     * were taken from this tutorial
     *
     * https://www.simplifiedcoding.net/android-upload-image-to-server/
     */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                buildingImageOverlay.setImageBitmap(bitmap);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Method to get the file path from uri
    public String getPath(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        String document_id = cursor.getString(0);
        document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
        cursor.close();

        cursor = getContentResolver().query(
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
        cursor.moveToFirst();
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        cursor.close();

        return path;
    }

    //Requesting permission
    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            return;

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            //If the user has denied the permission previously your code will come to this block
            //Here you can explain why you need this permission
            //Explain here why you need this permission
        }
        //And finally ask for the permission
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
    }

    //This method will be called when the user will tap on allow or deny
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        //Checking the request code of our request
        if (requestCode == STORAGE_PERMISSION_CODE) {

            //If permission is granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Displaying a toast
                Toast.makeText(this, "Permission granted now you can read the storage", Toast.LENGTH_LONG).show();
            } else {
                //Displaying another toast if permission is not granted
                Toast.makeText(this, "Oops you just denied the permission", Toast.LENGTH_LONG).show();
            }
        }
    }

    // Send user back to main page
    public void backButton (View target) {
        finish();
    }

    // Save Button
    public void saveButton (View target){
        // Get text from textviews
        sBuildingName = mBuildingName.getText().toString();
        sBuildingAddress = mBuildingAddress.getText().toString();
        sBuildingDescription = mBuildingDescription.getText().toString();
        //Log.e("TAG", sBuildingName + sBuildingAddress + sBuildingDescription);

        // If one of the textfields left empty, don't post to the server
        if (sBuildingName.isEmpty() || sBuildingAddress.isEmpty() || sBuildingDescription.isEmpty()) {
            Toast.makeText(this, "We need your information before continuing", Toast.LENGTH_LONG).show();
            return;
        } else  {
            // Create a new building from what the user has inputted
            createBuilding(REST_URI);
        //    addPhoto(PHOT0_URI);
            // Reset the textfields
            mBuildingName.setText("");
            mBuildingAddress.setText("");
            mBuildingDescription.setText("");
            Toast.makeText(this, "Building added", Toast.LENGTH_LONG).show();

        }

    }

    // Set up the request package to send a POST to the server after the user inputs data
    private void createBuilding(String uri) {

        sBuildingName = mBuildingName.getText().toString();
        sBuildingAddress = mBuildingAddress.getText().toString();
        sBuildingDescription = mBuildingDescription.getText().toString();

        //getting the actual path of the image
     //   String path = getPath(filePath);
       // String filename=path.substring(path.lastIndexOf("/")+1);

        Building building = new Building();
        building.setBuildingId(0);
        building.setName(sBuildingName);
        building.setImage("image/test.jpg");
        building.setDescription(sBuildingDescription);
        building.setAddress(sBuildingAddress);


        RequestPackage pkg = new RequestPackage();
        pkg.setMethod(HttpMethod.POST);
        pkg.setUri(uri);

        pkg.setParam("name", building.getName());
        pkg.setParam("address", building.getAddress());
        pkg.setParam("description", building.getDescription());
        pkg.setParam("image", building.getImage());

        DoTask postTask = new DoTask();
        postTask.execute(pkg);
    }
    private void addPhoto(String uri) {

        //getting the actual path of the image
        String path = getPath(filePath);
        String filename = path.substring(path.lastIndexOf("/")+1);

        Building building = new Building();
        building.setImage("images/" + filename);
        RequestPackage pkg = new RequestPackage();
        pkg.setMethod(HttpMethod.POST);
        pkg.setUri(uri + 152 + "/image");
        pkg.setParam("image", building.getImage());

        DoTask postTask = new DoTask();
        postTask.execute(pkg);
    }

    private class DoTask extends AsyncTask<RequestPackage, String, String> {
        @Override
        protected void onPreExecute() {
          //  pb.setVisibility(View.VISIBLE);
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
                Toast.makeText(NewBuildingActivity.this, "Web service not available", Toast.LENGTH_LONG).show();
                return;
            }
        }
    }


}
