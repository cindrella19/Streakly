package com.example.finalstreakkeeper;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class StatsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        SharedPreferences prefs = getSharedPreferences("streak_prefs", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString("habits_list", null);
        Type type = new TypeToken<ArrayList<Habit>>() {}.getType();
        ArrayList<Habit> habitList = json == null ? new ArrayList<>() : gson.fromJson(json, type);

        TextView totalStreaks = findViewById(R.id.totalStreaks);
        TextView bestStreakEver = findViewById(R.id.bestStreakEver);
        TextView totalDaysCompleted = findViewById(R.id.totalDaysCompleted);
        LinearProgressIndicator consistencyProgress = findViewById(R.id.consistencyProgress);

        int total = habitList.size();
        int best = 0;
        int completedToday = 0;

        for (Habit h : habitList) {
            if (h.streak > best) best = h.streak;
            if (h.isCompletedToday()) completedToday++;
        }

        totalStreaks.setText(String.valueOf(total));
        bestStreakEver.setText(best + " Days");
        
        int progress = total > 0 ? (completedToday * 100 / total) : 0;
        consistencyProgress.setProgress(progress);
        totalDaysCompleted.setText(progress + "% daily completion rate");
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