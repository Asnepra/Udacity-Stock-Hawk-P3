package com.sam_chordas.android.stockhawk.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.TaskParams;
import com.sam_chordas.android.stockhawk.R;

/**
 * Created by sam_chordas on 10/1/15.
 */
public class StockIntentService extends IntentService {
    private static final String ACTION_DATA_UPDATED = String.valueOf(R.string.action_data_updated);

    public StockIntentService() {
        super(StockIntentService.class.getName());
    }

    public StockIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(StockIntentService.class.getSimpleName(), "Stock Intent Service");
        StockTaskService stockTaskService = new StockTaskService(this);
        Bundle args = new Bundle();
        if (intent.getStringExtra("tag").equals("add")) {
            args.putString("symbol", intent.getStringExtra("symbol"));
        }
        // We can call OnRunTask from the intent service to force it to run immediately instead of
        // scheduling a task.
        Handler mHandler = new Handler(Looper.getMainLooper());

        try {
            stockTaskService.onRunTask(new TaskParams(intent.getStringExtra("tag"), args));
            Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED);
            getApplicationContext().sendBroadcast(dataUpdatedIntent);
        } catch (Exception e) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), R.string.stock_not_exist_string, Toast.LENGTH_SHORT).show();
                }
            });

        }

    }
}
