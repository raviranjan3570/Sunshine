/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.sunshine;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.sunshine.data.SunshinePreferences;
import com.example.android.sunshine.utilities.NetworkUtils;
import com.example.android.sunshine.utilities.OpenWeatherJsonUtils;

import java.net.URL;

public class MainActivity extends AppCompatActivity implements ForecastAdapter.ForecastAdapterOnClickListener, LoaderManager.LoaderCallbacks<String[]> {

    private RecyclerView mRecyclerView;
    TextView errorMessageTextView;
    ProgressBar mLoadingIndicator;
    private ForecastAdapter mForecastAdapter;
    private final String TAG = MainActivity.class.getSimpleName();
    private static final int FORECAST_LOADER_ID = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);

        mRecyclerView = findViewById(R.id.recycler_view_forecast);
        errorMessageTextView = findViewById(R.id.tv_error_message);
        mLoadingIndicator = findViewById(R.id.pb_loading_indicator);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);

        mForecastAdapter = new ForecastAdapter(this);
        mRecyclerView.setAdapter(mForecastAdapter);
        LoaderManager.LoaderCallbacks<String[]> callback = MainActivity.this;
        LoaderManager.getInstance(this).initLoader(FORECAST_LOADER_ID, null, callback);
    }

    public void showWeatherDataView() {
        mRecyclerView.setVisibility(View.VISIBLE);
        errorMessageTextView.setVisibility(View.INVISIBLE);
    }

    public void showErrorMessage() {
        mRecyclerView.setVisibility(View.INVISIBLE);
        errorMessageTextView.setVisibility(View.VISIBLE);
    }

    public void openMapLocation() {
        String address = "1600 Ampitheatre Parkway, CA";
        Uri geoLocation = Uri.parse("geo:0,0?q=" + address);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Log.d(TAG, "Couldn't call" + geoLocation.toString() + ", no receiving app installed!");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemThatWasClicked = item.getItemId();
        if (itemThatWasClicked == R.id.action_refresh) {
            invalidateData();
            LoaderManager.getInstance(this).restartLoader(FORECAST_LOADER_ID, null, this);
            return true;
        }
        if (itemThatWasClicked == R.id.action_map) {
            openMapLocation();
            return true;
        }
        if (itemThatWasClicked == R.id.action_setting) {
            Intent startSettingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(startSettingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.forecast, menu);
        return true;
    }

    @Override
    public void onClick(String weatherForDay) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(Intent.EXTRA_TEXT, weatherForDay);
        startActivity(intent);
    }

    @SuppressLint("StaticFieldLeak")
    @NonNull
    @Override
    public Loader<String[]> onCreateLoader(int i, @Nullable Bundle bundle) {
        return new AsyncTaskLoader<String[]>(this) {

            String[] mWeatherData = null;

            @Override
            protected void onStartLoading() {
                if (mWeatherData != null) {
                    deliverResult(mWeatherData);
                } else {
                    mLoadingIndicator.setVisibility(View.VISIBLE);
                    forceLoad();
                }
            }

            @Override
            public void deliverResult(@Nullable String[] data) {
                mWeatherData = data;
                super.deliverResult(data);
            }

            @Nullable
            @Override
            public String[] loadInBackground() {
                String location = SunshinePreferences.getPreferredWeatherLocation(MainActivity.this);
                URL weatherRequestUrl = NetworkUtils.buildUrl(location);

                try {
                    String jsonWeatherResponse = NetworkUtils
                            .getResponseFromHttpUrl(weatherRequestUrl);

                    return OpenWeatherJsonUtils
                            .getSimpleWeatherStringsFromJson(MainActivity.this, jsonWeatherResponse);

                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
        };
    }

    @Override
    public void onLoadFinished(@NonNull Loader<String[]> loader, String[] weatherData) {

        mLoadingIndicator.setVisibility(View.INVISIBLE);
        if (weatherData != null) {
            showWeatherDataView();
            mForecastAdapter.setWeatherData(weatherData);
        } else {
            showErrorMessage();
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<String[]> loader) {

    }

    public void invalidateData() {
        mForecastAdapter.setWeatherData(null);
    }
}