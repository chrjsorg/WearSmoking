package org.chrjs.wearsmoking;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;


public class SetupActivity extends Activity {

    private EditText editTextDate;
    private EditText editTextPricePerPackage;
    private EditText editTextCigsPerDay;
    private EditText editTextCigsPerPackage;

    private boolean datepickerOpen;
    private Calendar myCalendar = Calendar.getInstance();
    public static final String PREF_DATE = "quitdate";
    public static final String PREF_PRICE = "priceperpackage";
    public static final String PREF_CIGSPERPACKAGE = "cigsperpackage";
    public static final String PREF_CIGSPERDAY = "cigsperday";

    DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            // TODO Auto-generated method stub
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            datepickerOpen = false;
            updateLabel();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        editTextCigsPerDay = (EditText) findViewById(R.id.editText_smoke_per_day);
        editTextCigsPerPackage = (EditText) findViewById(R.id.edittext_cpp);
        editTextPricePerPackage = (EditText) findViewById(R.id.edittext_ppp);
        editTextDate = (EditText) findViewById(R.id.editText_quitdate);
        editTextDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });
        editTextDate.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showDatePickerDialog();
                }
            }
        });
    }

    private void showDatePickerDialog() {
        Logi.Debug("showDatePickerDialog, datepickerOpen=" + datepickerOpen);

        if (datepickerOpen == true) {
            return;
        }
        datepickerOpen = true;
        DatePickerDialog dialog = new DatePickerDialog(this, date, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.getDatePicker().setMaxDate(myCalendar.getTimeInMillis());
        dialog.show();
    }

    private void updateLabel() {
        DateFormat df;
        df = android.text.format.DateFormat.getDateFormat(getApplicationContext()); // Gets system DF
        editTextDate.setText(df.format(myCalendar.getTime())); // Returns a string formatted
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.setup, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_save) {
            if (save() == false) {
                Toast.makeText(this, "Please check your typed in values.", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean save() {
        final GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API).build();

        int cigsPerDay = 0;
        int cigsPerPackage = 0;
        float pricePerPackage = 0;
        long dateMillis = 0;

        if (!editTextCigsPerDay.getText().toString().isEmpty()) {
            cigsPerDay = Integer.parseInt(editTextCigsPerDay.getText().toString());
        }

        if (!editTextCigsPerPackage.getText().toString().isEmpty()) {
            cigsPerPackage = Integer.parseInt(editTextCigsPerPackage.getText().toString());
        }

        if (!editTextPricePerPackage.getText().toString().isEmpty()) {
            pricePerPackage = Float.parseFloat(editTextPricePerPackage.getText().toString());
        }

        if (!editTextDate.getText().toString().isEmpty()) {
            dateMillis = getMillisFromStringInput(editTextDate.getText().toString());
        }

        if (cigsPerDay == 0 || cigsPerPackage == 0 || pricePerPackage == 0 || dateMillis == 0) {
            return false;
        }

        mGoogleApiClient.connect();
        PutDataMapRequest dataMap = PutDataMapRequest.create("/smoke");
        dataMap.getDataMap().putInt(PREF_CIGSPERDAY, cigsPerDay);
        dataMap.getDataMap().putInt(PREF_CIGSPERPACKAGE, cigsPerPackage);
        dataMap.getDataMap().putLong(PREF_DATE, dateMillis);
        dataMap.getDataMap().putFloat(PREF_PRICE, pricePerPackage);

        PutDataRequest request = dataMap.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi
                .putDataItem(mGoogleApiClient, request);
        pendingResult.setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
            @Override
            public void onResult(DataApi.DataItemResult dataItemResult) {
                mGoogleApiClient.disconnect();
            }
        });
        return true;
    }

    private long getMillisFromStringInput(String input) {
        try {
            DateFormat df;
            df = android.text.format.DateFormat.getDateFormat(getApplicationContext());
            Date bla = df.parse(input);
            return bla.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
