package com.tonydantona.nomad;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParserException;

/**
 * Created by rti1ajd on 12/12/2016.
 */

public class BluetoothServices {

    private static final String TAG = "BluetoothServices";

    private final BluetoothAdapter mBluetoothAdapter;
    private final Context mContext;
    private IDestinationServices mDestinationListener;
    private int mState;
    private String mConnectedDeviceName;

    private AcceptThread mAcceptThread;
    private ConnectedThread mConnectedThread;

    public BluetoothServices(Context context) {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mContext = context;
        mDestinationListener = (IDestinationServices) context;
        mState = Immutables.STATE_NONE;
    }

    public void start() {
        Log.d(TAG, "start");

          // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        setState(Immutables.STATE_LISTEN);

        // Start the thread to listen on a BluetoothServerSocket
        if (mAcceptThread == null) {
            mAcceptThread = new AcceptThread(this, mBluetoothAdapter);
            mAcceptThread.start();
        }
    }

    public void stop() {
    }

    // Start the ConnectedThread to begin managing a Bluetooth connection
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        Log.d(TAG, "connected, Socket Type");

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Cancel the accept thread because we only want to connect to one device
        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket, this, mDestHandler);
        mConnectedThread.start();

        // Send the name of the connected device back to the UI Activity
        Message msg = mDestHandler.obtainMessage(Immutables.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(Immutables.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mDestHandler.sendMessage(msg);

        setState(Immutables.STATE_CONNECTED);
    }

    private final Handler mDestHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Immutables.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case Immutables.STATE_CONNECTED:
                            Toast.makeText(mContext, mContext.getString(R.string.title_connected_to) + " " + mConnectedDeviceName, Toast.LENGTH_LONG).show();
                            break;
                        case Immutables.STATE_CONNECTING:
                            Toast.makeText(mContext, mContext.getString(R.string.title_connecting) +  mConnectedDeviceName, Toast.LENGTH_LONG).show();
                            break;
                        case Immutables.STATE_LISTEN:
                        case Immutables.STATE_NONE:
                            break;
                    }
                    break;
                case Immutables.MESSAGE_WRITE:
                    if (null != mContext) {
                        Toast.makeText(mContext, "Error, this app does not write messages", Toast.LENGTH_LONG).show();
                    }
                    break;
                case Immutables.MESSAGE_READ:
                    Toast.makeText(mContext, "New destination received", Toast.LENGTH_SHORT).show();
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
                    break;
                case Immutables.MESSAGE_DEVICE_NAME:
                    // saves the connected device's name and displays it upon connection
                    mConnectedDeviceName = msg.getData().getString(Immutables.DEVICE_NAME);
                    break;
                case Immutables.MESSAGE_TOAST:
                    if (null != mContext) {
                        Toast.makeText(mContext, msg.getData().getString(Immutables.TOAST), Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };

    public synchronized void setState(int state) {
        Log.d(TAG, "setState() " + mState + "->" + state);
        mState = state;

        mDestHandler.obtainMessage(Immutables.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    public synchronized int getState() {
        return mState;
    }

    public interface IDestinationServices {
        void bluetoothServiceOnDestinationChange(Destination location);
    }
}
