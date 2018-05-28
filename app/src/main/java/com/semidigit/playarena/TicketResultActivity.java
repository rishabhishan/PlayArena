package com.semidigit.playarena;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hoin.btsdk.BluetoothService;
import com.semidigit.playarena.Utils.BTService;
import com.semidigit.playarena.Utils.Constants;
import com.semidigit.playarena.Utils.HttpConnectionService;
import com.semidigit.playarena.Utils.UtilityMethods;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;

import static com.semidigit.playarena.Utils.Constants.ALIGN_CENTER;
import static com.semidigit.playarena.Utils.Constants.ALIGN_LEFT;
import static com.semidigit.playarena.Utils.Constants.RESET_PRINTER;
import static com.semidigit.playarena.Utils.Constants.bb;
import static com.semidigit.playarena.Utils.Constants.bb2;
import static com.semidigit.playarena.Utils.Constants.cc;


public class TicketResultActivity extends AppCompatActivity {

    TextView tv_txtError, tv_checkinTime, tv_checkOutTime, tv_totalAmount, tv_totalTime, tv_discount;
    LinearLayout ll_ticketView;
    FloatingActionButton fab_bicycle, fab_bike, fab_car, fab_van, fab_bus;
    EditText et_discount_percent, et_discount_rs, et_remarks;

    String checkin_time, checkout_time, checkout_user_id, company_id, vehicleType;
    long diffHours=0, diffMins=0, actual_amount=0, final_amount=0, discount=0;

    Date dateStart, dateEnd;

    private CheckOutEntry checkOutEntry = null;
    private PrintBilltask printBillTask = null;
    private CalculateBillTask calculateBillTask = null;

    private static final String ACTIVITY_LOG_TAG = ".TicketResultActivity";

    UtilityMethods utilityMethods;
    BTService btService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_result);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        btService = BTService.getInstance();

        checkout_user_id = PreferenceManager.getDefaultSharedPreferences(this).getString("username", "");
        company_id = PreferenceManager.getDefaultSharedPreferences(this).getString("company_id", "");

        checkin_time = getIntent().getStringExtra("checkin_time");

        tv_checkinTime = findViewById(R.id.tv_checkinTime);
        tv_checkOutTime = findViewById(R.id.tv_checkOutTime);
        tv_totalAmount = findViewById(R.id.tv_totalAmount);
        tv_discount = findViewById(R.id.tv_discount);

        ll_ticketView = findViewById(R.id.ll_ticketView);
        tv_totalTime = findViewById(R.id.tv_totalTime);
        fab_bicycle = findViewById(R.id.fab_bicycle);
        fab_bike = findViewById(R.id.fab_bike);
        fab_car = findViewById(R.id.fab_car);
        fab_van = findViewById(R.id.fab_van);
        fab_bus = findViewById(R.id.fab_bus);
        et_discount_percent = findViewById(R.id.et_discount_percent);
        et_discount_rs = findViewById(R.id.et_discount_rs);
        et_remarks = findViewById(R.id.et_remarks);


        et_discount_percent.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String text = v.getText().toString();
                    long discount = calculateDiscountFromPercentage(Integer.valueOf(text));
                    et_discount_rs.setText(String.valueOf(discount));
                    tv_totalAmount.setText(String.format(Constants.TOTAL_AMOUNT_FORMAT,final_amount));
                    tv_discount.setText(String.format(Constants.DISCOUNT_FORMAT,discount));
                    return true;
                }
                return false;
            }
        });

        et_discount_rs.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String text = v.getText().toString();
                    long discount_percent = calculateDiscountFromRs(Integer.valueOf(text));
                    et_discount_percent.setText(String.valueOf(discount_percent));
                    tv_totalAmount.setText(String.format(Constants.TOTAL_AMOUNT_FORMAT,final_amount));
                    tv_discount.setText(String.format(Constants.DISCOUNT_FORMAT,discount));
                    return true;
                }
                return false;
            }
        });

        utilityMethods = new UtilityMethods(this);

        generateTicketUI(checkin_time);
        fab_bike.performClick();
    }


    public long calculateDiscountFromPercentage(int percentage){
        discount = (actual_amount*percentage)/100;
        final_amount=actual_amount-discount;
        return discount;
    }

    public long calculateDiscountFromRs(int rs){
        long discount_percent = (rs*100)/actual_amount;
        discount=rs;
        final_amount=actual_amount-discount;
        return discount_percent;
    }

    public void setVehicleType(View v){
        switch (v.getId()){
            case R.id.fab_bicycle:
                fab_bicycle.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
                fab_bike.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                fab_car.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                fab_van.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                fab_bus.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                vehicleType="BICYCLE";
                calculateBillTask = new CalculateBillTask(this, diffMins, vehicleType, company_id);
                calculateBillTask.execute((Void) null);
                break;

            case R.id.fab_bike:
                fab_bicycle.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                fab_bike.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                fab_car.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                fab_van.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                fab_bus.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                vehicleType="BIKE";
                calculateBillTask = new CalculateBillTask(this, diffMins, vehicleType, company_id);
                calculateBillTask.execute((Void) null);
                break;

            case R.id.fab_car:
                fab_bicycle.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                fab_bike.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                fab_car.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.btn_color_pressed)));
                fab_van.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                fab_bus.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                vehicleType="CAR";
                calculateBillTask = new CalculateBillTask(this, diffMins, vehicleType, company_id);
                calculateBillTask.execute((Void) null);
                break;

            case R.id.fab_van:
                fab_bicycle.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                fab_bike.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                fab_car.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                fab_van.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccentSecondary)));
                fab_bus.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                vehicleType="VAN";
                calculateBillTask = new CalculateBillTask(this, diffMins, vehicleType, company_id);
                calculateBillTask.execute((Void) null);
                break;

            case R.id.fab_bus:
                fab_bicycle.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                fab_bike.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                fab_car.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                fab_van.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                fab_bus.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimaryDark)));
                vehicleType="CAR";
                calculateBillTask = new CalculateBillTask(this, diffMins, vehicleType, company_id);
                calculateBillTask.execute((Void) null);
                break;
        }
    }



    public void generateTicketUI(String checkin_time){
        Log.d(ACTIVITY_LOG_TAG,"Checkin time :" +String.valueOf(checkin_time));

        if (TextUtils.isEmpty(checkin_time)) {
            Toast.makeText(getApplicationContext(), "Barcode is empty!", Toast.LENGTH_LONG).show();
            finish();
        }

        dateStart =  new Date();
        dateStart.setTime(Long.parseLong(checkin_time));
        dateEnd = new Date();
        checkout_time=String.valueOf(dateEnd.getTime());
        tv_checkinTime.setText(utilityMethods.displayDate(dateStart));
        tv_checkOutTime.setText(utilityMethods.displayDate(dateEnd));


        long diff = dateEnd.getTime() - dateStart.getTime();
        diffMins = diff / (60 * 1000);
        diffHours = diff / (60 * 60 * 1000);

        Log.d(ACTIVITY_LOG_TAG,"DIFF :" +String.valueOf(diff));
        Log.d(ACTIVITY_LOG_TAG,"DIFF MINS:" +String.valueOf(diffMins));
        Log.d(ACTIVITY_LOG_TAG,"DIFF HOURS:" +String.valueOf(diffHours));
        Log.d(ACTIVITY_LOG_TAG,"DIFF TOTAL:" +String.valueOf(actual_amount));

        tv_totalTime.setText(String.format(Constants.TOTAL_TIME_FORMAT, diffHours, diffMins%60));
    }

    public void saveInOut(View view){
        if(btService.mService.getState()== BluetoothService.STATE_CONNECTED){
            printBillTask = new PrintBilltask(this, utilityMethods.displayDate(dateStart), utilityMethods.displayDate(dateEnd), String.format(Constants.TOTAL_TIME_FORMAT, diffHours, diffMins),String.format(Constants.TOTAL_AMOUNT_FORMAT, final_amount));
            printBillTask.execute((Void) null);
            checkOutEntry = new CheckOutEntry(this, checkin_time, checkout_time,checkout_user_id, String.valueOf(diffMins),vehicleType, String.valueOf(actual_amount), String.valueOf(discount), String.valueOf(final_amount), et_remarks.getText().toString(), company_id );
            checkOutEntry.execute((Void) null);
        }else{
            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Error connecting to printer. Try restarting the application", Snackbar.LENGTH_INDEFINITE);
            snackbar.show();
        }
    }


    class CalculateBillTask extends AsyncTask<Void, Void, JSONObject> {
        Context context;
        private ProgressDialog pdia;
        String minutes, company_id,vehicle_type;

        CalculateBillTask(Context context, long minutes, String vehicle_type, String company_id) {
            this.context = context;
            this.minutes = String.valueOf(minutes);
            this.vehicle_type=vehicle_type;
            this.company_id = company_id;
        }

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            pdia = new ProgressDialog(context);
            pdia.setMessage("Calculating bill ...");
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
            postDataParams.put("minutes", minutes);
            postDataParams.put("vehicle_type", vehicle_type);
            postDataParams.put("company_id", company_id);

            HttpConnectionService service = new HttpConnectionService();
            String response = service.sendRequest(String.format(Constants.CALCULATE_BILL_API_PATH,company_id), postDataParams);
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
            if(pdia!=null)
                pdia.dismiss();
            int responseCode=1;

            try {
                responseCode = (int)(resultJsonObject.get("responseCode"));
                actual_amount = (int)(resultJsonObject.get("total_amount"));
                final_amount=actual_amount-discount;
                tv_totalAmount.setText(String.format(Constants.TOTAL_AMOUNT_FORMAT, actual_amount));
            } catch (JSONException e) {
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Something went wrong. Try again!", Snackbar.LENGTH_LONG);
                snackbar.show();
            }
            if (responseCode==1){
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Something went wrong. Try again!", Snackbar.LENGTH_LONG);
                snackbar.show();
            }

        }

        @Override
        protected void onCancelled() {
            calculateBillTask = null;
            if(pdia!=null)
                pdia.dismiss();
        }
    }


    class CheckOutEntry extends AsyncTask<Void, Void, Integer> {
        Context context;
        private ProgressDialog pdia;
        String in_time, out_time, out_id, total_time, vehicle_type, actual_amount, discount, final_amount, remarks, company_id;

        public CheckOutEntry(Context context, String in_time, String out_time, String out_id, String total_time, String vehicle_type, String actual_amount, String discount, String final_amount, String remarks, String company_id) {
            this.context = context;
            this.in_time = in_time;
            this.out_time = out_time;
            this.out_id = out_id;
            this.total_time = total_time;
            this.actual_amount = actual_amount;
            this.discount = discount;
            this.final_amount = final_amount;
            this.remarks = remarks;
            this.company_id = company_id;
            this.vehicle_type=vehicle_type;
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
        protected Integer doInBackground(Void... params) {
            HashMap<String, String> postDataParams;
            postDataParams = new HashMap<String, String>();
            postDataParams.put("HTTP_ACCEPT", "application/json");
            postDataParams.put("in_time", in_time);
            postDataParams.put("out_time", out_time);
            postDataParams.put("out_id", out_id);
            postDataParams.put("total_time", total_time);
            postDataParams.put("vehicle_type", vehicle_type);
            postDataParams.put("actual_amount", actual_amount);
            postDataParams.put("discount", discount);
            postDataParams.put("final_amount", final_amount);
            postDataParams.put("remarks", remarks);
            postDataParams.put("company_id", company_id);

            HttpConnectionService service = new HttpConnectionService();
            String response = service.sendRequest(Constants.CHECKOUT_API_PATH, postDataParams);
            try {
                JSONObject resultJsonObject = new JSONObject(response);
                return (int)(resultJsonObject.get("responseCode"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return 1;
        }

        @Override
        protected void onPostExecute(final Integer responseCode) {
            checkOutEntry = null;
            if(pdia!=null)
                pdia.dismiss();

            if(responseCode==0){
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Data synced with server", Snackbar.LENGTH_LONG);
                snackbar.show();
                finish();
            }
            else {
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Server error. Please try again.", Snackbar.LENGTH_INDEFINITE);
                snackbar.show();
            }
        }

        @Override
        protected void onCancelled() {
            checkOutEntry = null;
            if(pdia!=null)
                pdia.dismiss();
        }
    }


    class PrintBilltask extends AsyncTask<Void, Void, Boolean> {

        Context context;
        private ProgressDialog pdia;
        String in_time, out_time, total_time, total_amount;

        PrintBilltask(Context context, String in_time, String out_time, String total_time, String total_amount) {
            this.context = context;
            this.in_time = in_time;
            this.out_time = out_time;
            this.total_time = total_time;
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
            btService.mService.sendMessage("BILL", "GBK");
            btService.mService.write(cc);
            btService.mService.sendMessage("Play, Sarjapur Road", "GBK");
            btService.mService.sendMessage("==============================", "GBK");
            btService.mService.write(RESET_PRINTER);
            btService.mService.write(ALIGN_LEFT);
            btService.mService.write(cc);
            btService.mService.sendMessage("CheckIn            "+in_time, "GBK");
            btService.mService.sendMessage("CheckOut           "+out_time, "GBK");
            btService.mService.sendMessage("Chargeable Time    "+total_time, "GBK");
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