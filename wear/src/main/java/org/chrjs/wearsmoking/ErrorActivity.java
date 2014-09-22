package org.chrjs.wearsmoking;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;

public class ErrorActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
    }
}
