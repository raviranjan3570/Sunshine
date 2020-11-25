package com.example.android.sunshine.sync;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.example.android.sunshine.data.WeatherContract;
import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;

import java.util.concurrent.TimeUnit;

public class SunshineSyncUtils {

    /*
     * Interval at which to sync with the weather. Use TimeUnit for convenience, rather than
     * writing out a bunch of multiplication ourselves and risk making a silly mistake.
     */
    private static final int SYNC_INTERVALS_HOURS = 3;
    private static final int SYNC_INTERVALS_SECONDS = (int) TimeUnit.HOURS.toSeconds(SYNC_INTERVALS_HOURS);
    private static final int SYNC_FLEXTIME_SECONDS = SYNC_INTERVALS_SECONDS / 3;

    private static final String SUNSHINE_SYNC_TAG = "sunshine-sync";

    private static boolean sInitialized;

    /**
     * Schedules a repeating sync of Sunshine's weather data using FirebaseJobDispatcher.
     *
     * @param context Context used to create the GooglePlayDriver that powers the
     *                FirebaseJobDispatcher
     */
    static void scheduleFirebaseJobDispatcher(@NonNull final Context context) {

        Driver driver = new GooglePlayDriver(context);
        FirebaseJobDispatcher firebaseJobDispatcher = new FirebaseJobDispatcher(driver);

        Job syncSunshineJob = firebaseJobDispatcher.newJobBuilder()
                .setService(SunshineFirebaseJobService.class)
                .setTag(SUNSHINE_SYNC_TAG)
                .setConstraints(Constraint.ON_ANY_NETWORK)
                .setLifetime(Lifetime.FOREVER)
                .setRecurring(true)
                .setTrigger(Trigger.executionWindow(SYNC_INTERVALS_SECONDS,
                        SYNC_INTERVALS_SECONDS + SYNC_FLEXTIME_SECONDS))
                .setReplaceCurrent(true)
                .build();

        firebaseJobDispatcher.schedule(syncSunshineJob);

    }


    /**
     * Creates periodic sync tasks and checks to see if an immediate sync is required. If an
     * immediate sync is required, this method will take care of making sure that sync occurs.
     *
     * @param context Context that will be passed to other methods and used to access the
     *                ContentResolver
     */
    synchronized public static void initialize(@NonNull final Context context) {

        if (sInitialized) return;
        sInitialized = true;

        scheduleFirebaseJobDispatcher(context);

        /*
         * We need to check to see if our ContentProvider has data to display in our forecast
         * list. However, performing a query on the main thread is a bad idea as this may
         * cause our UI to lag. Therefore, we create a thread in which we will run the query
         * to check the contents of our ContentProvider.
         */
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                Uri forecastQueryUri = WeatherContract.WeatherEntry.CONTENT_URI;
                /*
                 * Since this query is going to be used only as a check to see if we have any
                 * data (rather than to display data), we just need to PROJECT the ID of each
                 * row. In our queries where we display data, we need to PROJECT more columns
                 * to determine what weather details need to be displayed.
                 */
                String[] projectionColumns = {WeatherContract.WeatherEntry._ID};
                String selectionStatement = WeatherContract.WeatherEntry.getSqlSelectedFromTodayOnwards();

                /* Here, we perform the query to check to see if we have any weather data */
                Cursor cursor = context.getContentResolver().query(
                        forecastQueryUri,
                        projectionColumns,
                        selectionStatement,
                        null,
                        null
                );

                /*
                 * A Cursor object can be null for various different reasons. A few are
                 * listed below.
                 *
                 *   1) Invalid URI
                 *   2) A certain ContentProvider's query method returns null
                 *   3) A RemoteException was thrown.
                 *
                 * Bottom line, it is generally a good idea to check if a Cursor returned
                 * from a ContentResolver is null.
                 *
                 * If the Cursor was null OR if it was empty, we need to sync immediately to
                 * be able to display data to the user.
                 */
                if (cursor == null || cursor.getCount() == 0) {
                    startImmediateSync(context);
                }

                assert cursor != null;
                cursor.close();
                return null;
            }
        }.execute();
    }

    public static void startImmediateSync(Context context) {
        Intent intentToSyncImmediately = new Intent(context, SunshineSyncIntentService.class);
        context.startService(intentToSyncImmediately);
    }
}