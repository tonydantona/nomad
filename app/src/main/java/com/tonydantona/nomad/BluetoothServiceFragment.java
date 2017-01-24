package com.tonydantona.nomad;

import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;

import org.xmlpull.v1.XmlPullParserException;


public class BluetoothServiceFragment extends Fragment {

    private static final String TAG = "NomadBluetoothFragment";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    private BluetoothAdapter mBluetoothAdapter = null;

    private BluetoothServices mBluetoothServices = null;

    private ArrayAdapter<String> mMessageArrayAdapter;

    private ListView mMessageView;

    private String mConnectedDeviceName;

    private Context mContext;

    private IDestinationServices mDestinationListener;

    public BluetoothServiceFragment(Context context) {
        mContext = context;
        mDestinationListener = (IDestinationServices) mContext;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Activity activity = getActivity();
            Toast.makeText(activity, "Bluetooth not supported", Toast.LENGTH_LONG).show();
            activity.finish();
        }
        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();

        // if BT not enabled, request that it be enabled
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
        else {
            setupBluetoothServices();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mBluetoothServices != null) {
            mBluetoothServices.stop();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mBluetoothServices != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mBluetoothServices.getState() == Immutables.STATE_NONE) {
                // Start the Bluetooth chat services
                mBluetoothServices.start();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_bluetooth_service, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mMessageView = (ListView) view.findViewById(R.id.in);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.bluetooth_service, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }

    // tapping the bluetooth symbol at the top right gets you here
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }

    private void setupBluetoothServices() {
        Log.d(TAG, "setupBluetoothServices");

        mMessageArrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.message);
        mMessageView.setAdapter(mMessageArrayAdapter);

        mBluetoothServices = new BluetoothServices(getActivity(), mHandler);
    }

    // The Handler that gets information back from the BluetoothChatService

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            FragmentActivity activity = getActivity();
            switch (msg.what) {
                case Immutables.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case Immutables.STATE_CONNECTED:
                            setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                            mMessageArrayAdapter.clear();
                            break;
                        case Immutables.STATE_CONNECTING:
                            setStatus(R.string.title_connecting);
                            break;
                        case Immutables.STATE_LISTEN:
                        case Immutables.STATE_NONE:
                            setStatus(R.string.title_not_connected);
                            break;
                    }
                    break;
                case Immutables.MESSAGE_WRITE:
                    if (null != activity) {
                        Toast.makeText(activity, "Error, this app does not write messages", Toast.LENGTH_LONG).show();
                    }
                    break;
                case Immutables.MESSAGE_READ:
                    Toast.makeText(activity, "New destination received", Toast.LENGTH_SHORT).show();
                    // reads the incoming message
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    XMLDestinationParser destinationParser = new XMLDestinationParser();
                    Destination dest = null;
                    try {
                        dest = destinationParser.parseDestination(readMessage);
                    } catch (XmlPullParserException e) {
                        e.printStackTrace();
                    }
                    mDestinationListener.bluetoothServiceOnDestinationChange(dest);
                    //mMessageArrayAdapter.add(mConnectedDeviceName + ":  " + readMessage);
                    break;
                case Immutables.MESSAGE_DEVICE_NAME:
                    // saves the connected device's name and displays it upon connection
                    mConnectedDeviceName = msg.getData().getString(Immutables.DEVICE_NAME);
                    if (null != activity) {
                        Toast.makeText(activity, "Connected to " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Immutables.MESSAGE_TOAST:
                    if (null != activity) {
                        Toast.makeText(activity, msg.getData().getString(Immutables.TOAST), Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };

    // Updates the status on the action bar.
    private void setStatus(int resourceId) {
        FragmentActivity activity = getActivity();
        if (null == activity) {
            return;
        }
        final ActionBar actionBar = activity.getActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(resourceId);
    }

    // Updates the status on the action bar.
    private void setStatus(CharSequence subTitleStatus) {
        FragmentActivity activity = getActivity();
        if (null == activity) {
            return;
        }
        final ActionBar actionBar = activity.getActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(subTitleStatus);
    }

    public interface IDestinationServices {
        void bluetoothServiceOnDestinationChange(Destination location);
    }

}
