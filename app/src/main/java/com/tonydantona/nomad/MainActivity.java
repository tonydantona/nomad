package com.tonydantona.nomad;

import android.app.FragmentManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.google.android.gms.common.ConnectionResult;

import org.xmlpull.v1.XmlPullParserException;

public class MainActivity extends AppCompatActivity implements LocationBeacon.ILocationServices, BluetoothServices.IDestinationServices {

    private LocationBeacon mLocationBeacon;
    private Location mCurrentLocation;
    private Destination mDestination;

    private BluetoothServices mBluetoothServices;

    private TextView mLatitudeText;
    private TextView mLongitudeText;

    private MapView mMapView;

    private Handler mHandler = new Handler();
    final Runnable mUpdateResults = new Runnable() {
        public void run() {
            updateUI();
        }
    };

    // test message
    String message =   "<?xml version='1.0' encoding='utf-8'?>" +
            "<Message>" +
            "<Consignee>KATHLEEN MCMULLEN</Consignee>" +
            "<NapLat>39.64993</NapLat>" +
            "<NapLong>-76.703443</NapLong>" +
            "<HIN>19206</HIN>" +
            "</Message>";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());

        setContentView(R.layout.activity_main);

//        mLatitudeText = (TextView) findViewById(R.id.latView);
//        mLongitudeText = (TextView) findViewById(R.id.lonView);

//        XMLDestinationParser destinationParser = new XMLDestinationParser();
//        try {
//            destinationParser.parseDestination(message);
//        } catch (XmlPullParserException e) {
//            e.printStackTrace();
//        }

        initializeLocationBeacon();

        mBluetoothServices = new BluetoothServices(this);
        mBluetoothServices.start();

//        if (savedInstanceState == null) {
//            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//            BluetoothServiceFragment fragment = new BluetoothServiceFragment(this);
//            transaction.replace(R.id.activity_main, fragment);
//            transaction.commit();
//
//
//        }

//        mMapView = (MapView) findViewById(R.id.mapView);
//        ArcGISMap mMap = new ArcGISMap(Basemap.Type.TOPOGRAPHIC, 34.056295, -117.195800, 16);
//        mMapView.setMap(mMap);
    }

    private void initializeLocationBeacon() {
        mLocationBeacon = new LocationBeacon(this);
    }

    private void updateUI() {
//        mLatitudeText.setText(String.valueOf(mCurrentLocation.getLatitude()));
//        mLongitudeText.setText(String.valueOf(mCurrentLocation.getLongitude()));
    }

    @Override
    protected void onPause(){
//        mMapView.pause();
        super.onPause();
    }

    @Override
    protected void onResume(){
        super.onResume();
//        mMapView.resume();
    }

    @Override
    // this callback is on the main thread
    public void LocationBeaconOnConnected(Bundle extras, Location location) {
        mCurrentLocation = new Location(location);
        updateUI();
    }

    @Override
    // this callback is on a separate thread
    public void LocationBeaconOnLocationChange(Location location) {
        mCurrentLocation.setLatitude(location.getLatitude());
        mCurrentLocation.setLongitude(location.getLongitude());

        mHandler.post(mUpdateResults);
    }

    @Override
    public void LocationBeaconOnConnectionSuspended(int cause) {

    }

    @Override
    public void LocationBeaconOnConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    protected void onStop() {
        super.onStop();
        mHandler = null;
        mLocationBeacon.onStopFromCaller();
    }

    @Override
    public void bluetoothServiceOnDestinationChange(Destination destination) {
        mDestination = destination;
        Toast.makeText(this, "lat: " + mDestination.getNapLat() + " lon: " + mDestination.getNapLon(), Toast.LENGTH_LONG).show();
    }

}
