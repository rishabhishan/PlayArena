package com.semidigit.playarena;

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
import android.view.View;
import android.widget.TextView;

import com.semidigit.playarena.Utils.BTService;
import com.semidigit.playarena.Utils.Constants;
import com.semidigit.playarena.Utils.HttpConnectionService;
import com.semidigit.playarena.Utils.UtilityMethods;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import static com.semidigit.playarena.Utils.Constants.ALIGN_CENTER;
import static com.semidigit.playarena.Utils.Constants.ALIGN_LEFT;
import static com.semidigit.playarena.Utils.Constants.RESET_PRINTER;
import static com.semidigit.playarena.Utils.Constants.bb;
import static com.semidigit.playarena.Utils.Constants.bb2;
import static com.semidigit.playarena.Utils.Constants.cc;

public class LostTicket extends AppCompatActivity {

    BTService btService;
    UtilityMethods utilityMethods;
    int actual_amount;
    FloatingActionButton fab_bicycle, fab_bike, fab_car, fab_van, fab_bus;
    TextView tv_totalAmount, tv_vehicleType, tv_penalty;

    String vehicleType, company_id;
    private static final String ACTIVITY_LOG_TAG = ".LostTicket";
    private LostTicket.CalculateBillTask calculateBillTask = null;
    private LostTicket.PrintBilltask printBillTask = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lost_ticket);
        tv_totalAmount = findViewById(R.id.tv_totalAmount);
        tv_vehicleType = findViewById(R.id.tv_vehicle_type);
        tv_penalty = findViewById(R.id.tv_penalty);
        fab_bicycle = findViewById(R.id.fab_bicycle);
        fab_bike = findViewById(R.id.fab_bike);
        fab_car = findViewById(R.id.fab_car);
        fab_van = findViewById(R.id.fab_van);
        fab_bus = findViewById(R.id.fab_bus);
        company_id = PreferenceManager.getDefaultSharedPreferences(this).getString("company_id", "");

        btService = BTService.getInstance();
        findViewById(R.id.btnCashCollect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                processTicket();
            }
        });
        fab_bike.performClick();
    }


    public void processTicket(){
        printBillTask = new LostTicket.PrintBilltask(this, vehicleType, String.valueOf(actual_amount));
        printBillTask.execute((Void) null);
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

            case R.id.fab_van:
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
                responseCode = (int) (resultJsonObject.get("responseCode"));
                actual_amount = (int) (resultJsonObject.get("total_amount"));
                tv_totalAmount.setText(String.format(Constants.TOTAL_AMOUNT_FORMAT, actual_amount));
                tv_penalty.setText(String.format(Constants.TOTAL_AMOUNT_FORMAT, actual_amount));
            } catch (JSONException e) {
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Something went wrong. Try again!", Snackbar.LENGTH_LONG);
                snackbar.show();
            }
            if (responseCode == 1) {
                String errorMsg = null;
                try {
                    errorMsg = resultJsonObject.get("errorMessage").toString();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Something went wrong. Try again!" + errorMsg, Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        }
    }

    class PrintBilltask extends AsyncTask<Void, Void, Boolean> {

        Context context;
        private ProgressDialog pdia;
        String vehicle_type, total_amount;

        PrintBilltask(Context context, String vehicle_type, String total_amount) {
            this.context = context;
            this.vehicle_type = vehicle_type;
            this.total_amount = total_amount;
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
