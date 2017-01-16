package com.tonydantona.nomad;

import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;

public class MainActivity extends AppCompatActivity implements LocationBeacon.ILocationServices {

    LocationBeacon mLocationBeacon;

    private Location mCurrentLocation;

    private TextView mLatitudeText;
    private TextView mLongitudeText;

    private Handler mHandler = new Handler();
    final Runnable mUpdateResults = new Runnable() {
        public void run() {
            updateUI();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());

        setContentView(R.layout.activity_main);

        mLatitudeText = (TextView) findViewById(R.id.latView);
        mLongitudeText = (TextView) findViewById(R.id.lonView);

        initializeLocationBeacon();
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
}
