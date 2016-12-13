package com.algonquincollege.wils0751.doorsopenottawa;

import android.app.Activity;
import android.app.ListActivity;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.location.Geocoder;
import android.location.Address;
import android.widget.Toast;
import android.widget.EditText;
import android.widget.TextView;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.android.gms.maps.MapFragment;
/**
 * Detail activity class handles all the items displayed in the detail activity
 *
 * This class retrives the selected building information from the server,
 * populating the detail view with Name, Description, Address/Mapview, and hours open
 *
 * @author Geemakun Storey (Stor0095)
 */

public class DetailActivity extends FragmentActivity implements OnMapReadyCallback {

    private TextView buildingName;
    private TextView buildingDescription;
    private TextView buildingHours;
    private String newStringBuildingAddress;
    private GoogleMap mMap;
    private Geocoder mGeocoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        // Instantiate geocode
        mGeocoder = new Geocoder(this);

        buildingName = (TextView) findViewById(R.id.textViewName);
        buildingDescription = (TextView) findViewById(R.id.textViewDescription);
        buildingHours = (TextView) findViewById(R.id.textviewDate);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String buildingNameFromMainActivity = bundle.getString("Name");
            String buildingDescriptionFromMainActivity = bundle.getString("Description");
            String buildingDateFromMainActivity = bundle.getString("Date");

            newStringBuildingAddress = bundle.getString("Address");

            buildingName.setText(buildingNameFromMainActivity);
            buildingDescription.setText(buildingDescriptionFromMainActivity);
            buildingHours.setText(buildingDateFromMainActivity);
        }

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Pin location
        pin(newStringBuildingAddress);
    }

    /**
     * Locate and pin locationName to the map.
     */
    private void pin(String locationName) {
        try {
            Address address = mGeocoder.getFromLocationName(locationName, 1).get(0);
            LatLng ll = new LatLng(address.getLatitude(), address.getLongitude());
            // Set zoom level
            float zoomLevel = (float) 16.0; //This goes up to 21
            mMap.addMarker(new MarkerOptions().position(ll).title(locationName));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ll, zoomLevel));
          //  Toast.makeText(this, "Pinned: " + locationName, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Not found: " + locationName, Toast.LENGTH_SHORT).show();
        }
    }
}



