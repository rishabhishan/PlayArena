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
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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


public class TicketResultActivity extends AppCompatActivity {

    TextView tv_txtError, tv_checkinTime, tv_checkOutTime, tv_totalAmount, tv_totalTime, tv_discount;
    LinearLayout ll_ticketView;
    FloatingActionButton fab_bicycle, fab_bike, fab_car, fab_valet, fab_hmv;
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
        fab_valet = findViewById(R.id.fab_valet);
        fab_hmv = findViewById(R.id.fab_hmv);
        et_discount_percent = findViewById(R.id.et_discount_percent);
        et_discount_rs = findViewById(R.id.et_discount_rs);
        et_remarks = findViewById(R.id.et_remarks);


        et_discount_percent.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String text = v.getText().toString();
                    if (Integer.valueOf(text)>100){
                        Toast.makeText(getApplicationContext(),"Percentage can`t be greater than 100", Toast.LENGTH_SHORT).show();
                        v.setText("");
                        return true;
                    }
                    else if(text.length()>0){
                        long discount = calculateDiscountFromPercentage(Integer.valueOf(text));
                        et_discount_rs.setText(String.valueOf(discount));
                        tv_totalAmount.setText(String.format(Constants.TOTAL_AMOUNT_FORMAT,final_amount));
                        tv_discount.setText(String.format(Constants.DISCOUNT_FORMAT,discount));
                        return true;
                    }
                }
                return false;
            }
        });

        et_discount_rs.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String text = v.getText().toString();
                    if(Integer.valueOf(text)>actual_amount){
                        Toast.makeText(getApplicationContext(),"Discount can`t be greater than actual amount", Toast.LENGTH_SHORT).show();
                        v.setText("");
                        return true;
                    }else if(text.length()>0){
                        long discount_percent = calculateDiscountFromRs(Integer.valueOf(text));
                        et_discount_percent.setText(String.valueOf(discount_percent));
                        tv_totalAmount.setText(String.format(Constants.TOTAL_AMOUNT_FORMAT,final_amount));
                        tv_discount.setText(String.format(Constants.DISCOUNT_FORMAT,discount));
                        return true;
                    }
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
                fab_bike.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                fab_car.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                fab_valet.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                fab_hmv.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                vehicleType="BICYCLE";
                calculateBillTask = new CalculateBillTask(this, diffMins, vehicleType, company_id);
                calculateBillTask.execute((Void) null);
                break;

            case R.id.fab_bike:
                fab_bicycle.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                fab_bike.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
                fab_car.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                fab_valet.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                fab_hmv.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                vehicleType="BIKE";
                calculateBillTask = new CalculateBillTask(this, diffMins, vehicleType, company_id);
                calculateBillTask.execute((Void) null);
                break;

            case R.id.fab_car:
                fab_bicycle.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                fab_bike.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                fab_car.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
                fab_valet.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                fab_hmv.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                vehicleType="CAR";
                calculateBillTask = new CalculateBillTask(this, diffMins, vehicleType, company_id);
                calculateBillTask.execute((Void) null);
                break;

            case R.id.fab_hmv:
                fab_bicycle.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                fab_bike.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                fab_car.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                fab_valet.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                fab_hmv.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
                vehicleType="HMV";
                calculateBillTask = new CalculateBillTask(this, diffMins, vehicleType, company_id);
                calculateBillTask.execute((Void) null);
                break;

            case R.id.fab_valet:
                fab_bicycle.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                fab_bike.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                fab_car.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                fab_valet.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
                fab_hmv.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                vehicleType="VALET";
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
        checkOutEntry = new CheckOutEntry(this, checkin_time, checkout_time,checkout_user_id, String.valueOf(diffMins),vehicleType, String.valueOf(actual_amount), String.valueOf(discount), String.valueOf(final_amount), et_remarks.getText().toString(), company_id );
        checkOutEntry.execute((Void) null);
        if(btService.mService!=null) {
            if(btService.mService.getState()== BluetoothService.STATE_CONNECTED){
                checkOutEntry = new CheckOutEntry(this, checkin_time, checkout_time,checkout_user_id, String.valueOf(diffMins),vehicleType, String.valueOf(actual_amount), String.valueOf(discount), String.valueOf(final_amount), et_remarks.getText().toString(), company_id );
                checkOutEntry.execute((Void) null);
            }else{
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Error connecting to printer. Try restarting the application", Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        }
        else {
            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Error connecting to printer. Try restarting the application", Snackbar.LENGTH_LONG);
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


    class CheckOutEntry extends AsyncTask<Void, Void, JSONObject> {
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
        protected JSONObject doInBackground(Void... params) {
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
            checkOutEntry = null;
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
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Data synced with server", Snackbar.LENGTH_LONG);
                snackbar.show();
                try {
                    String invoice_id = utilityMethods.getValueOrDefaultString(resultJsonObject.get("invoice_id"), "NA");
                    System.out.println("****** "+ String.format(Constants.TOTAL_TIME_FORMAT, diffHours, diffMins));
                    System.out.println("****** "+ String.format(Constants.TOTAL_AMOUNT_FORMAT, final_amount));
                    printBillTask = new PrintBilltask(context, invoice_id, utilityMethods.displayDate(dateStart), utilityMethods.displayDate(dateEnd), String.format(Constants.TOTAL_TIME_FORMAT, diffHours, diffMins%60),final_amount);
                    printBillTask.execute((Void) null);

                } catch (JSONException e) {
                    snackbar = Snackbar.make(findViewById(android.R.id.content), "Unexpected response. Try again", Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
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
        String invoice_id, in_time, out_time, total_time, total_amount;

        PrintBilltask(Context context, String invoice_id, String in_time, String out_time, String total_time, String total_amount) {
            this.context = context;
            this.invoice_id=invoice_id;
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
            double gst_total = Double.valueOf(total_amount)*0.18;
            double total_amnt = Double.valueOf(total_amount);
            btService.mService.write(RESET_PRINTER);
            btService.mService.write(bb2);
            btService.mService.write(ALIGN_CENTER);
            btService.mService.sendMessage("INVOICE", "GBK");
            btService.mService.write(cc);
            btService.mService.sendMessage("Play Arena Sports & Adventure Pvt Ltd", "GBK");
            btService.mService.sendMessage("#Sy 75 Kasavanahalli, Amritha College Road", "GBK");
            btService.mService.sendMessage("Off Sarjapur Rd, Bangalore-560035", "GBK");
            btService.mService.sendMessage("==============================", "GBK");
            btService.mService.write(RESET_PRINTER);
            btService.mService.write(ALIGN_LEFT);
            btService.mService.write(cc);
            btService.mService.sendMessage("GST#           29AAGCP1029B1ZS", "GBK");
            btService.mService.sendMessage("==============================", "GBK");
            btService.mService.sendMessage("Invoice #          "+invoice_id, "GBK");
            btService.mService.sendMessage("CheckIn            "+in_time, "GBK");
            btService.mService.sendMessage("CheckOut           "+out_time, "GBK");
            btService.mService.sendMessage("Chargeable Time    "+total_time, "GBK");
            btService.mService.sendMessage("Amount             "+String.format(Constants.TOTAL_AMOUNT_FORMAT, String.valueOf(total_amnt-gst_total)), "GBK");
            btService.mService.sendMessage("==============================", "GBK");
            btService.mService.sendMessage("CGST 9%            "+String.format(Constants.TOTAL_AMOUNT_FORMAT, String.valueOf(gst_total/2)), "GBK");
            btService.mService.sendMessage("SGST 9%            "+String.format(Constants.TOTAL_AMOUNT_FORMAT, String.valueOf(gst_total/2)), "GBK");
            btService.mService.sendMessage("==============================", "GBK");
            btService.mService.write(bb);
            btService.mService.sendMessage("Total              "+String.format(Constants.TOTAL_AMOUNT_FORMAT, total_amount), "GBK");
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