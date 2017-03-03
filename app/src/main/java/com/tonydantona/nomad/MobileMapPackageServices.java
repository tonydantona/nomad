package com.tonydantona.nomad;

// Created by rti1ajd on 1/30/2017.

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.esri.arcgisruntime.data.TransportationNetworkDataset;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.MobileMapPackage;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.tasks.networkanalysis.RouteTask;

import java.io.File;
import java.util.List;

import static com.tonydantona.nomad.R.id.mapView;

public class MobileMapPackageServices {

    private static final String TAG = "MainActivity";

    private static final String FILE_EXTENSION = ".mmpk";
    private static File extStorDir;
    private static String extSDCardDirName;
    private static String filename;
    private static String mmpkFilePath;

    MobileMapPackage mMobileMapPackage;

    private IMmpkCallbacks mmpkCallbacksListener;
    private Context mContext;

    // define permission to request
    String[] reqPermission = new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private int requestCode = 2;

    public MobileMapPackageServices(Context context) {

        mContext = context;
        mmpkCallbacksListener = (IMmpkCallbacks)context;

        // get sdcard resource name
        extStorDir = Environment.getExternalStorageDirectory();
        // get the directory
        extSDCardDirName = mContext.getResources().getString(R.string.config_data_sdcard_offline_dir);
        // get mobile map package filename
        filename = mContext.getResources().getString(R.string.config_mmpk_name);
        // create the full path to the mobile map package file
        mmpkFilePath = createMobileMapPackageFilePath();

        // need to initialize
        mMobileMapPackage = null;
    }

    public void loadMapFromMobileMapPackage() {

        // For API level 23+ request permission at runtime
        if(ContextCompat.checkSelfPermission(mContext, reqPermission[0]) == PackageManager.PERMISSION_GRANTED) {
            loadMobileMapPackage(mmpkFilePath);
        }else{
            // request permission
            ActivityCompat.requestPermissions((Activity) mContext, reqPermission, requestCode);
        }
    }

    public TransportationNetworkDataset getTransportationNetwork() {
        List<TransportationNetworkDataset> datasets = mMobileMapPackage.getMaps().get(0).getTransportationNetworks();
        return datasets.get(0);
    }

    // Create the mobile map package file location and name structure
    private static String createMobileMapPackageFilePath(){
        return extStorDir.getAbsolutePath() + File.separator + extSDCardDirName + File.separator + filename + FILE_EXTENSION;
    }

    private void loadMobileMapPackage(String mmpkFile){
        // create the mobile map package
        mMobileMapPackage = new MobileMapPackage(mmpkFile);
        // load the mobile map package asynchronously
        mMobileMapPackage.loadAsync();

        // add done listener which will invoke when mobile map package has loaded
        mMobileMapPackage.addDoneLoadingListener(new Runnable() {
            @Override
            public void run() {
                // check load status and that the mobile map package has maps
                if(mMobileMapPackage.getLoadStatus() == LoadStatus.LOADED && mMobileMapPackage.getMaps().size() > 0){
                    mmpkCallbacksListener.onMmpkDoneLoading(mMobileMapPackage.getMaps().get(0));
                }else{
                    // Log an issue if the mobile map package fails to load
                    Log.e(TAG, mMobileMapPackage.getLoadError().getMessage());
                }
            }
        });
    }

    public interface IMmpkCallbacks {

        void onMmpkDoneLoading(ArcGISMap map);
    }

}

