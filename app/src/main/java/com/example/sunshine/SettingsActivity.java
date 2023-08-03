package com.example.sunshine;

import android.os.Bundle;
//import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

// COMPLETED (1) Add new Activity called SettingsActivity using Android Studio wizard

/**
 * Loads the SettingsFragment and handles the proper behavior of the up button.
 */
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }
}