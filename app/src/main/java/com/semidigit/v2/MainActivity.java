package com.semidigit.v2;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
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
import com.semidigit.v2.Utils.BTService;
import com.semidigit.v2.Utils.Constants;
import com.semidigit.v2.Utils.HttpConnectionService;
import com.semidigit.v2.Utils.UtilityMethods;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

import static com.semidigit.v2.Utils.Constants.REQUEST_ENABLE_BT;

public class MainActivity extends AppCompatActivity {

    private TotalCollectionTask totalCollectionTask = null;
    TextView tv_total_collection, tv_current_date, tv_collections_count;

    String user_id, company_id;

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
                startActivity(new Intent(MainActivity.this, ScanActivity.class));
            }
        });

        findViewById(R.id.btn_generate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Constants.BLUETOOTH_PRINTER_AT_ENTRY) {
                    startActivity(new Intent(MainActivity.this, DisplayQRActivityBluetooth.class));
                }
                else{
                    startActivity(new Intent(MainActivity.this, DisplayQRActivityUSB.class));
                }

            }
        });

        findViewById(R.id.btn_lost_ticket).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    startActivity(new Intent(MainActivity.this, LostTicketInput.class));
            }
        });

        setDailyCollection();

    }

    public void setDailyCollection(){
        user_id = PreferenceManager.getDefaultSharedPreferences(this).getString("username", "");
        company_id = PreferenceManager.getDefaultSharedPreferences(this).getString("company_id", "");

        tv_total_collection=(TextView) findViewById(R.id.tv_total_collection);
        tv_current_date=(TextView) findViewById(R.id.tv_current_date);
        tv_collections_count=(TextView) findViewById(R.id.tv_collections_count);

        SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd MMM yy");
        tv_current_date.setText(formatter.format(new Date()));

        totalCollectionTask = new TotalCollectionTask(this, user_id, company_id);
        totalCollectionTask.execute((Void) null);

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

    private Boolean exit = false;
    @Override
    public void onBackPressed() {
        if (exit) {
            finish(); // finish activity
        } else {
            Toast.makeText(this, "Press Back again to Exit.",
                    Toast.LENGTH_SHORT).show();
            exit = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    exit = false;
                }
            }, 3 * 1000);

        }

    }


    class TotalCollectionTask extends AsyncTask<Void, Void, JSONObject> {
        Context context;
        private ProgressDialog pdia;
        String company_id, staff_id;

        TotalCollectionTask(Context context, String staff_id, String company_id) {
            this.context = context;
            this.staff_id=staff_id;
            this.company_id = company_id;
        }

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            pdia = new ProgressDialog(context);
            pdia.setMessage("Retrieving details ...");
            if(!((Activity) context).isFinishing())
            {
                pdia.show();
            }
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            HashMap<String, String> postDataParams;
            postDataParams = new HashMap<String, String>();
            postDataParams.put("HTTP_ACCEPT", "application/json");
            postDataParams.put("staff_id", staff_id);
            postDataParams.put("company_id", company_id);

            HttpConnectionService service = new HttpConnectionService();
            String response = service.sendRequest(String.format(Constants.TOTAL_COLLECTION), postDataParams);
            JSONObject resultJsonObject = null;
            try {
                resultJsonObject = new JSONObject(response);
                return resultJsonObject;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return resultJsonObject;
        }

        @Override
        protected void onPostExecute(final JSONObject resultJsonObject) {
            totalCollectionTask = null;
            if(pdia!=null)
                pdia.dismiss();
            int responseCode=1;

            try {
                responseCode = utilityMethods.getValueOrDefaultInt(resultJsonObject.get("responseCode"),1);
            } catch (JSONException e) {
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Unexpected response. Try again", Snackbar.LENGTH_LONG);
                snackbar.show();
            }

            if (responseCode==1){
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Task Failed. Something went wrong. Try again!", Snackbar.LENGTH_LONG);
                snackbar.show();
            }
            else{
                try {
                    String total_amount = utilityMethods.getValueOrDefaultString(resultJsonObject.get("total_amount"),"0");
                    String collection_count = utilityMethods.getValueOrDefaultString(resultJsonObject.get("collection_count"),"NA");
                    tv_total_collection.setText(total_amount);
                    tv_collections_count.setText(collection_count);
                } catch (JSONException e) {
                    Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Unexpected response. Try again", Snackbar.LENGTH_LONG);
                    snackbar.show();
                }

            }
        }

        @Override
        protected void onCancelled() {
            totalCollectionTask = null;
            if(pdia!=null)
                pdia.dismiss();
        }
    }



}
