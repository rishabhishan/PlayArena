package com.semidigit.playarena;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class TicketResultActivity extends AppCompatActivity {

    TextView txtError, checkinTime, checkOutTime, ratePerHour, totalAmount;
    LinearLayout ticketView;
    float rate = 10.0f;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_result);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String dateStartString = getIntent().getStringExtra("time");
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
        SimpleDateFormat formatter = new SimpleDateFormat("dd MMMM yyyy hh:mm:ss");
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
        float total = diffHours*rate;
        totalAmount.setText(String.valueOf(total));



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

}