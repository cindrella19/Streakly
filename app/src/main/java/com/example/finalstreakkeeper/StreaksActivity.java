package com.example.finalstreakkeeper;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class StreaksActivity extends AppCompatActivity {

    private ArrayList<Habit> habitList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_streaks);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        loadData();

        TextView longestStreakEver = findViewById(R.id.longestStreakEver);
        TextView highestCurrentStreak = findViewById(R.id.highestCurrentStreak);
        RecyclerView recyclerView = findViewById(R.id.activeStreaksRecycler);

        int maxStreak = 0;
        int currentBest = 0;
        ArrayList<Habit> activeHabits = new ArrayList<>();

        for (Habit h : habitList) {
            if (h.streak > maxStreak) maxStreak = h.streak; // Simplified for now
            if (h.streak > currentBest) currentBest = h.streak;
            if (h.streak > 0) activeHabits.add(h);
        }

        longestStreakEver.setText(String.valueOf(maxStreak));
        highestCurrentStreak.setText(String.valueOf(currentBest));

        HabitAdapter adapter = new HabitAdapter(this, activeHabits);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadData() {
        SharedPreferences prefs = getSharedPreferences("streak_prefs", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString("habits_list", null);
        Type type = new TypeToken<ArrayList<Habit>>() {}.getType();
        habitList = json == null ? new ArrayList<>() : gson.fromJson(json, type);
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