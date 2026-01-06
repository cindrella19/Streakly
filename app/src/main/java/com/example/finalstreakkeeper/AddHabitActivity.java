package com.example.finalstreakkeeper;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class AddHabitActivity extends AppCompatActivity {

    private LinearLayout container;
    private ProgressBar progressBar;
    private ArrayList<String> existingHabits;
    private RequestQueue requestQueue;
    private Set<String> fetchedIdeas = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_habit);

        container = findViewById(R.id.suggestedHabitsContainer);
        progressBar = findViewById(R.id.progressBar);
        requestQueue = Volley.newRequestQueue(this);

        existingHabits = getIntent().getStringArrayListExtra("existing_habits");
        if (existingHabits == null) existingHabits = new ArrayList<>();

        // Fetch initial batch of unique ideas from API
        fetchInitialIdeas(5);
    }

    private void fetchInitialIdeas(int count) {
        if (count <= 0) return;
        fetchNewIdea(new FetchCallback() {
            @Override
            public void onFetched() {
                fetchInitialIdeas(count - 1);
            }
        });
    }

    private void fetchNewIdea(FetchCallback callback) {
        // Using an alternative API if boredapi is slow or caching
        String url = "https://www.boredapi.com/api/activity?random=" + System.currentTimeMillis();

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        String activity = response.getString("activity");
                        if (!isAlreadyAdded(activity, existingHabits) && !fetchedIdeas.contains(activity)) {
                            fetchedIdeas.add(activity);
                            addIdeaToUi(activity);
                            if (callback != null) callback.onFetched();
                        } else {
                            fetchNewIdea(callback); // Try again for a unique one
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    // Fallback to local varied suggestions
                    String[] fallback = {"Read a book", "Go for a run", "Drink water", "Meditate", "Journaling"};
                    String picked = fallback[(int)(Math.random() * fallback.length)];
                    if (!fetchedIdeas.contains(picked)) {
                        fetchedIdeas.add(picked);
                        addIdeaToUi(picked);
                        if (callback != null) callback.onFetched();
                    }
                });

        requestQueue.add(request);
    }

    private void addIdeaToUi(String habitNameText) {
        View item = getLayoutInflater().inflate(R.layout.item_suggested_habit, container, false);
        TextView habitName = item.findViewById(R.id.habitName);
        habitName.setText(habitNameText);

        item.findViewById(R.id.addBtn).setOnClickListener(v -> {
            Intent data = new Intent();
            data.putExtra("habit_name", habitNameText);
            setResult(RESULT_OK, data);
            
            // Remove from UI
            container.removeView(item);
            fetchedIdeas.remove(habitNameText);
            
            finish();
        });

        container.addView(item);
    }

    private boolean isAlreadyAdded(String habit, ArrayList<String> existing) {
        for (String s : existing) {
            if (s.equalsIgnoreCase(habit)) return true;
        }
        return false;
    }

    interface FetchCallback {
        void onFetched();
    }
}