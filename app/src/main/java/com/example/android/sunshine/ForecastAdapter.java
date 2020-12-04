package com.example.android.sunshine;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.sunshine.utilities.SunshineDateUtils;
import com.example.android.sunshine.utilities.SunshineWeatherUtils;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link android.database.Cursor} to a {@link android.support.v7.widget.RecyclerView}.
 */
class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ForecastAdapterViewHolder> {

    /* The context we use to utility methods, app resources and layout inflaters */
    private final Context mContext;

    private static final int VIEW_TYPE_TODAY = 0;
    private static final int VIEW_TYPE_FUTURE_DAY = 1;
    private final boolean mUseTodayLayout;

    /**
     * Below, we've defined an interface to handle clicks on items within this Adapter. In the
     * constructor of our ForecastAdapter, we receive an instance of a class that has implemented
     * said interface. We store that instance in this variable to call the onClick method whenever
     * an item is clicked in the list.
     */
    private final ForecastAdapterOnClickListener mClickHandler;

    private Cursor mCursor;

    /**
     * Creates a ForecastAdapter.
     *
     * @param context       Used to talk to the UI and app resources
     * @param mClickHandler The on-click handler for this adapter. This single handler is called
     *                      when an item is clicked.
     */
    public ForecastAdapter(@NonNull Context context, ForecastAdapterOnClickListener mClickHandler) {
        mContext = context;
        this.mClickHandler = mClickHandler;
        mUseTodayLayout = mContext.getResources().getBoolean(R.bool.use_today_layout);
    }

    /**
     * This gets called when each new ViewHolder is created. This happens when the RecyclerView
     * is laid out. Enough ViewHolders will be created to fill the screen and allow for scrolling.
     *
     * @param viewGroup The ViewGroup that these ViewHolders are contained within.
     * @param viewType  If your RecyclerView has more than one type of item (which ours doesn't) you
     *                  can use this viewType integer to provide a different layout. See
     *                  {@link android.support.v7.widget.RecyclerView.Adapter#getItemViewType(int)}
     *                  for more details.
     * @return A new ForecastAdapterViewHolder that holds the View for each list item
     */
    @NonNull
    @Override
    public ForecastAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        int layoutIdForListItem;

        if (viewType == VIEW_TYPE_TODAY) layoutIdForListItem = R.layout.list_item_forecast_today;
        else layoutIdForListItem = R.layout.forecast_list_item;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, false);
        view.setFocusable(true);
        return new ForecastAdapterViewHolder(view);
    }

    /**
     * OnBindViewHolder is called by the RecyclerView to display the data at the specified
     * position. In this method, we update the contents of the ViewHolder to display the weather
     * details for this particular position, using the "position" argument that is conveniently
     * passed into us.
     *
     * @param forecastAdapterViewHolder The ViewHolder which should be updated to represent the
     *                                  contents of the item at the given position in the data set.
     * @param position                  The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull ForecastAdapterViewHolder forecastAdapterViewHolder,
                                 int position) {
        mCursor.moveToPosition(position);

        /* Read date from the cursor */
        long dateInMills = mCursor.getLong(MainActivity.INDEX_WEATHER_DATE);

        /* Get human readable string using our utility method */
        String dateString = SunshineDateUtils.getFriendlyDateString(mContext, dateInMills,
                false);
        forecastAdapterViewHolder.tv_weather_date.setText(dateString);
        /* Use the weatherId to obtain the proper description */
        int weatherId = mCursor.getInt(MainActivity.INDEX_WEATHER_CONDITION_ID);
        forecastAdapterViewHolder.iv_weather_preview.setImageResource(
                SunshineWeatherUtils.getSmallArtResourceIdForWeatherCondition(weatherId));
        String description = SunshineWeatherUtils.getStringForWeatherCondition(mContext, weatherId);
        forecastAdapterViewHolder.tv_weather_detail.setText(description);
        /* Read high temperature from the cursor (in degrees celsius) */
        double highInCelsius = mCursor.getDouble(MainActivity.INDEX_WEATHER_MAX_TEMP);
        /* Read low temperature from the cursor (in degrees celsius) */
        double lowInCelsius = mCursor.getDouble(MainActivity.INDEX_WEATHER_MIN_TEMP);

        String highTemperature =
                SunshineWeatherUtils.formatTemperature(mContext, highInCelsius);
        forecastAdapterViewHolder.tv_max_temperature.setText(highTemperature);

        String lowTemperature =
                SunshineWeatherUtils.formatTemperature(mContext, lowInCelsius);
        forecastAdapterViewHolder.tv_min_temperature.setText(lowTemperature);
    }

    /**
     * This method simply returns the number of items to display. It is used behind the scenes
     * to help layout our Views and for animations.
     *
     * @return The number of items available in our forecast
     */
    @Override
    public int getItemCount() {
        if (null == mCursor) return 0;
        return mCursor.getCount();
    }

    @Override
    public int getItemViewType(int position) {
        if (mUseTodayLayout && position == 0) return VIEW_TYPE_TODAY;
        else return VIEW_TYPE_FUTURE_DAY;
    }

    /**
     * Swaps the cursor used by the ForecastAdapter for its weather data. This method is called by
     * MainActivity after a load has finished, as well as when the Loader responsible for loading
     * the weather data is reset. When this method is called, we assume we have a completely new
     * set of data, so we call notifyDataSetChanged to tell the RecyclerView to update.
     *
     * @param newCursor the new cursor to use as ForecastAdapter's data source
     */
    void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
    }

    /**
     * The interface that receives onClick messages.
     */
    public interface ForecastAdapterOnClickListener {
        void onClick(long date);
    }

    /**
     * A ViewHolder is a required part of the pattern for RecyclerViews. It mostly behaves as
     * a cache of the child views for a forecast item. It's also a convenient place to set an
     * OnClickListener, since it has access to the adapter and the views.
     */
    public class ForecastAdapterViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener {

        final TextView tv_weather_date;
        final TextView tv_weather_detail;
        final TextView tv_min_temperature;
        final TextView tv_max_temperature;
        final ImageView iv_weather_preview;

        public ForecastAdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_weather_date = itemView.findViewById(R.id.day);
            tv_weather_detail = itemView.findViewById(R.id.weather_detail);
            tv_min_temperature = itemView.findViewById(R.id.minimum_temperature);
            tv_max_temperature = itemView.findViewById(R.id.maximum_temperature);
            iv_weather_preview = itemView.findViewById(R.id.weather_preview);
            itemView.setOnClickListener(this);
        }

        /**
         * This gets called by the child views during a click. We fetch the date that has been
         * selected, and then call the onClick handler registered with this adapter, passing that
         * date.
         *
         * @param view the View that was clicked
         */
        @Override
        public void onClick(View view) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            long dateInMills = mCursor.getLong(MainActivity.INDEX_WEATHER_DATE);
            mClickHandler.onClick(dateInMills);
        }
    }
}