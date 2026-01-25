package com.example.finalstreakkeeper;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.List;

public class IdeasActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    IdeasAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ideas);

        recyclerView = findViewById(R.id.ideasRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 🔹 20 habit ideas (inline, no extra class)
        List<String> ideas = Arrays.asList(
                "Drink Water",
                "Morning Walk",
                "Read 10 Pages",
                "Meditate",
                "Workout",
                "Sleep Early",
                "No Junk Food",
                "Journaling",
                "Stretching",
                "Learn One Word",
                "Code 30 Minutes",
                "Pray",
                "Cold Shower",
                "Healthy Breakfast",
                "No Phone After 10 PM",
                "Gratitude List",
                "Push-ups",
                "Walk 5k Steps",
                "Cycling",
                "Deep Breathing"
        );

        adapter = new IdeasAdapter(this, ideas);
        recyclerView.setAdapter(adapter);
    }
}
