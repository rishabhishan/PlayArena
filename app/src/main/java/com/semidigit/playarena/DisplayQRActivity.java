package com.semidigit.playarena;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.hoin.btsdk.BluetoothService;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.semidigit.playarena.Utils.BTService;
import com.semidigit.playarena.Utils.Constants;
import com.semidigit.playarena.Utils.HttpConnectionService;
import com.semidigit.playarena.Utils.UtilityMethods;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Date;
import java.util.HashMap;
import static com.semidigit.playarena.Utils.Constants.ALIGN_CENTER;
import static com.semidigit.playarena.Utils.Constants.RESET_PRINTER;
import static com.semidigit.playarena.Utils.Constants.bb;
import static com.semidigit.playarena.Utils.Constants.bb2;
import static com.semidigit.playarena.Utils.Constants.cc;

/**
 * Created by rishabhk on 15/04/18.
 */

public class DisplayQRActivity extends AppCompatActivity {

    private static final String apiPath = Constants.CHECKIN_API_PATH;
    private static final String ACTIVITY_LOG_TAG = ".DisplayQRActivity";
    ImageView qr;

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
        setContentView(R.layout.display_qr);
        btService = BTService.getInstance();
        username = PreferenceManager.getDefaultSharedPreferences(this).getString("username", "");
        company_id = PreferenceManager.getDefaultSharedPreferences(this).getString("company_id", "");
        qr =  (ImageView)findViewById(R.id.iv_qr);
        util = new UtilityMethods(this);
        generate_qr();
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

        if(btService.mService.getState()==BluetoothService.STATE_CONNECTED){
            printBillTask = new PrintBilltask(this);
            printBillTask.execute((Void) null);
            checkinEntry = new CheckinEntry(this, qr_data,username, company_id );
            checkinEntry.execute((Void) null);
        }else{
            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Error connecting to printer. Try restarting the application", Snackbar.LENGTH_INDEFINITE);
            snackbar.show();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    class CheckinEntry extends AsyncTask<Void, Void, Integer> {

        Context context;
        private ProgressDialog pdia;
        String in_time, in_id, company_id;

        CheckinEntry(Context context, String in_time, String in_id, String company_id) {
            this.context = context;
            this.in_time = in_time;
            this.in_id = in_id;
            this.company_id = company_id;
        }

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            pdia = new ProgressDialog(context);
            pdia.setMessage("Syncing with server...");
            pdia.show();
        }

        @Override
        protected Integer doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            HashMap<String, String> postDataParams;
            postDataParams = new HashMap<String, String>();
            postDataParams.put("HTTP_ACCEPT", "application/json");
            postDataParams.put("in_time", in_time);
            postDataParams.put("in_id", in_id);
            postDataParams.put("company_id", company_id);

            HttpConnectionService service = new HttpConnectionService();
            String response = service.sendRequest(apiPath, postDataParams);
            try {
                JSONObject resultJsonObject = new JSONObject(response);
                return (int)(resultJsonObject.get("responseCode"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return 1;
        }
        // TODO: register the new account here

        @Override
        protected void onPostExecute(final Integer responseCode) {
            checkinEntry = null;
            pdia.dismiss();

            if(responseCode==0){
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Data synced with server", Snackbar.LENGTH_LONG);
                snackbar.show();
            }
            else {
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Server error. Please try again.", Snackbar.LENGTH_INDEFINITE);
                snackbar.show();
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

        PrintBilltask(Context context) {
            this.context = context;
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
            btService.mService.sendMessage(current_timestamp, "GBK");

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
