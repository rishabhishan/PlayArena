package com.semidigit.v2;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.hoin.btsdk.BluetoothService;
import com.semidigit.v2.Utils.BTService;
import com.semidigit.v2.Utils.Constants;
import com.semidigit.v2.Utils.HttpConnectionService;
import com.semidigit.v2.Utils.UtilityMethods;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;

import static com.semidigit.v2.Utils.Constants.ALIGN_CENTER;
import static com.semidigit.v2.Utils.Constants.ALIGN_LEFT;
import static com.semidigit.v2.Utils.Constants.RESET_PRINTER;
import static com.semidigit.v2.Utils.Constants.bb;
import static com.semidigit.v2.Utils.Constants.bb2;
import static com.semidigit.v2.Utils.Constants.cc;

public class LostTicket extends AppCompatActivity {

    BTService btService;
    UtilityMethods utilityMethods;
    int actual_amount;
    FloatingActionButton fab_bicycle, fab_bike, fab_car, fab_van, fab_bus;
    TextView tv_totalAmount, tv_vehicleType, tv_penalty;
    EditText et_remarks;

    String vehicleType, company_id, checkout_user_id;
    private static final String ACTIVITY_LOG_TAG = ".LostTicket";
    private LostTicket.CalculateBillTask calculateBillTask = null;
    private LostTicket.PrintBilltask printBillTask = null;
    private LostTicket.LostTicketEntryTask lostTicketEntryTask = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lost_ticket);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        tv_totalAmount = findViewById(R.id.tv_totalAmount);
        tv_vehicleType = findViewById(R.id.tv_vehicle_type);
        tv_penalty = findViewById(R.id.tv_penalty);
        fab_bicycle = findViewById(R.id.fab_bicycle);
        fab_bike = findViewById(R.id.fab_bike);
        fab_car = findViewById(R.id.fab_car);
        fab_van = findViewById(R.id.fab_valet);
        fab_bus = findViewById(R.id.fab_bus);
        et_remarks=findViewById(R.id.et_remarks);
        company_id = PreferenceManager.getDefaultSharedPreferences(this).getString("company_id", "");
        checkout_user_id = PreferenceManager.getDefaultSharedPreferences(this).getString("username", "");

        btService = BTService.getInstance();
        utilityMethods =  new UtilityMethods(this);
        findViewById(R.id.btnCashCollect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                processTicket();
            }
        });
        fab_bike.performClick();
    }

    public void processTicket(){
        Snackbar snackbar;
        Date dateCur = new Date();
        if(btService.mService!=null) {
            if (btService.mService.getState() == BluetoothService.STATE_CONNECTED) {
                lostTicketEntryTask = new LostTicket.LostTicketEntryTask(this, String.valueOf(dateCur.getTime()), checkout_user_id, vehicleType, String.valueOf(actual_amount), et_remarks.getText().toString(), company_id);
                lostTicketEntryTask.execute((Void) null);
            } else {
                snackbar = Snackbar.make(findViewById(android.R.id.content), "Error connecting to printer. Try restarting the application", Snackbar.LENGTH_INDEFINITE);
                snackbar.show();
            }
        }
        else {
            snackbar = Snackbar.make(findViewById(android.R.id.content), "Error connecting to printer. Try restarting the application", Snackbar.LENGTH_INDEFINITE);
            snackbar.show();
        }
    }

    public void setVehicleType(View v) {
        switch (v.getId()) {
            case R.id.fab_bicycle:
                fab_bicycle.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
                fab_bike.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                fab_car.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                fab_van.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                fab_bus.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                vehicleType = "BICYCLE";
                calculateBillTask = new LostTicket.CalculateBillTask(this, vehicleType, company_id);
                calculateBillTask.execute((Void) null);
                break;

            case R.id.fab_bike:
                fab_bicycle.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                fab_bike.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
                fab_car.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                fab_van.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                fab_bus.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                vehicleType = "BIKE";
                calculateBillTask = new LostTicket.CalculateBillTask(this, vehicleType, company_id);
                calculateBillTask.execute((Void) null);
                break;

            case R.id.fab_car:
                fab_bicycle.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                fab_bike.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                fab_car.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
                fab_van.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                fab_bus.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                vehicleType = "CAR";
                calculateBillTask = new LostTicket.CalculateBillTask(this, vehicleType, company_id);
                calculateBillTask.execute((Void) null);
                break;

            case R.id.fab_valet:
                fab_bicycle.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                fab_bike.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                fab_car.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                fab_van.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
                fab_bus.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                vehicleType = "VAN";
                calculateBillTask = new LostTicket.CalculateBillTask(this, vehicleType, company_id);
                calculateBillTask.execute((Void) null);
                break;

            case R.id.fab_bus:
                fab_bicycle.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                fab_bike.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                fab_car.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                fab_van.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                fab_bus.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
                vehicleType = "BUS";
                calculateBillTask = new LostTicket.CalculateBillTask(this, vehicleType, company_id);
                calculateBillTask.execute((Void) null);
                break;
        }
    }

    class LostTicketEntryTask extends AsyncTask<Void, Void, JSONObject> {
        Context context;
        private ProgressDialog pdia;
        String in_time, out_time, out_id, total_time, vehicle_type, actual_amount, discount, final_amount, remarks, company_id;

        public LostTicketEntryTask(Context context, String out_time, String out_id, String vehicle_type, String final_amount, String remarks, String company_id) {
            this.context = context;
            this.out_time = out_time;
            this.out_id = out_id;
            this.vehicle_type=vehicle_type;
            this.final_amount = final_amount;
            this.remarks = remarks;
            this.company_id = company_id;
        }


        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            pdia = new ProgressDialog(context);
            pdia.setMessage("Syncing with server...");
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
            postDataParams.put("out_time", out_time);
            postDataParams.put("out_id", out_id);
            postDataParams.put("vehicle_type", vehicle_type);
            postDataParams.put("final_amount", final_amount);
            postDataParams.put("remarks", remarks);
            postDataParams.put("company_id", company_id);

            HttpConnectionService service = new HttpConnectionService();
            String response = service.sendRequest(Constants.LOST_TICKET_ENTRY_API_PATH, postDataParams);
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
            lostTicketEntryTask = null;
            if (pdia != null)
                pdia.dismiss();
            int responseCode = 1;
            try {
                responseCode = utilityMethods.getValueOrDefaultInt(resultJsonObject.get("responseCode"), 1);
            } catch (JSONException e) {
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Unexpected response. Try again", Snackbar.LENGTH_LONG);
                snackbar.show();
            }

            if (responseCode == 1) {
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Task Failed. Something went wrong. Try again!", Snackbar.LENGTH_LONG);
                snackbar.show();
            } else {
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Data synced with server", Snackbar.LENGTH_SHORT);
                snackbar.show();
                try {
                    String invoice_id = utilityMethods.getValueOrDefaultString(resultJsonObject.get("invoice_id"), "NA");
                    printBillTask = new LostTicket.PrintBilltask(context, invoice_id, vehicleType, String.valueOf(actual_amount));
                    printBillTask.execute((Void) null);

                } catch (JSONException e) {
                    snackbar = Snackbar.make(findViewById(android.R.id.content), "Unexpected response. Try again", Snackbar.LENGTH_LONG);
                    snackbar.show();
                }

            }
        }

        @Override
        protected void onCancelled() {
            lostTicketEntryTask = null;
            if(pdia!=null)
                pdia.dismiss();
        }
    }


    class CalculateBillTask extends AsyncTask<Void, Void, JSONObject> {
        Context context;
        private ProgressDialog pdia;
        String minutes, company_id, vehicle_type;

        CalculateBillTask(Context context, String vehicle_type, String company_id) {
            this.context = context;
            this.vehicle_type = vehicle_type;
            this.company_id = company_id;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pdia = new ProgressDialog(context);
            pdia.setMessage("Calculating bill ...");
            if (!((Activity) context).isFinishing()) {
                pdia.show();
            }
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            HashMap<String, String> postDataParams;
            postDataParams = new HashMap<String, String>();
            postDataParams.put("HTTP_ACCEPT", "application/json");
            postDataParams.put("vehicle_type", vehicle_type);
            postDataParams.put("vehicle_no", "");
            postDataParams.put("company_id", company_id);

            HttpConnectionService service = new HttpConnectionService();
            String response = service.sendRequest(String.format(Constants.LOST_TICKET_API_PATH, company_id), postDataParams);
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
            calculateBillTask = null;
            if (pdia != null)
                pdia.dismiss();
            int responseCode = 1;
            tv_vehicleType.setText(vehicle_type);
            try {
                responseCode = utilityMethods.getValueOrDefaultInt(resultJsonObject.get("entryFound"),1);
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
                    actual_amount = utilityMethods.getValueOrDefaultInt(resultJsonObject.get("total_amount"), 0);
                    tv_totalAmount.setText(String.format(Constants.TOTAL_AMOUNT_FORMAT, actual_amount));
                    tv_penalty.setText(String.format(Constants.TOTAL_AMOUNT_FORMAT, actual_amount));

                } catch (JSONException e) {
                    Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Unexpected response. Try again", Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
            }
        }
    }

    class PrintBilltask extends AsyncTask<Void, Void, Boolean> {

        Context context;
        private ProgressDialog pdia;
        String vehicle_type, total_amount, invoice_no;

        PrintBilltask(Context context, String invoice_no, String vehicle_type, String total_amount) {
            this.context = context;
            this.vehicle_type = vehicle_type;
            this.total_amount = total_amount;
            this.invoice_no=invoice_no;
        }

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            pdia = new ProgressDialog(context);
            pdia.setMessage("Printing Ticket ...");
            if(!((Activity) context).isFinishing())
            {
                pdia.show();
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            btService.mService.write(RESET_PRINTER);
            btService.mService.write(bb2);
            btService.mService.write(ALIGN_CENTER);
            btService.mService.sendMessage("LOST TICKET RECEIPT", "GBK");
            btService.mService.write(cc);
            btService.mService.sendMessage("Play, Sarjapur Road", "GBK");
            btService.mService.sendMessage("==============================", "GBK");
            btService.mService.write(RESET_PRINTER);
            btService.mService.write(ALIGN_LEFT);
            btService.mService.write(cc);
            btService.mService.sendMessage("Invoice #            "+invoice_no, "GBK");
            btService.mService.sendMessage("Vehicle Type            "+vehicle_type, "GBK");
            btService.mService.sendMessage("Penalty           "+total_amount, "GBK");
            btService.mService.sendMessage("==============================", "GBK");
            btService.mService.write(bb);
            btService.mService.sendMessage("Total              "+total_amount, "GBK");
            btService.mService.sendMessage("\n", "GBK");
            btService.mService.write(ALIGN_CENTER);
            btService.mService.write(cc);
            btService.mService.sendMessage(Constants.FOOTER_MSG_RECEIPT, "GBK");
            btService.mService.write(RESET_PRINTER);
            btService.mService.sendMessage("\n", "GBK");
            btService.mService.sendMessage("\n", "GBK");
            return null;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            printBillTask = null;
            if(pdia!=null)
                pdia.dismiss();
        }

        @Override
        protected void onCancelled() {
            printBillTask = null;
            if(pdia!=null)
                pdia.dismiss();
        }

    }

}
