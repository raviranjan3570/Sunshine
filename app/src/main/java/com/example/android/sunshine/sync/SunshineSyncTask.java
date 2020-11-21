package com.example.android.sunshine.sync;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;

import com.example.android.sunshine.data.WeatherContract;
import com.example.android.sunshine.utilities.NetworkUtils;
import com.example.android.sunshine.utilities.OpenWeatherJsonUtils;

import java.net.URL;

public class SunshineSyncTask {

    synchronized public static void SyncWeather(Context context) {

        try {
            URL weatherRequestUrl = NetworkUtils.getUrl(context);
            String jsonWeatherResponse = NetworkUtils.getResponseFromHttpUrl(weatherRequestUrl);
            ContentValues[] weatherValues = OpenWeatherJsonUtils.getWeatherContentValuesFromJson(context, jsonWeatherResponse);

            if (weatherValues != null && weatherValues.length != 0) {

                ContentResolver sunshineContentResolver = context.getContentResolver();
                sunshineContentResolver.delete(WeatherContract.WeatherEntry.CONTENT_URI,
                        null,
                        null);
                sunshineContentResolver.bulkInsert(WeatherContract.WeatherEntry.CONTENT_URI, weatherValues);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}