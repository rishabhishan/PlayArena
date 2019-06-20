package com.semidigit.v2.Utils;

import android.bluetooth.BluetoothDevice;
import android.support.v7.app.AppCompatActivity;

import com.hoin.btsdk.BluetoothService;

public class BTService extends AppCompatActivity {

    public BluetoothService mService=null;
    public BluetoothDevice con_dev = null;

    private static BTService singleton = new BTService( );
    private BTService() { }

    /* Static 'instance' method */
    public static BTService getInstance( ) {
        return singleton;
    }


}
