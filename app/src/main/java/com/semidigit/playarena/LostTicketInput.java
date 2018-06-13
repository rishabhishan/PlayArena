package com.semidigit.playarena;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.semidigit.playarena.Utils.Constants;
import com.semidigit.playarena.Utils.HttpConnectionService;
import com.semidigit.playarena.Utils.UtilityMethods;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class LostTicketInput extends AppCompatActivity{

    EditText et1, et2, et3, et4;
    String vehicle_number="", company_id;
    CalculateBillTask calculateBillTask = null;
    UtilityMethods utilityMethods;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lost_ticket_input);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        utilityMethods =  new UtilityMethods(this);
        company_id = PreferenceManager.getDefaultSharedPreferences(this).getString("company_id", "");
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

    public void proceed(View v){
        vehicle_number = et1.getText().toString() + et2.getText().toString() + et3.getText().toString() + et4.getText().toString();
        calculateBillTask = new CalculateBillTask(this, vehicle_number, company_id);
        calculateBillTask.execute((Void) null);
    }

    class CalculateBillTask extends AsyncTask<Void, Void, JSONObject> {
        Context context;
        private ProgressDialog pdia;
        String minutes, company_id, vehicle_number;

        CalculateBillTask(Context context, String vehicle_number, String company_id) {
            this.context = context;
            this.vehicle_number = vehicle_number;
            this.company_id = company_id;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pdia = new ProgressDialog(context);
            pdia.setMessage("Enquiring ...");
            if (!((Activity) context).isFinishing()) {
                pdia.show();
            }
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            HashMap<String, String> postDataParams;
            postDataParams = new HashMap<String, String>();
            postDataParams.put("HTTP_ACCEPT", "application/json");
            postDataParams.put("vehicle_no", vehicle_number);
            postDataParams.put("vehicle_type", "BIKE");
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
                if (responseCode==0){
                    try{
                        String in_time = utilityMethods.getValueOrDefaultString(resultJsonObject.get("in_time"), "");
                        Intent intent = new Intent(LostTicketInput.this, TicketResultActivity.class);
                        intent.putExtra("checkin_time", in_time);
                        startActivity(intent);
                        finish();
                    }catch (JSONException e) {
                        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Unexpected response. Try again", Snackbar.LENGTH_LONG);
                        snackbar.show();
                    }
                }
                else{
                    Intent intent = new Intent(LostTicketInput.this, LostTicket.class);
                    intent.putExtra("vehicle_number", vehicle_number);
                    startActivity(intent);

                }

            }
        }
    }


}
