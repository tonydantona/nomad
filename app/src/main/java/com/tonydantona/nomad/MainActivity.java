package com.tonydantona.nomad;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.ArcGISVectorTiledLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.LocationDisplay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.tasks.networkanalysis.Route;
import com.esri.arcgisruntime.tasks.networkanalysis.RouteParameters;
import com.esri.arcgisruntime.tasks.networkanalysis.RouteResult;
import com.esri.arcgisruntime.tasks.networkanalysis.RouteTask;
import com.esri.arcgisruntime.tasks.networkanalysis.Stop;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.tonydantona.nomad.R.id.mapView;

public class MainActivity extends AppCompatActivity implements BluetoothServices.IDestinationServices, MobileMapPackageServices.IMmpkCallbacks {

    private static final String TAG = "MainActivity";
    // Spatial references used for projecting points
    public static final SpatialReference mWmSpatialReference = SpatialReference.create(102100);
    public static final SpatialReference mEgsSpatialReference = SpatialReference.create(4326);

    private BluetoothServices mBluetoothServices;
    private MobileMapPackageServices mMmpkServices;

    private LocationDisplay mLocationDisplay;
    private Destination mDestination;

    private List<Stop> mStops;
    RouteParameters mRouteParameters = null;

    private MapView mMapView;
    private GraphicsOverlay mGraphicsOverlay;

    private RouteTask mRouteTask;

    private ArcGISVectorTiledLayer mVectorTiledLayer;

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

        mBluetoothServices = new BluetoothServices(this);
        mBluetoothServices.start();

        // retrieve the MapView from layout
        mMapView = (MapView) findViewById(mapView);

        /* testing portal */

        ArcGISVectorTiledLayer mVectorTiledLayer = new ArcGISVectorTiledLayer(getResources().getString(R.string.navigation_vector));

        // set tiled layer as basemap
        Basemap basemap = new Basemap(mVectorTiledLayer);
        // create a map with the basemap
        ArcGISMap mMap = new ArcGISMap(basemap);
        // create a viewpoint from lat, long, scale
//        Viewpoint huntValleyPoint = new Viewpoint(39.4988544, -76.6570629, 100000);
//        // set initial map extent
//        mMap.setInitialViewpoint(huntValleyPoint);
        // set the map to be displayed in this view

        Viewpoint sanDiegoPoint = new Viewpoint(32.7157, -117.1611, 200000);
        // set initial map extent
        mMap.setInitialViewpoint(sanDiegoPoint);
        mMapView.setMap(mMap);
        /* end testing portal */

        mGraphicsOverlay = new GraphicsOverlay();

        //add the overlay to the map view
        mMapView.getGraphicsOverlays().add(mGraphicsOverlay);

        // load the map
//        mMmpkServices = new MobileMapPackageServices(this);
//        mMmpkServices.loadMapFromMobileMapPackage();

//        setupLocationDisplay();
        setupRouteTask();
    }

    private void routeStops() {
        mRouteParameters.getStops().addAll(mStops);

        final ListenableFuture<RouteResult> routeResultFuture = mRouteTask.solveRouteAsync(mRouteParameters);

        mRouteTask.addDoneLoadingListener(new Runnable() {
            @Override
            public void run() {
                try {
                    RouteResult routeResult = routeResultFuture.get();
                    // process result
                    Route route = routeResult.getRoutes().get(0);

                    int routeSymbolSize = 10;
                    SimpleLineSymbol routeSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.DASH_DOT, Color.YELLOW, routeSymbolSize);
                    Graphic routeGraphic = new Graphic(route.getRouteGeometry(), routeSymbol);
                    mGraphicsOverlay.getGraphics().add(routeGraphic);
                }
                catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void processResults() {

    }

    private void loadStops() {
        mStops = new ArrayList<Stop>();
//        mStops.add(getDepot());
//        mStops.add(new Stop(new Point(-76.6570629,39.4988544)));

        mStops.add(new Stop(new Point(-117.15083257944445, 32.741123367963446, SpatialReferences.getWgs84())));
        mStops.add(new Stop(new Point(-117.15557279683529, 32.703360305883045, SpatialReferences.getWgs84())));
    }

    private Stop getDepot() {
        Point p = new Point(-76.6570629, 39.4988544);
        return new Stop(p);
    }

    private void setupRouteTask() {
//        mRouteTask = new RouteTask(this, mMmpkServices.getTransportationNetwork());
        mRouteTask = new RouteTask("http://sampleserver6.arcgisonline.com/arcgis/rest/services/NetworkAnalysis/SanDiego/NAServer/Route");

        mRouteTask.loadAsync();
        mRouteTask.addDoneLoadingListener(new Runnable() {
            @Override
            public void run() {
                mRouteTask.getLoadError();
                mRouteTask.getLoadStatus();
            }
        });

        final ListenableFuture<RouteParameters> routeParametersFuture = mRouteTask.createDefaultParametersAsync();

        // in a non-UI thread, get the route parameters
        routeParametersFuture.addDoneListener(new Runnable() {
            @Override
            public void run() {
                try {
                    mRouteParameters = routeParametersFuture.get();

                    // update properties of route parameters
                    mRouteParameters.setReturnDirections(true);
                    mRouteParameters.setReturnRoutes(true);
                    mRouteParameters.setOutputSpatialReference(mMapView.getSpatialReference());

                    loadStops();
                    routeStops();
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                    Log.e(TAG, e.getMessage());
                } catch (ExecutionException e) {
                    e.printStackTrace();
                    Log.e(TAG, e.getMessage());
                }
            }
        });
    }

    private void setupLocationDisplay() {
        // get the MapView's LocationDisplay
        mLocationDisplay = mMapView.getLocationDisplay();

        mLocationDisplay.addDataSourceStatusChangedListener(new LocationDisplay.DataSourceStatusChangedListener() {
            @Override
            public void onStatusChanged(LocationDisplay.DataSourceStatusChangedEvent dataSourceStatusChangedEvent) {
                if (dataSourceStatusChangedEvent.getSource().getLocationDataSource().getError() == null) {
                    Log.e(TAG, "Location Display Started=" + dataSourceStatusChangedEvent.isStarted());
                } else {
                    // Deal with problems starting the LocationDisplay...
                }
            }
        });
        mLocationDisplay.startAsync();

        mLocationDisplay.addLocationChangedListener(new LocationDisplay.LocationChangedListener() {
            @Override
            public void onLocationChanged(LocationDisplay.LocationChangedEvent locationChangedEvent) {

            }

        });
    }

    private void updateUI() {

    }

    @Override
    protected void onPause(){
        super.onPause();
        mMapView.pause();
    }

    @Override
    protected void onResume(){
        super.onResume();
        mMapView.resume();

        if (mBluetoothServices != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mBluetoothServices.getState() == Immutables.STATE_NONE) {
                // Start the Bluetooth chat services
                mBluetoothServices.start();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBluetoothServices != null) {
            mBluetoothServices.stop();
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void bluetoothServiceOnDestinationChange(Destination destination) {
        mDestination = destination;
        Toast.makeText(this, "lat: " + mDestination.getNapLat() + " lon: " + mDestination.getNapLon(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onMmpkDoneLoading(ArcGISMap map) {
        Viewpoint huntValleyPoint = new Viewpoint(39.4988544, -76.6570629, 20000);
        // set initial map extent
        map.setInitialViewpoint(huntValleyPoint);
        mMapView.setMap(map);

        setupLocationDisplay();
        setupRouteTask();
    }
}
