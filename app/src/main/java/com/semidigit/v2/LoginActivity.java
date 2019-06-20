package com.semidigit.v2;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.hoin.btsdk.BluetoothService;
import com.semidigit.v2.Utils.BTService;
import com.semidigit.v2.Utils.Constants;
import com.semidigit.v2.Utils.HttpConnectionService;
import com.semidigit.v2.Utils.UtilityMethods;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Set;

import static com.semidigit.v2.Utils.Constants.REQUEST_ENABLE_BT;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class LoginActivity extends AppCompatActivity {

    private static final String apiPath = Constants.LOGIN_API_PATH;
    private static final String ACTIVITY_LOG_TAG = ".LoginActivity";

    BTService btService;
    UtilityMethods utilityMethods;
    private LoginActivity.UserLoginTask mAuthTask = null;
    String username, password, company_id;
    ProgressDialog pd;

    EditText et_username, et_password, et_company_id;
    Button bt_login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        initViews();
        utilityMethods =  new UtilityMethods(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupBluetoothService();
    }

    private void setupBluetoothService(){
        if(pd==null){
            pd = new ProgressDialog(this);
            pd.setMessage("Setting up bluetooth...");
            pd.show();
        }
        btService=BTService.getInstance();
        if(btService.mService==null){
            btService.mService=new BluetoothService(this, mHandler);
        }
        if(!btService.mService.isAvailable()){
            pd.dismiss();
            Log.d(ACTIVITY_LOG_TAG,"Bluetooth Unavailable");
            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Device doesn`t support bluetooth service. Can`t proceed further", Snackbar.LENGTH_LONG);
            snackbar.show();
            finish();
            return;
        }
        enableBluetooth(btService);
    }

    public void enableBluetooth(BTService btService){
        if( btService.mService.isBTopen() == false){
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
        else{
            connectPrinter();
        }
    }

    public void connectPrinter(){
        if(btService.con_dev==null){
            Set<BluetoothDevice> pairedDevices = btService.mService.getPairedDev();
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    if (device.getName().equals(Constants.BLUETOOTH_PRINTER_NAME)) {
                        btService.con_dev = btService.mService.getDevByMac(device.getAddress());
                        btService.mService.connect(btService.con_dev);
                        break;
                    }
                }
            }
            if(btService.con_dev==null){
                pd.dismiss();
                Log.d(ACTIVITY_LOG_TAG,"No paired device found/Printer not paired");
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Pair your bluetooth printer and restart the application.", Snackbar.LENGTH_LONG);
                snackbar.show();
                return;
            }
        }else{
            btService.mService.connect(btService.con_dev);
            pd.dismiss();
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, "Bluetooth Enabled", Toast.LENGTH_LONG).show();
                    connectPrinter();
                    return;
                }
                else{
                    Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Enable Bluetooth to proceed", Snackbar.LENGTH_LONG);
                    snackbar.show();
                    enableBluetooth(this.btService);
                    return;
                }
        }
    }

    public final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            pd.dismiss();
            switch (msg.what) {
                case BluetoothService.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            Log.d(ACTIVITY_LOG_TAG, "Connect successful");
                            Snackbar snackbar1 = Snackbar.make(findViewById(android.R.id.content), "Connected to printer", Snackbar.LENGTH_LONG);
                            snackbar1.show();
                            check_existing_login();
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            Log.d(ACTIVITY_LOG_TAG, "Connecting");
                            Snackbar snackbar2 = Snackbar.make(findViewById(android.R.id.content), "Connecting to printer", Snackbar.LENGTH_SHORT);
                            snackbar2.show();
                            break;
                        case BluetoothService.STATE_LISTEN:
                            Log.d(ACTIVITY_LOG_TAG, "Listening");
                            //Snackbar snackbar3 = Snackbar.make(findViewById(android.R.id.content), "Trying to connect", Snackbar.LENGTH_SHORT);
                            //snackbar3.show();
                            break;
                        case BluetoothService.STATE_NONE:
                            Log.d(ACTIVITY_LOG_TAG, "None");
                            //Snackbar snackbar4 = Snackbar.make(findViewById(android.R.id.content), "Trying to connect", Snackbar.LENGTH_SHORT);
                            //snackbar4.show();
                            break;
                    }
                    break;
                case BluetoothService.MESSAGE_CONNECTION_LOST:
                    Log.d(ACTIVITY_LOG_TAG, "Device connection lost");
                    Snackbar snackbar3 = Snackbar.make(findViewById(android.R.id.content), "Device connection lost", Snackbar.LENGTH_INDEFINITE);
                    snackbar3.setAction("Try Again", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            btService.mService.connect(btService.con_dev);
                        }
                    });
                    snackbar3.show();
                    break;
                case BluetoothService.MESSAGE_UNABLE_CONNECT:
                    Log.d(ACTIVITY_LOG_TAG, "Unable to connect to device");
                    Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Unable to connect to printer", Snackbar.LENGTH_INDEFINITE);
                    snackbar.setAction("Try Again", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            btService.mService.connect(btService.con_dev);
                        }
                    });
                    snackbar.show();
                    break;
            }
        }

    };

    // Initiate Views
    private void initViews() {
        et_username = (EditText) findViewById(R.id.et_username);
        et_password = (EditText) findViewById(R.id.et_password);
        et_company_id = (EditText) findViewById(R.id.et_company_id);
        bt_login = (Button) findViewById(R.id.btnLogin);
    }

    private void check_existing_login(){
        if(pd!=null){
            pd.dismiss();
        }
        username = PreferenceManager.getDefaultSharedPreferences(this).getString("username", null);
        if (username!=null){
            password = PreferenceManager.getDefaultSharedPreferences(this).getString("password", null);
            company_id = PreferenceManager.getDefaultSharedPreferences(this).getString("company_id", null);
            mAuthTask = new LoginActivity.UserLoginTask(this, username, password, company_id, getUniqueMachineID(), true);
            mAuthTask.execute((Void) null);
            return;
        }
    }


    public void login(View v){
        username = et_username.getText().toString();
        password = et_password.getText().toString();
        company_id = et_company_id.getText().toString();

        if (validate() == false){
            return;
        }

        mAuthTask = new LoginActivity.UserLoginTask(this, username, password, company_id, getUniqueMachineID(), false);
        mAuthTask.execute((Void) null);
    }

    public boolean validate() {
        boolean valid = true;

        // Check for all fields are empty or not
        if (username.equals("") || username.length() == 0
                || password.equals("") || password.length() == 0 || company_id.equals("") || company_id.length() == 0) {
            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Input all fields", Snackbar.LENGTH_LONG);
            snackbar.show();
            valid=false;
        }
        return valid;
    }

    public String getUniqueMachineID(){
        String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        Log.d(ACTIVITY_LOG_TAG,"Device id : "+ androidId);
        return androidId;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    class UserLoginTask extends AsyncTask<Void, Void, JSONObject> {

        String username;
        String password;
        String company_id;
        String machine_id;
        boolean manual_login;
        Context context;
        ProgressDialog pdia;

        UserLoginTask(Context context, String username, String password, String company_id, String machine_id, boolean manual_login) {
            this.context = context;
            this.username = username;
            this.password = password;
            this.company_id = company_id;
            this.machine_id = machine_id;
            this.manual_login = manual_login;
        }

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            pdia = new ProgressDialog(context);
            pdia.setMessage("Authenticating...");
            if(!((Activity) context).isFinishing())
            {
                pdia.show();
            }


        }

        @Override
        protected JSONObject doInBackground(Void... params) {

            HashMap<String, String> postDataParams = new HashMap<String, String>();
            postDataParams.put("HTTP_ACCEPT", "application/json");
            postDataParams.put("username", username);
            postDataParams.put("password", password);
            postDataParams.put("company_id", company_id);
            postDataParams.put("user_type", "staff");
            postDataParams.put("machine_id", machine_id);
            if(manual_login==true){
                postDataParams.put("save_login_entry", "0");
            }
            else{
                postDataParams.put("save_login_entry", "1");
            }

            HttpConnectionService service = new HttpConnectionService();
            String response = service.sendRequest(apiPath, postDataParams);
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
            mAuthTask = null;
            pdia.dismiss();
            int responseCode=1;

            try {
                responseCode = utilityMethods.getValueOrDefaultInt(resultJsonObject.get("responseCode"),1);
            } catch (JSONException e) {
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Unexpected response. Try again", Snackbar.LENGTH_LONG);
                snackbar.show();
            }
            if (responseCode==0){
                PreferenceManager.getDefaultSharedPreferences(context).edit().putString("username", username).apply();
                PreferenceManager.getDefaultSharedPreferences(context).edit().putString("password", password).apply();
                PreferenceManager.getDefaultSharedPreferences(context).edit().putString("company_id", company_id).apply();
                Intent intent = new Intent(context, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
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
            pdia.dismiss();
            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Authentication cancelled by user!", Snackbar.LENGTH_LONG);
            snackbar.show();
        }

    }
}
