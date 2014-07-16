package org.chrjs.wearsmoking;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

import java.text.NumberFormat;

public class Statistic extends Activity {

    private TextView textViewAvoided;
    private TextView textViewSince;
    private TextView textViewSavedMoney;

    private static final String PREF_DATE = "quitdate";
    private static final String PREF_PRICE = "priceperpackage";
    private static final String PREF_CIGSPERPACKAGE = "cigsperpackage";
    private static final String PREF_CIGSPERDAY = "cigsperday";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rect_activity_statictic);

        textViewAvoided = (TextView) findViewById(R.id.textview_avoided_value);
        textViewSince = (TextView) findViewById(R.id.textview_since_value);
        textViewSavedMoney = (TextView) findViewById(R.id.textview_saved_value);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getValues();
    }

    private void getValues() {
        final GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(Wearable.API).build();
        mGoogleApiClient.connect();

        PendingResult<DataItemBuffer> results = Wearable.DataApi.getDataItems(mGoogleApiClient);
        results.setResultCallback(new ResultCallback<DataItemBuffer>() {
            @Override
            public void onResult(DataItemBuffer dataItems) {
                if (dataItems.getCount() != 0) {
                    DataMap dataMap = DataMapItem.fromDataItem(dataItems.get(0)).getDataMap();

                    int cigsPerPackage = dataMap.getInt(PREF_CIGSPERPACKAGE, -1);
                    long date = dataMap.getLong(PREF_DATE, -1);
                    float savedMoney = dataMap.getFloat(PREF_PRICE, -1);
                    int cigsPerDay = dataMap.getInt(PREF_CIGSPERDAY, -1);

                    if (cigsPerDay == 0 || date == 0 || savedMoney == 0 || cigsPerDay == 0) {
                        raiseError();
                    } else {
                        setValues(new Data(cigsPerDay, cigsPerPackage, savedMoney, date));
                    }
                } else {
                    raiseError();
                }
                dataItems.release();
                mGoogleApiClient.disconnect();
            }
        });
    }

    private void raiseError() {
        Intent i = new Intent(this, ErrorActivity.class);
        startActivity(i);
    }

    private void setValues(Data dto) {
        textViewAvoided.setText(String.valueOf(dto.getAvoidedCigs()));
        textViewSince.setText(dto.getTimeSinceQuit());
        NumberFormat format = NumberFormat.getCurrencyInstance();
        String formattedMoneyValue = dto.getSavedMoney() + format.getCurrency().getSymbol();
        textViewSavedMoney.setText(formattedMoneyValue);
    }
}
