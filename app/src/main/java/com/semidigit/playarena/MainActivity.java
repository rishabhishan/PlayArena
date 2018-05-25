package com.semidigit.playarena;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.hoin.btsdk.BluetoothService;
import com.semidigit.playarena.Utils.BTService;
import com.semidigit.playarena.Utils.Constants;
import com.semidigit.playarena.Utils.UtilityMethods;

import java.io.IOException;
import java.util.Set;

import static com.semidigit.playarena.Utils.Constants.REQUEST_ENABLE_BT;

public class MainActivity extends AppCompatActivity {

    BTService btService;
    UtilityMethods utilityMethods;
    private static final String ACTIVITY_LOG_TAG = ".MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        btService=BTService.getInstance();
        utilityMethods =  new UtilityMethods(this);

        findViewById(R.id.btn_scan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(btService.mService.getState()== BluetoothService.STATE_CONNECTED) {
                    startActivity(new Intent(MainActivity.this, ScanActivity.class));
                }else{
                    Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Error connecting to printer. Try restarting the application", Snackbar.LENGTH_INDEFINITE);
                    snackbar.show();
                }
            }
        });

        findViewById(R.id.btn_generate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(btService.mService.getState()== BluetoothService.STATE_CONNECTED) {
                    startActivity(new Intent(MainActivity.this, DisplayQRActivity.class));
                }else{
                    Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Error connecting to printer. Try restarting the application", Snackbar.LENGTH_INDEFINITE);
                    snackbar.show();
                }
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.logout_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_logout:
                PreferenceManager.getDefaultSharedPreferences(this).edit().remove("username").commit();
                PreferenceManager.getDefaultSharedPreferences(this).edit().remove("company_id").commit();
                PreferenceManager.getDefaultSharedPreferences(this).edit().remove("password").commit();
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
