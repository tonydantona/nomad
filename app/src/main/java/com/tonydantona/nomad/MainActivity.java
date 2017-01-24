package com.tonydantona.nomad;

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

import com.google.android.gms.common.ConnectionResult;

import org.xmlpull.v1.XmlPullParserException;

public class MainActivity extends AppCompatActivity implements LocationBeacon.ILocationServices, BluetoothServiceFragment.IDestinationServices {

    LocationBeacon mLocationBeacon;

    private Location mCurrentLocation;
    private Destination mDestination;

    private TextView mLatitudeText;
    private TextView mLongitudeText;

    private Handler mHandler = new Handler();
    final Runnable mUpdateResults = new Runnable() {
        public void run() {
            updateUI();
        }
    };


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

        mLatitudeText = (TextView) findViewById(R.id.latView);
        mLongitudeText = (TextView) findViewById(R.id.lonView);

        XMLDestinationParser destinationParser = new XMLDestinationParser();
        try {
            destinationParser.parseDestination(message);
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }

        initializeLocationBeacon();

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            BluetoothServiceFragment fragment = new BluetoothServiceFragment(this);
            transaction.replace(R.id.activity_main, fragment);
            transaction.commit();
        }
    }

    private void initializeLocationBeacon() {
        mLocationBeacon = new LocationBeacon(this);
    }

    private void updateUI() {
        mLatitudeText.setText(String.valueOf(mCurrentLocation.getLatitude()));
        mLongitudeText.setText(String.valueOf(mCurrentLocation.getLongitude()));
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
