package com.example.sunshine.sync;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.text.format.DateUtils;

//import com.example.android.sunshine.data.SunshinePreferences;
//import com.example.android.sunshine.data.WeatherContract;
//import com.example.android.sunshine.utilities.NetworkUtils;
//import com.example.android.sunshine.utilities.NotificationUtils;
//import com.example.android.sunshine.utilities.OpenWeatherJsonUtils;
import com.example.sunshine.data.SunshinePreferences;
import com.example.sunshine.data.WeatherContract;
import com.example.sunshine.utilities.NetworkUtils;
import com.example.sunshine.utilities.NotificationUtils;
import com.example.sunshine.utilities.OpenWeatherJsonUtils;

import java.net.URL;

public class SunshineSyncTask {

    synchronized public static void syncWeather(Context context) {

        try {

            URL weatherRequestUrl = NetworkUtils.getUrl(context);
            /* Use the URL to retrieve the JSON */
            String jsonWeatherResponse = NetworkUtils.getResponseFromHttpUrl(weatherRequestUrl);
            /* Parse the JSON into a list of weather values */
            ContentValues[] weatherValues = OpenWeatherJsonUtils
                    .getWeatherContentValuesFromJson(context, jsonWeatherResponse);

            if (weatherValues != null && weatherValues.length != 0) {
                /* Get a handle on the ContentResolver to delete and insert data */
                ContentResolver sunshineContentResolver = context.getContentResolver();
                /* Delete old weather data because we don't need to keep multiple days' data */
                sunshineContentResolver.delete(
                        WeatherContract.WeatherEntry.CONTENT_URI,
                        null,
                        null);

                /* Insert our new weather data into Sunshine's ContentProvider */
                sunshineContentResolver.bulkInsert(
                        WeatherContract.WeatherEntry.CONTENT_URI,
                        weatherValues);


                boolean notificationsEnabled = SunshinePreferences.areNotificationsEnabled(context);

                long timeSinceLastNotification = SunshinePreferences
                        .getEllapsedTimeSinceLastNotification(context);

                boolean oneDayPassedSinceLastNotification = false;

//              COMPLETED (14) Check if a day has passed since the last notification
                if (timeSinceLastNotification >= DateUtils.DAY_IN_MILLIS) {
                    oneDayPassedSinceLastNotification = true;
                }

                /*
                 * We only want to show the notification if the user wants them shown and we
                 * haven't shown a notification in the past day.
                 */
//              COMPLETED (15) If more than a day have passed and notifications are enabled, notify the user
                if (notificationsEnabled && oneDayPassedSinceLastNotification) {
                    NotificationUtils.notifyUserOfNewWeather(context);
                }

            }

        } catch (Exception e) {
            /* Server probably invalid */
            e.printStackTrace();
        }
    }
}