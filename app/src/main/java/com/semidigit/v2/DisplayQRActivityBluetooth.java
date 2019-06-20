package com.semidigit.v2;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.hoin.btsdk.BluetoothService;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.semidigit.v2.Utils.BTService;
import com.semidigit.v2.Utils.Constants;
import com.semidigit.v2.Utils.HttpConnectionService;
import com.semidigit.v2.Utils.UtilityMethods;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Date;
import java.util.HashMap;
import static com.semidigit.v2.Utils.Constants.ALIGN_CENTER;
import static com.semidigit.v2.Utils.Constants.CHECKIN_API_PATH;
import static com.semidigit.v2.Utils.Constants.RESET_PRINTER;
import static com.semidigit.v2.Utils.Constants.bb;
import static com.semidigit.v2.Utils.Constants.bb2;
import static com.semidigit.v2.Utils.Constants.cc;

/**
 * Created by rishabhk on 15/04/18.
 */

public class DisplayQRActivityBluetooth extends AppCompatActivity {


    private static final String ACTIVITY_LOG_TAG = ".DisplayQRActivityBluetooth";
    ImageView qr;
    String vehicle_no;
    EditText et1, et2, et3, et4;
    UtilityMethods utilityMethods;
    private PrintBilltask printBillTask = null;
    private CheckinEntry checkinEntry = null;

    String current_timestamp;
    String qr_data;
    String username, company_id;

    BTService btService;

    UtilityMethods util;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_qr_bluetooth);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        btService = BTService.getInstance();
        utilityMethods =  new UtilityMethods(this);
        username = PreferenceManager.getDefaultSharedPreferences(this).getString("username", "");
        company_id = PreferenceManager.getDefaultSharedPreferences(this).getString("company_id", "");
        qr =  (ImageView)findViewById(R.id.iv_qr);
        util = new UtilityMethods(this);
        generate_qr();
        et1 = findViewById(R.id.et_vehicle_no_1);
        et2 = findViewById(R.id.et_vehicle_no_2);
        et3 = findViewById(R.id.et_vehicle_no_3);
        et4 = findViewById(R.id.et_vehicle_no_4);
        et1.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence s, int start,int before, int count)
            {
                // TODO Auto-generated method stub
                if(et1.getText().toString().length()==1)
                {
                    et2.requestFocus();
                }
            }
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            public void afterTextChanged(Editable s) {
            }

        });
        et2.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence s, int start,int before, int count)
            {
                // TODO Auto-generated method stub
                if(et2.getText().toString().length()==1)
                {
                    et3.requestFocus();
                }
            }
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            public void afterTextChanged(Editable s) {
            }

        });
        et3.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence s, int start,int before, int count)
            {
                // TODO Auto-generated method stub
                if(et3.getText().toString().length()==1)
                {
                    et4.requestFocus();
                }
            }
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            public void afterTextChanged(Editable s) {
            }

        });

        et4.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence s, int start,int before, int count)
            {
                // TODO Auto-generated method stub
                if(et4.getText().toString().length()==1)
                {
                    hideKeyboard();
                }
            }
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            public void afterTextChanged(Editable s) {
            }

        });

    }

    public void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


    public void printTicket(View v){
        vehicle_no = "" + et1.getText().toString() + et2.getText().toString() + et3.getText().toString() + et4.getText().toString();
        if(btService.mService!=null) {
            if(btService.mService.getState()==BluetoothService.STATE_CONNECTED){
                checkinEntry = new CheckinEntry(this, qr_data,username, company_id , vehicle_no);
                checkinEntry.execute((Void) null);
            }else{
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Error connecting to printer. Try restarting the application", Snackbar.LENGTH_INDEFINITE);
                snackbar.show();
            }
        }
        else {
            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Error connecting to printer. Try restarting the application", Snackbar.LENGTH_LONG);
            snackbar.show();
        }
    }

    public void generate_qr() {
        // Find current time

        Date dateCur = new Date();
        current_timestamp = util.displayDate(dateCur);
        qr_data = String.valueOf(dateCur.getTime());

        Log.d(ACTIVITY_LOG_TAG, "QR Data : " + qr_data);
        Log.d(ACTIVITY_LOG_TAG, "Timestamp : " + current_timestamp);

        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(qr_data, BarcodeFormat.QR_CODE,200,200);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            qr.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    class CheckinEntry extends AsyncTask<Void, Void, JSONObject> {

        Context context;
        private ProgressDialog pdia;
        String in_time, in_id, company_id, vehicle_no;

        CheckinEntry(Context context, String in_time, String in_id, String company_id, String vehicle_no) {
            this.context = context;
            this.in_time = in_time;
            this.in_id = in_id;
            this.company_id = company_id;
            this.vehicle_no=vehicle_no;
        }

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            pdia = new ProgressDialog(context);
            pdia.setMessage("Syncing with server...");
            pdia.show();
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            HashMap<String, String> postDataParams;
            postDataParams = new HashMap<String, String>();
            postDataParams.put("HTTP_ACCEPT", "application/json");
            postDataParams.put("in_time", in_time);
            postDataParams.put("in_id", in_id);
            postDataParams.put("company_id", company_id);
            postDataParams.put("vehicle_no", vehicle_no);

            HttpConnectionService service = new HttpConnectionService();
            String response = service.sendRequest(CHECKIN_API_PATH, postDataParams);
            JSONObject resultJsonObject = null;
            try {
                resultJsonObject = new JSONObject(response);
                return resultJsonObject;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return resultJsonObject;
        }
        // TODO: register the new account here

        @Override
        protected void onPostExecute(final JSONObject resultJsonObject) {
            checkinEntry = null;
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
                    String invoice_id = utilityMethods.getValueOrDefaultString(resultJsonObject.get("invoice_id"),"NA");
                    Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Data synced with server", Snackbar.LENGTH_LONG);
                    snackbar.show();
                    printBillTask = new PrintBilltask(context, invoice_id, vehicle_no);
                    printBillTask.execute((Void) null);
                } catch (JSONException e) {
                    Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Unexpected response. Try again", Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
            }



        }

        @Override
        protected void onCancelled() {
            printBillTask = null;
        }

    }


    class PrintBilltask extends AsyncTask<Void, Void, Boolean> {

        Context context;
        private ProgressDialog pdia;
        String vehicle_no, invoice_id;

        PrintBilltask(Context context, String invoice_id, String vehicle_no) {
            this.context = context;
            this.vehicle_no=vehicle_no;
            this.invoice_id=invoice_id;
        }

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            pdia = new ProgressDialog(context);
            pdia.setMessage("Printing Ticket ...");
            pdia.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            btService.mService.write(RESET_PRINTER);
            btService.mService.write(bb2);
            btService.mService.write(ALIGN_CENTER);
            btService.mService.sendMessage("Play", "GBK");
            btService.mService.write(cc);
            btService.mService.sendMessage("Sarjapur Road", "GBK");
            btService.mService.sendMessage("==============================", "GBK");
            btService.mService.write(bb);
            btService.mService.sendMessage(invoice_id, "GBK");
            btService.mService.sendMessage(current_timestamp, "GBK");
            btService.mService.sendMessage(vehicle_no, "GBK");

            byte[] cmd;
            cmd = new byte[7];
            cmd[0] = 0x1B;
            cmd[1] = 0x5A;
            cmd[2] = 0x00;
            cmd[3] = 0x02;
            cmd[4] = 0x07;
            cmd[5] = 0x0D;
            cmd[6] = 0x00;
            btService.mService.write(cmd);
            btService.mService.write(qr_data.getBytes());
            btService.mService.write(cc);
            btService.mService.sendMessage(Constants.FOOTER_MSG_TICKET, "GBK");
            btService.mService.write(RESET_PRINTER);
            btService.mService.sendMessage("\n", "GBK");
            btService.mService.sendMessage("\n", "GBK");
            return null;
        }
        // TODO: register the new account here

        @Override
        protected void onPostExecute(final Boolean success) {
            printBillTask = null;
            pdia.dismiss();
        }

        @Override
        protected void onCancelled() {
            printBillTask = null;
        }

    }
}
