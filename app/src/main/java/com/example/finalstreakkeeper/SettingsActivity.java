package com.example.finalstreakkeeper;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences("streak_prefs", MODE_PRIVATE);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        SwitchMaterial notificationsSwitch = findViewById(R.id.notificationsSwitch);
        Button resetStreaksBtn = findViewById(R.id.resetStreaksBtn);
        Button clearDataBtn = findViewById(R.id.clearDataBtn);

        notificationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String status = isChecked ? "enabled" : "disabled";
            Toast.makeText(this, "Notifications " + status, Toast.LENGTH_SHORT).show();
        });

        resetStreaksBtn.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Reset All Streaks")
                    .setMessage("Are you sure? This action cannot be undone.")
                    .setPositiveButton("Reset", (dialog, which) -> {
                        resetStreaksInPrefs();
                        Toast.makeText(this, "All streaks reset", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        clearDataBtn.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Clear All Data")
                    .setMessage("This will delete all your habits and streaks. Continue?")
                    .setPositiveButton("Clear", (dialog, which) -> {
                        prefs.edit().remove("habits_list").apply();
                        Toast.makeText(this, "All data cleared. Restart the app to see changes.", Toast.LENGTH_LONG).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void resetStreaksInPrefs() {
        Gson gson = new Gson();
        String json = prefs.getString("habits_list", null);
        Type type = new TypeToken<ArrayList<Habit>>() {}.getType();
        ArrayList<Habit> habitList = json == null ? new ArrayList<>() : gson.fromJson(json, type);

        for (Habit h : habitList) {
            h.setStreak(0);
            h.setCompletedToday(false);

        }

        prefs.edit().putString("habits_list", gson.toJson(habitList)).apply();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}