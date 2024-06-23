package com.suman.iotserver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import androidx.core.content.ContextCompat;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.e("TAG", "Boot Completed: " );
            Intent mainIntent = new Intent(context, MainActivity.class);
            context.startActivity(mainIntent);
        }
    }
}
