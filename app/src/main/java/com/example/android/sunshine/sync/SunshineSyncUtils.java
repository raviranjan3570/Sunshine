package com.example.android.sunshine.sync;

import android.content.Context;
import android.content.Intent;

public class SunshineSyncUtils {

    public static void startImmediateSync(Context context) {
        Intent intentToSyncImmediately = new Intent(context, SunshineSyncIntentService.class);
        context.startService(intentToSyncImmediately);
    }
}