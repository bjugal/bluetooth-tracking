package com.example.bluetooth_app;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private static final String TAG="MainActivity";
    BluetoothAdapter mbluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
    public ArrayList<BluetoothDevice> mBTDevices= new ArrayList<>();
    public DeviceListAdapter mDeviceListAdapter;
    public BluetoothDevice toBePaired;

    BluetoothConnectionService mBluetoothConnection;
    private static final UUID MY_UUID_INSECURE = UUID.randomUUID();

    //ListView newDevicesListView;

    private final BroadcastReceiver mbroadcastReceiver1 =new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action =intent.getAction();

            if(action.equals(BluetoothAdapter.ACTION_STATE_CHANGED))
            {
                TextView op=findViewById(R.id.op);
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                String ans;
                switch(state){
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG,"STATE OFF");
                        ans="turned off";
                        op.setText(ans);
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG,"STATE TURNING OFF");
                        ans="turning off";
                        op.setText(ans);
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG,"STATE ON");
                        ans="turned on";
                        op.setText(ans);
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG,"STATE TURNING ON");
                        ans="turning on";
                        op.setText(ans);
                        break;
                }
            }
        }
    };


    private final BroadcastReceiver mbroadcastReceiver2 =new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action =intent.getAction();

            if(action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED))
            {
                TextView op=findViewById(R.id.op);
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);
                String ans;
                switch(state){
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d(TAG,"DISCOVERABLE");
                        ans="discoverability turned on";
                        op.setText(ans);
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d(TAG,"NOT DISCOVERABLE");
                        ans="discoverability off";
                        op.setText(ans);
                        break;
                }
            }
        }
    };

    private BroadcastReceiver mbroadcastReceiver3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "onReceive: ACTION FOUND.");

            if (action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice device = intent.getParcelableExtra (BluetoothDevice.EXTRA_DEVICE);

                //if(!mBTDevices.contains(device))
                    mBTDevices.add(device);
                if(device.getName().equals("Galaxy J2") || device.getName().equals("Lenovo"))
                    toBePaired=device;
                Log.d(TAG, "onReceive: " + device.getName() + ": " + device.getAddress());
                mDeviceListAdapter = new DeviceListAdapter(context, R.layout.device_adapter_view, mBTDevices);
                //newDevicesListView.setAdapter(mDeviceListAdapter);
                String ans="";
                TextView op= findViewById(R.id.op);
                for(BluetoothDevice d:mBTDevices)
                {
                    ans=ans+" "+d.getName();

                }
                op.setText(ans);
            }
        }
    };

    private final BroadcastReceiver mbroadcastReceiver4 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            TextView op = findViewById(R.id.op);
            if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //3 cases:
                //case1: bonded already
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED){
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDED.");
                    op.setText("bonded");
                    toBePaired=mDevice;
                }
                //case2: creating a bone
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDING.");
                    op.setText("bonding");
                }
                //case3: breaking a bond
                if (mDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                    Log.d(TAG, "BroadcastReceiver: BOND_NONE.");
                    op.setText("no bond");
                }
            }
        }
    };

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            TextView op = findViewById(R.id.op);
            String ans;
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            if (device.ACTION_FOUND.equals(action)) {
                ans="device found";
                op.setText(ans);
            }
            else if (device.ACTION_ACL_CONNECTED.equals(action)) {
                ans="device connected";
                op.setText(ans);
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //Done searching
                ans="done searching";
                op.setText(ans);
            }
            else if (device.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
                //Device is about to disconnect
                ans="device about to disconnect";
                op.setText(ans);
            }
            else if (device.ACTION_ACL_DISCONNECTED.equals(action)) {
                //Device has disconnected
                ans="device has disconnected";
                op.setText(ans);
                MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.alert);
                mediaPlayer.start();
            }
        }
    };
    @Override
    protected void onDestroy() {
        Log.d(TAG,"onDestroy() called");
        super.onDestroy();
        unregisterReceiver(mbroadcastReceiver1);
        unregisterReceiver(mbroadcastReceiver2);
        unregisterReceiver(mbroadcastReceiver3);
        unregisterReceiver(mbroadcastReceiver4);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        this.registerReceiver(mReceiver, filter);

        Button checkBtn = findViewById(R.id.checkBtn);
        Button enablediscoverBtn = findViewById(R.id.enabledicoverBtn);
        Button discoverBtn = findViewById(R.id.discoverBtn);
        Button pairBtn=findViewById(R.id.pairBtn);
        Button connectBtn = findViewById(R.id.connectBtn);

        //newDevicesListView=findViewById(R.id.newDevicesListView);
        mBTDevices=new ArrayList<>();

        checkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableDisableBT();
            }
        });

        enablediscoverBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enablediscoverBT();
            }
        });

        discoverBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                discoverDevices();
            }
        });

        pairBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2){
//                    Log.d(TAG, "Trying to pair with " + toBePaired.getName());
                    toBePaired.createBond();
                    mBluetoothConnection = new BluetoothConnectionService(MainActivity.this,MY_UUID_INSECURE);

                }
            }
        });

        IntentFilter bonded = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mbroadcastReceiver4,bonded);

        connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startBTconnection(toBePaired,MY_UUID_INSECURE);
            }
        });
    }

    public void startBTconnection(BluetoothDevice device,UUID uuid) {
        Log.d(TAG,"initializing connection!!!");
        mBluetoothConnection.startClient(device,uuid);
    }

    public void enableDisableBT()
    {
        if(mbluetoothAdapter == null)
            Log.d(TAG,"device does not have BT");
        if(!mbluetoothAdapter.isEnabled())
        {
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBTIntent);

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
             registerReceiver(mbroadcastReceiver1,BTIntent);
        }
        if(mbluetoothAdapter.isEnabled())
        {
            mbluetoothAdapter.disable();
           IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
           registerReceiver(mbroadcastReceiver1,BTIntent);
        }
    }


    public void enablediscoverBT()
    {
        Intent enableDiscoverability = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        enableDiscoverability.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,300);
        startActivity(enableDiscoverability);

        IntentFilter BTDiscover = new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(mbroadcastReceiver2,BTDiscover);
    }

    public void discoverDevices(){
        if(mbluetoothAdapter.isDiscovering()) {
            mbluetoothAdapter.cancelDiscovery();
            mbluetoothAdapter.startDiscovery();

            //checkBTPermissions();
            IntentFilter discovery = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mbroadcastReceiver3, discovery);
        }
        if(!mbluetoothAdapter.isDiscovering()){
            //checkBTPermissions();
            mbluetoothAdapter.startDiscovery();
            IntentFilter discovery = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mbroadcastReceiver3, discovery);
        }
    }

//    private void checkBTPermissions() {
//        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
//            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
//            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
//            if (permissionCheck != 0) {
//
//                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
//            }
//        }else{
//            Log.d(TAG, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
//        }
//    }
}
