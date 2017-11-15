package com.by_mohammadelsayed.pinpoper.googlemaps5;

import android.*;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.CustomCap;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnPolygonClickListener,
        GoogleMap.OnPolylineClickListener, LocationListener {

    private GoogleMap mMap;
    private Marker mMarker;
    private LatLng mLatLng;
    private WifiManager wifiManager;
    private AlertDialog dialog;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private static final PatternItem DOT = new Dot();
    private static final PatternItem GAP = new Gap(5);
    private Circle circle;
    /*
    to make patter in the polyline we have to make dots and gaps
    then create a list containing the complete stroke(only one iteration).
     */

    private static final List<PatternItem> PATTERN_POLYLINE_DOTTED = Arrays.asList(GAP, DOT);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int permission = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        }



        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
            mBuilder.setTitle("Turn Wifi On");
            mBuilder.setMessage("This app requires Wifi to be on");
            mBuilder.setIcon(R.mipmap.wifi_icon);
            mBuilder.setCancelable(false);
            mBuilder.setPositiveButton("Turn On", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    wifiManager.setWifiEnabled(true);
                    dialog.dismiss();
                }
            });
            mBuilder.setNegativeButton("Close App", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialog.dismiss();
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    finish();
                }
            });
            dialog = mBuilder.create();
            dialog.show();
        }

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        createLocationRequest();
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        Log.d("Armstring", "onCreate 1");
        final PendingResult<LocationSettingsResult> result =//this is a pending intent that is triggered whenever LocationServices
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, // is used.
                        builder.build());

        Log.d("Armstring", "onCreate 2");
        result.setResultCallback(
                new ResultCallback<LocationSettingsResult>() {
                    @Override
                    public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                        final Status status = locationSettingsResult.getStatus();
                        switch (status.getStatusCode()) {
                            case LocationSettingsStatusCodes.SUCCESS:
                                Log.d("Armstring", "Satisfied");
                                break;
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                Log.d("Armstring", "Not Satisfied");
                                try {
                                    status.startResolutionForResult(MainActivity.this, 100);//this method gives the user dialog
                                } catch (IntentSender.SendIntentException e) {
                                    e.printStackTrace();
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                Log.d("Armstring", "Not Satisfied can't be fixed");
                                break;
                        }
                    }
                }
        );

        Log.d("Armstring", "onCreate 3");
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.fragmentId);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        int permission = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        }
        mMap.setMyLocationEnabled(true);
        googleMap.setOnPolylineClickListener(this);
        googleMap.setOnPolygonClickListener(this);

        Polyline polyline1 = googleMap.addPolyline(new PolylineOptions()
                .clickable(true)
                .add(
                        new LatLng(-35.016, 143.321),
                        new LatLng(-34.747, 145.592),
                        new LatLng(-34.364, 147.891),
                        new LatLng(-33.501, 150.217),
                        new LatLng(-32.306, 149.248),
                        new LatLng(-32.491, 147.309)));

        polyline1.setStartCap(
                new CustomCap(
                        BitmapDescriptorFactory.fromResource(R.mipmap.wifi_icon), 10));
        polyline1.setEndCap(new RoundCap());
        polyline1.setWidth(5);
        polyline1.setColor(0xff66AA33);
        polyline1.setJointType(JointType.ROUND);

        polyline1.setPattern(PATTERN_POLYLINE_DOTTED);
        circle = mMap.addCircle(new CircleOptions()
                .center(new LatLng(-33.87365, 151.20689))
                .radius(250)
                .strokeColor(Color.RED));
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(3000);
        mLocationRequest.setFastestInterval(2000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Log.d("Armstring", "onConnected");
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onPolygonClick(Polygon polygon) {

    }

    @Override
    public void onPolylineClick(Polyline polyline) {
        Log.d("Armstring", "onPolylineClickListener");
        /*
        in this method we use method getTag() of the polyline so that we can know which line is the clicked line.
         */
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("Armstring", "onLocationChanged");
        if(circle != null){
            mLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            circle.setCenter(mLatLng);
            Log.d("Armstring", "onLocationChanged inside if statement");
        }
    }
}


/*
<meta-data
    android:name="com.google.android.gms.version"
    android:value="@integer/google_play_services_version" />

    this element is to embed the version of Google Play Services that your app was compiled with.
 */

/*
<meta-data
  android:name="com.google.android.geo.API_KEY"
  android:value="@string/google_maps_key" />

  this element is to specify the API Key given from console.developers.google.com
 */