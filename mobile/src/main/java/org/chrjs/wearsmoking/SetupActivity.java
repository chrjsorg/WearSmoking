package org.chrjs.wearsmoking;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;

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

public class SetupActivity extends AppCompatActivity {

    private static final String PREF_FILE = "smokeFile";
    private static final String PREF_DATE = "quitdate";
    private static final String PREF_PRICE = "priceperpackage";
    private static final String PREF_CIGSPERPACKAGE = "cigsperpackage";
    private static final String PREF_CIGSPERDAY = "cigsperday";
    private EditText editTextDate;
    private EditText editTextPricePerPackage;
    private EditText editTextCigsPerDay;
    private EditText editTextCigsPerPackage;
    private boolean isDatePickerOpened;
    private final DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            calendarInstance.set(Calendar.YEAR, year);
            calendarInstance.set(Calendar.MONTH, monthOfYear);
            calendarInstance.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            isDatePickerOpened = false;
            updateDateTextView();
            editTextDate.clearFocus();
        }
    };
    private final Calendar calendarInstance = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        setViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setViews();
        readPreferences();
    }

    private void showDatePickerDialog() {
        if (isDatePickerOpened) {
            return;
        }
        isDatePickerOpened = true;
        DatePickerDialog dialog = new DatePickerDialog(this, onDateSetListener, calendarInstance
                .get(Calendar.YEAR), calendarInstance.get(Calendar.MONTH),
                calendarInstance.get(Calendar.DAY_OF_MONTH)
        );
        dialog.setCanceledOnTouchOutside(true);
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                isDatePickerOpened = false;
                editTextDate.clearFocus();
            }
        });
        //Use new instance every time so the max date is always "now", not some previous saved date
        Calendar c = Calendar.getInstance();
        dialog.getDatePicker().setMaxDate(c.getTimeInMillis());
        dialog.show();
    }

    private void updateDateTextView() {
        DateFormat df;
        df = android.text.format.DateFormat.getDateFormat(getApplicationContext()); // Gets system DF
        editTextDate.setText(df.format(calendarInstance.getTime())); // Returns a string formatted
    }

    private void showSnackbar(boolean success) {
        String text;

        if (success) {
            View.OnClickListener ocl;
            text = getString(R.string.save_successful);
            ocl = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            };

            Snackbar.make(findViewById(R.id.rootView), text, Snackbar.LENGTH_SHORT)
                    .setAction("Close", ocl).show();
        } else {
            text = getString(R.string.save_failed);
            Snackbar.make(findViewById(R.id.rootView), text, Snackbar.LENGTH_SHORT).show();
        }
    }

    private void save() {
        final GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API).build();

        SharedPreferences settings = getSharedPreferences(PREF_FILE, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

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
            showSnackbar(false);
            return;
        }

        editor.putInt(PREF_CIGSPERDAY, cigsPerDay);
        editor.putInt(PREF_CIGSPERPACKAGE, cigsPerPackage);
        editor.putFloat(PREF_PRICE, pricePerPackage);
        editor.putLong(PREF_DATE, dateMillis);
        editor.apply();

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
        showSnackbar(true);
        return;
    }

    private void readPreferences() {
        SharedPreferences settings = getSharedPreferences(PREF_FILE, 0);
        int cigsPerDay = settings.getInt(PREF_CIGSPERDAY, -1);
        int cigsPerPackage = settings.getInt(PREF_CIGSPERPACKAGE, -1);
        float pricePerPackage = settings.getFloat(PREF_PRICE, -1f);
        long dateMillis = settings.getLong(PREF_DATE, -1);

        if (cigsPerDay != -1) {
            editTextCigsPerDay.setText(String.valueOf(cigsPerDay));
        }
        if (cigsPerPackage != -1) {
            editTextCigsPerPackage.setText(String.valueOf(cigsPerPackage));
        }
        if (pricePerPackage != -1f) {
            editTextPricePerPackage.setText(String.valueOf(pricePerPackage));
        }
        if (dateMillis != -1) {
            editTextDate.setText(getDateFromMillis(dateMillis));
        }
    }

    private String getDateFromMillis(long millis) {
        DateFormat df;
        df = android.text.format.DateFormat.getDateFormat(getApplicationContext());

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        return df.format(calendar.getTime());
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

    private void setViews() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
            }
        });

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
}
