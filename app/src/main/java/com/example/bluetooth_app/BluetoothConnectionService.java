package com.example.bluetooth_app;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.UUID;

public class BluetoothConnectionService {
    private static final String TAG = "BluetoothConnectionServ";
    private static final String appName = "MYAPP";
    private static UUID MY_UUID_INSECURE;
    private final BluetoothAdapter mBluetoothAdapter;
    Context mContext;
    private AcceptThread mInsecureAcceptThread;
    private ConnectThread mConnectThread;
    private BluetoothDevice mmDevice;
    private UUID deviceUUID;
    ProgressDialog mProgressDialog;

    public BluetoothConnectionService(Context mContext,UUID uuid) {
        mBluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        this.mContext=mContext;
        MY_UUID_INSECURE=uuid;
        start();
    }

    private class AcceptThread extends Thread{
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread(){
            BluetoothServerSocket tmp=null;

            try {
                Log.d(TAG,"Setting up server using UUID");
                tmp=mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(appName,MY_UUID_INSECURE);
            } catch (IOException e) {
                e.printStackTrace();
            }

            mmServerSocket = tmp;
        }

        public void run(){
            Log.d(TAG,"AcceptThread running");
            BluetoothSocket socket = null;

            Log.d(TAG,"RFCOM server socket start");

            try {
                socket = mmServerSocket.accept(5000);
                Log.d(TAG,"RFCOM server socket accepted connection");
            } catch (IOException e) {
                e.printStackTrace();
            }

//            if(socket !=null)
//            {
//
//            }
            Log.i(TAG,"end AcceptThread");
        }

        public void cancel() {
            Log.d(TAG, "cancel: Canceling AcceptThread.");
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "cancel: Close of AcceptThread ServerSocket failed. " + e.getMessage() );
            }
        }
    }

    private class ConnectThread extends Thread{
        private BluetoothSocket mmSocket;
        public ConnectThread(BluetoothDevice device, UUID uuid){
            Log.d(TAG,"connectThread started");
            mmDevice=device;
            deviceUUID=uuid;

        }

        public void run(){
            BluetoothSocket tmp=null;
            try {
                Log.d(TAG,"trying to create insecureRFCOMSocket using UUID");
                tmp=mmDevice.createInsecureRfcommSocketToServiceRecord(deviceUUID);
            } catch (IOException e) {
                Log.e(TAG,"COULD NOT create insecurerfcommsocket"+e.getMessage());
            }
            mmSocket=tmp;

            mBluetoothAdapter.cancelDiscovery();
            try {
                mmSocket.connect();
                Log.d(TAG,"connection successfull");
            } catch (IOException e) {
                try {
                    e.printStackTrace();
                    try {
                        mmSocket =(BluetoothSocket) mmDevice.getClass().getMethod("createRfcommSocket", new Class[] {int.class}).invoke(mmDevice,1);
                    } catch (IllegalAccessException e1) {
                        e1.printStackTrace();
                    } catch (InvocationTargetException e1) {
                        e1.printStackTrace();
                    } catch (NoSuchMethodException e1) {
                        e1.printStackTrace();
                    }
                    mmSocket.connect();
                    //mmSocket.close();
                    Log.d(TAG,"connection not successfull,socket closed");
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                Log.d(TAG,"could not connect to UUID");
                mProgressDialog.dismiss();
            }
        }

        public void cancel() {
            try {
                Log.d(TAG, "cancel: Closing Client Socket.");
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "cancel: close() of mmSocket in Connectthread failed. " + e.getMessage());
            }
        }
    }

    public synchronized void start(){
        Log.d(TAG,"start");
        if(mConnectThread != null)
        {
            mConnectThread.cancel();
            mConnectThread=null;
        }
        if(mInsecureAcceptThread != null)
        {
            mInsecureAcceptThread=new AcceptThread();
            mInsecureAcceptThread.start();
        }
    }

    public void startClient(BluetoothDevice device,UUID uuid){
        Log.d(TAG,"startClient started");
        mProgressDialog=ProgressDialog.show(mContext,"Connecting Bluetooth","please wait",true);

        mConnectThread=new ConnectThread(device,uuid);
        mConnectThread.start();
    }

}
