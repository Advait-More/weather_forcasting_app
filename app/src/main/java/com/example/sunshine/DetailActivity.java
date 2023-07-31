package com.example.sunshine;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
//import android.support.v4.app.ShareCompat;
//import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ShareCompat;
import androidx.databinding.DataBindingUtil;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.example.sunshine.data.WeatherContract;
import com.example.sunshine.databinding.ActivityDetailBinding;
import com.example.sunshine.utilities.SunshineDateUtils;
import com.example.sunshine.utilities.SunshineWeatherUtils;

public class DetailActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>{

    private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";

    public static final String[] WEATHER_DETAIL_PROJECTION = {
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_DEGREES,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID
    };

    public static final int INDEX_WEATHER_DATE = 0;
    public static final int INDEX_WEATHER_MAX_TEMP = 1;
    public static final int INDEX_WEATHER_MIN_TEMP = 2;
    public static final int INDEX_WEATHER_HUMIDITY = 3;
    public static final int INDEX_WEATHER_PRESSURE = 4;
    public static final int INDEX_WEATHER_WIND_SPEED = 5;
    public static final int INDEX_WEATHER_DEGREES = 6;
    public static final int INDEX_WEATHER_CONDITION_ID = 7;

    private static final int ID_DETAIL_LOADER = 353;
    private String mForecastSummary;
    private Uri mUri;

    private ActivityDetailBinding mDetailBinding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDetailBinding = DataBindingUtil.setContentView(this, R.layout.activity_detail);

        mUri = getIntent().getData();
        if (mUri == null) throw new NullPointerException("URI for DetailActivity cannot be null");

        /* This connects our Activity into the loader lifecycle. */
        getSupportLoaderManager().initLoader(ID_DETAIL_LOADER, null, this);
    }

    private Intent createShareForecastIntent() {
        Intent shareIntent = ShareCompat.IntentBuilder.from(this)
                .setType("text/plain")
                .setText(mForecastSummary + FORECAST_SHARE_HASHTAG)
                .getIntent();
        return shareIntent;
    }

    public Loader<Cursor> onCreateLoader(int loaderId, Bundle loaderArgs) {

        switch (loaderId) {

            case ID_DETAIL_LOADER:

                return new CursorLoader(this,
                        mUri,
                        WEATHER_DETAIL_PROJECTION,
                        null,
                        null,
                        null);

            default:
                throw new RuntimeException("Loader Not Implemented: " + loaderId);
        }
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        boolean cursorHasValidData = false;
        if (data != null && data.moveToFirst()) {
            /* We have valid data, continue on to bind the data to the UI */
            cursorHasValidData = true;
        }

        if (!cursorHasValidData) {
            /* No data to display, simply return and do nothing */
            return;
        }

        /* Read weather condition ID from the cursor (ID provided by Open Weather Map) */
        int weatherId = data.getInt(INDEX_WEATHER_CONDITION_ID);
        /* Use our utility method to determine the resource ID for the proper art */
        int weatherImageId = SunshineWeatherUtils.getLargeArtResourceIdForWeatherCondition(weatherId);

        /* Set the resource ID on the icon to display the art */
        mDetailBinding.primaryInfo.weatherIcon.setImageResource(weatherImageId);


        long localDateMidnightGmt = data.getLong(INDEX_WEATHER_DATE);
        String dateText = SunshineDateUtils.getFriendlyDateString(this, localDateMidnightGmt, true);

        mDetailBinding.primaryInfo.date.setText(dateText);


        /* Use the weatherId to obtain the proper description */
        String description = SunshineWeatherUtils.getStringForWeatherCondition(this, weatherId);

        /* Create the accessibility (a11y) String from the weather description */
        String descriptionA11y = getString(R.string.a11y_forecast, description);

        /* Set the text and content description (for accessibility purposes) */
        mDetailBinding.primaryInfo.weatherDescription.setText(description);
        mDetailBinding.primaryInfo.weatherDescription.setContentDescription(descriptionA11y);
        /* Set the content description on the weather image (for accessibility purposes) */
        mDetailBinding.primaryInfo.weatherIcon.setContentDescription(descriptionA11y);


        /* Read high temperature from the cursor (in degrees celsius) */
        double highInCelsius = data.getDouble(INDEX_WEATHER_MAX_TEMP);

        String highString = SunshineWeatherUtils.formatTemperature(this, highInCelsius);

        /* Create the accessibility (a11y) String from the weather description */
        String highA11y = getString(R.string.a11y_high_temp, highString);

        /* Set the text and content description (for accessibility purposes) */
        mDetailBinding.primaryInfo.highTemperature.setText(highString);
        mDetailBinding.primaryInfo.highTemperature.setContentDescription(highA11y);


        /* Read low temperature from the cursor (in degrees celsius) */
        double lowInCelsius = data.getDouble(INDEX_WEATHER_MIN_TEMP);

        String lowString = SunshineWeatherUtils.formatTemperature(this, lowInCelsius);
        String lowA11y = getString(R.string.a11y_low_temp, lowString);

        /* Set the text and content description (for accessibility purposes) */
        mDetailBinding.primaryInfo.lowTemperature.setText(lowString);
        mDetailBinding.primaryInfo.lowTemperature.setContentDescription(lowA11y);


        /* Read humidity from the cursor */
        float humidity = data.getFloat(INDEX_WEATHER_HUMIDITY);
        String humidityString = getString(R.string.format_humidity, humidity);

        String humidityA11y = getString(R.string.a11y_humidity, humidityString);

        /* Set the text and content description (for accessibility purposes) */
        mDetailBinding.extraDetails.humidity.setText(humidityString);
        mDetailBinding.extraDetails.humidity.setContentDescription(humidityA11y);

        mDetailBinding.extraDetails.humidityLabel.setContentDescription(humidityA11y);

        /* Read wind speed (in MPH) and direction (in compass degrees) from the cursor  */
        float windSpeed = data.getFloat(INDEX_WEATHER_WIND_SPEED);
        float windDirection = data.getFloat(INDEX_WEATHER_DEGREES);
        String windString = SunshineWeatherUtils.getFormattedWind(this, windSpeed, windDirection);

        String windA11y = getString(R.string.a11y_wind, windString);

        /* Set the text and content description (for accessibility purposes) */
        mDetailBinding.extraDetails.windMeasurement.setText(windString);
        mDetailBinding.extraDetails.windMeasurement.setContentDescription(windA11y);

        mDetailBinding.extraDetails.windLabel.setContentDescription(windA11y);

        /* Read pressure from the cursor */
        float pressure = data.getFloat(INDEX_WEATHER_PRESSURE);

        String pressureString = getString(R.string.format_pressure, pressure);
        String pressureA11y = getString(R.string.a11y_pressure, pressureString);

        /* Set the text and content description (for accessibility purposes) */
        mDetailBinding.extraDetails.pressure.setText(pressureString);
        mDetailBinding.extraDetails.pressure.setContentDescription(pressureA11y);

        mDetailBinding.extraDetails.pressureLabel.setContentDescription(pressureA11y);

        /* Store the forecast summary String in our forecast summary field to share later */
        mForecastSummary = String.format("%s - %s - %s/%s",
                dateText, description, highString, lowString);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent startSettingsActivity = new Intent(this, com.example.sunshine.SettingsActivity.class);
            startActivity(startSettingsActivity);
            return true;
        }

        if (id == R.id.action_share) {
            Intent shareIntent = createShareForecastIntent();
            startActivity(shareIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    public void onLoaderReset(Loader<Cursor> loader) {
    }

}