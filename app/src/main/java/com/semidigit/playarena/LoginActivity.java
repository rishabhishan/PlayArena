package com.semidigit.playarena;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class LoginActivity extends AppCompatActivity {
    String username, password, company_id;
    EditText et_username, et_password, et_company_id;
    Button bt_login;
    private LoginActivity.UserLoginTask mAuthTask = null;
    private String apiPath = "http://semidigit.com/boom/logincheck.php";
    private JSONArray restulJsonArray;
    private int success = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        initViews();
    }


    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
    // Initiate Views
    private void initViews() {
        username = PreferenceManager.getDefaultSharedPreferences(this).getString("username", "");
        if (username!=""){
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        et_username = (EditText) findViewById(R.id.et_username);
        et_password = (EditText) findViewById(R.id.et_password);
        et_company_id = (EditText) findViewById(R.id.et_company_id);
        bt_login = (Button) findViewById(R.id.btnLogin);
    }


    public void login(View v){

        username = et_username.getText().toString();
        password = et_password.getText().toString();
        company_id = et_company_id.getText().toString();

        if (validate() == false){
            return;
        }
        mAuthTask = new LoginActivity.UserLoginTask(this, username, password, company_id);
        mAuthTask.execute((Void) null);

    }

    public boolean validate() {
        boolean valid = true;
        // Check for both field is empty or not
        if (username.equals("") || username.length() == 0
                || password.equals("") || password.length() == 0 || company_id.equals("") || company_id.length() == 0) {
            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Input all fields", Snackbar.LENGTH_LONG);
            snackbar.show();
            valid=false;
        }
        return valid;
    }


    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        String response = "";
        HashMap<String, String> postDataParams;
        String username;
        String password;
        String company_id;
        Context context;
        private ProgressDialog pdia;

        UserLoginTask(Context context, String username, String password, String company_id) {
            this.username = username;
            this.password = password;
            this.company_id = company_id;
            this.context = context;
        }

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            pdia = new ProgressDialog(context);
            pdia.setMessage("Authenticating...");
            pdia.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            postDataParams = new HashMap<String, String>();
            postDataParams.put("HTTP_ACCEPT", "application/json");
            postDataParams.put("username", username);
            postDataParams.put("password", password);
            postDataParams.put("company_id", company_id);
            HttpConnectionService service = new HttpConnectionService();
            response = service.sendRequest(apiPath, postDataParams);
            try {
                JSONObject resultJsonObject = new JSONObject(response);
                if ((int)(resultJsonObject.get("success")) == 1)
                    return true;
                else
                    return false;
            } catch (JSONException e) {
                success = 0;
                e.printStackTrace();
            }
            return null;


        }
            // TODO: register the new account here

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            pdia.dismiss();
            if (success){
                PreferenceManager.getDefaultSharedPreferences(context).edit().putString("username", username).apply();
                PreferenceManager.getDefaultSharedPreferences(context).edit().putString("company_id", company_id).apply();
                Intent intent = new Intent(context, MainActivity.class);
                startActivity(intent);
                finish();
                return;
            }
            else {
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Invalid credentials. Try again!", Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
        }

    }
}
