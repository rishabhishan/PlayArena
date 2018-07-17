package com.semidigit.v2.Utils;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;

import static com.semidigit.v2.Utils.Constants.REQUEST_ENABLE_BT;

public class BaseApplicationUtility extends Application {

    BTService btService;
    private static final String ACTIVITY_LOG_TAG = ".BaseApplicationUtility";
    @Override
    public void onTerminate() {
        super.onTerminate();
        btService=BTService.getInstance();
        if (btService.mService != null)
            btService.mService.stop();
        btService.mService = null;
    }
}
