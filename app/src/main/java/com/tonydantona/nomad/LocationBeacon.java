package com.tonydantona.nomad;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;

public class LocationBeacon implements com.google.android.gms.location.LocationListener, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    private ILocationServices mLocationListener;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    // The desired interval for location updates. Inexact. Updates may be more or less frequent.
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    // The fastest rate for active location updates. Exact. Updates will never be more frequent than this value.
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    private Context mContext;
    private boolean mRequestingLocationUpdates;

    private HandlerThread mGpsHandlerThread;


    public LocationBeacon(Context context) {
        mContext = context;
        mLocationListener = (ILocationServices) mContext;

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(context)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        mRequestingLocationUpdates = true;
        createLocationRequest();
        getLocationSettings();

        mGoogleApiClient.connect();
    }


    // mLocationRequest will passed into requestLocationUpdates
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void getLocationSettings() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
    }

    private void startLocationUpdates() {
        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mGpsHandlerThread = getGpsHandlerThread();
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this, mGpsHandlerThread.getLooper());
    }

    private HandlerThread getGpsHandlerThread() {
        if (mGpsHandlerThread == null) {
            mGpsHandlerThread = new HandlerThread("GpsHandlerThread");
            mGpsHandlerThread.start();
        }

        return mGpsHandlerThread;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location currentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (currentLocation == null) {
            return;
        }

        mLocationListener.onLocationBeaconConnected(bundle, currentLocation);

        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mLocationListener.onLocationBeaconLocationChange(location);
    }

    @Override
    public void onConnectionSuspended(int cause) {
        mLocationListener.onLocationBeaconConnectionSuspended(cause);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        mLocationListener.onLocationBeaconConnectionFailed(connectionResult);
    }

    public void onStopFromCaller() {
        if (mGpsHandlerThread != null) {
            mGpsHandlerThread.quit();
            mGpsHandlerThread = null;
        }
    }


    public interface ILocationServices {
        void onLocationBeaconLocationChange(Location location);
        void onLocationBeaconConnected(Bundle extras, Location currentLocation);
        void onLocationBeaconConnectionSuspended(int cause);
        void onLocationBeaconConnectionFailed(ConnectionResult connectionResult);
    }
}
