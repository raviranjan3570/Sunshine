package com.example.android.sunshine;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class DetailActivity extends AppCompatActivity {

    TextView mWeatherDisplay;
    /* A summary of the forecast that can be shared by clicking the share button in the ActionBar */
    String mForecast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Intent intent = getIntent();
        mWeatherDisplay = findViewById(R.id.tv_display_weather);
        if (intent != null) {
            if (intent.hasExtra(Intent.EXTRA_TEXT)) {
                mForecast = intent.getStringExtra(Intent.EXTRA_TEXT);
                mWeatherDisplay.setText(mForecast);
            }
        }
    }

    /**
     * Uses the ShareCompat Intent builder to create our Forecast intent for sharing.  All we need
     * to do is set the type, text and the NEW_DOCUMENT flag so it treats our share as a new task.
     * See: http://developer.android.com/guide/components/tasks-and-back-stack.html for more info.
     *
     * @return the Intent to use to share our weather forecast
     */
    private Intent createShareForecastIntent() {

        Intent shareIntent = ShareCompat.IntentBuilder.from(this)
                .setType("text/plain")
                .setText(mForecast)
                .getIntent();
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        return shareIntent;
    }

    /**
     * This is where we inflate and set up the menu for this Activity.
     *
     * @param menu The options menu in which you place your items.
     * @return You must return true for the menu to be displayed;
     * if you return false it will not be shown.
     * @see #onPrepareOptionsMenu
     * @see #onOptionsItemSelected
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail, menu);
        MenuItem menuItem = menu.findItem(R.id.action_share);
        menuItem.setIntent(createShareForecastIntent());
        return true;
    }

    /**
     * Callback invoked when a menu item was selected from this Activity's menu. Android will
     * automatically handle clicks on the "up" button for us so long as we have specified
     * DetailActivity's parent Activity in the AndroidManifest.
     *
     * @param item The menu item that was selected by the user
     * @return true if you handle the menu click here, false otherwise
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_setting) {
            Intent startSettingsIntent = new Intent(DetailActivity.this,
                    SettingsActivity.class);
            startActivity(startSettingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}