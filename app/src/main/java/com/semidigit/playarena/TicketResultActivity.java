package com.semidigit.playarena;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;


public class TicketResultActivity extends AppCompatActivity {

    TextView txtError, checkinTime, checkOutTime, ratePerHour, totalAmount;
    LinearLayout ticketView;
    float rate = 10.0f;
    private SaveInOutTask mAuthTask = null;
    private String apiPath = "http://semidigit.com/boom/timedetail.php";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_result);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        String checkout_user_id = PreferenceManager.getDefaultSharedPreferences(this).getString("username", "");
        String company_id = PreferenceManager.getDefaultSharedPreferences(this).getString("company_id", "");
        String dateStartString = getIntent().getStringExtra("checkin_time");
        String checkin_user_id = getIntent().getStringExtra("checkin_user_id");
        txtError = findViewById(R.id.txt_error);
        checkinTime = findViewById(R.id.checkinTime);
        checkOutTime = findViewById(R.id.checkOutTime);
        ratePerHour = findViewById(R.id.ratePerHour);
        totalAmount = findViewById(R.id.totalAmount);
        ticketView = findViewById(R.id.ticketView);
        // close the activity in case of empty barcode
        if (TextUtils.isEmpty(dateStartString)) {
            showNoTicket();
            Toast.makeText(getApplicationContext(), "Barcode is empty!", Toast.LENGTH_LONG).show();
            finish();
        }
        Date dateStart =  new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd MMMM yyyy hh:mm");
        try {
            dateStart = formatter.parse(dateStartString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Date dateEnd = new Date();
        String dateEndString = formatter.format(dateEnd);
        checkinTime.setText(dateStartString);
        checkOutTime.setText(dateEndString);
        ratePerHour.setText(String.valueOf(rate));

        long diff = dateEnd.getTime() - dateStart.getTime();
        long diffHours = diff / (60 * 60 * 1000);
        float total;
        if(diffHours==0||diffHours==12){
            diffHours=1;
            total = rate;
            System.out.println("****** "+String.valueOf(diff));
            System.out.println("****** "+String.valueOf(diffHours));
            System.out.println("****** "+String.valueOf(total));
        }
        else {
            total = diffHours*rate;
            System.out.println("****** "+String.valueOf(diff));
            System.out.println("****** "+String.valueOf(diffHours));
            System.out.println("****** "+String.valueOf(total));
        }


        totalAmount.setText(String.valueOf(total));

        mAuthTask = new SaveInOutTask(this, company_id, checkin_user_id, dateStartString, checkout_user_id, dateEndString, String.valueOf(diffHours), String.valueOf(total));
        mAuthTask.execute((Void) null);
    }

    private void showNoTicket() {
        txtError.setVisibility(View.VISIBLE);
        ticketView.setVisibility(View.GONE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    class SaveInOutTask extends AsyncTask<Void, Void, Boolean> {

        String response = "";
        HashMap<String, String> postDataParams;
        String username;
        String company_id;
        String in_time;
        String in_id;
        String out_time;
        String out_id;
        String total_time;
        String total_amount;

        Context context;
        private ProgressDialog pdia;

        SaveInOutTask(Context context, String company_id, String in_id, String in_time, String out_id,  String out_time, String total_time, String total_amount) {
            this.context = context;
            this.username = username;
            this.company_id = company_id;
            this.in_id = in_id;
            this.in_time = in_time;
            this.out_id = out_id;
            this.out_time = out_time;

            this.total_amount = total_amount;
            this.total_time = total_time;



        }

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            pdia = new ProgressDialog(context);
            pdia.setMessage("Syncing with server ...");
            pdia.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            postDataParams = new HashMap<String, String>();
            postDataParams.put("HTTP_ACCEPT", "application/json");
            postDataParams.put("in_time", in_time);
            postDataParams.put("in_id", in_id);
            postDataParams.put("out_time", out_time);
            postDataParams.put("out_id", out_id);
            postDataParams.put("total_time", total_time);
            postDataParams.put("total_amount", total_amount);
            postDataParams.put("company_id", company_id);

            HttpConnectionService service = new HttpConnectionService();
            response = service.sendRequest(apiPath, postDataParams);
            return null;


        }
        // TODO: register the new account here

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            pdia.dismiss();
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
        }

    }

}