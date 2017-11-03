package com.example.andy.mapeandlocation;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

import static android.Manifest.*;
import static android.Manifest.permission.*;
import static android.os.Build.VERSION_CODES.M;

public class MapsMain extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Marker mCurrLocationMarker;
    Location mLastLocation;
    private LocationRequest mLocationRequest;
    private EditText userInputField;
    private Button mButton;
    private static final String TAG = "MapsMain";




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_maps_main);
        initializeWidgets();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }
//initialises the button and edittext
    private void initializeWidgets() {
mButton=findViewById(R.id.mb);
userInputField=findViewById(R.id.where);
mButton.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        String input=userInputField.getText().toString().trim().toLowerCase();
        if(!TextUtils.isEmpty(input)){
            geolocate(input);
        }
    }
});
    }
//geolocates user inputs to lntlg object
    private void geolocate(String input) {
        Log.d(TAG, "geolocate: Trying to gelocate users query|:"+input);
        Geocoder geocoder = new Geocoder(this);
        try {
            List<Address> list = geocoder.getFromLocationName(input, 1);
            Address address = list.get(0);
            Log.d(TAG, "geolocate: "+address.getLocality());
            Toast.makeText(this,address.getLocality(), Toast.LENGTH_SHORT).show();
            Double lat = address.getLatitude();
            Double lng = address.getLongitude();
            LatLng ll=new LatLng(lat,lng);

            //move map camera
            mMap.moveCamera(CameraUpdateFactory.newLatLng(ll));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(ll);
            markerOptions.title("your position");
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
            mCurrLocationMarker = mMap.addMarker(markerOptions);



        } catch (IOException e) {
e.printStackTrace();
            Log.d(TAG, "geolocate: IOEXCEPTION:"+e.getMessage());
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
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        //initialising google play services.
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
           if(ContextCompat.checkSelfPermission(this, permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){

                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        }
        else{
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
            
        }

        /* Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        */
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }



    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
       LocationRequest locationRequest1=new LocationRequest();
        locationRequest1.setPriority(com.google.android.gms.location.LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, new com.google.android.gms.location.LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    mLastLocation = location;
                    if (mCurrLocationMarker != null) {
                        mCurrLocationMarker.remove();
                    }

                    //Place current location marker
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(latLng);
                    markerOptions.title("Current Position");
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                    mCurrLocationMarker = mMap.addMarker(markerOptions);

                    //move map camera
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(11));

                    //stop location updates
                    if (mGoogleApiClient != null) {

                        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
                    }
                }
            });
        }
    }

    public static final int MY_PERMISSIONS_REQUESTS_LOCATION = 90;
        public Boolean CheckLocationPermission(){
            if (ContextCompat.checkSelfPermission(this, permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
                // Asking the user if the explanation is needed.
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.




                } else {
                    String[] permissions={permission.ACCESS_FINE_LOCATION};
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(this,
                            permissions,
                            MY_PERMISSIONS_REQUESTS_LOCATION);
                }
                return false;
            } else {
                return true;
            }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUESTS_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // Permission was granted.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }

                } else {

                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }


        }
    }




    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
