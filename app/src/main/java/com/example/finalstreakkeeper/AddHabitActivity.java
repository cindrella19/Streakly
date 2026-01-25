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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;

import java.util.HashMap;
import java.util.Map;

import android.content.Intent;
import android.widget.Toast;

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

        if (habitName == null) {
            android.util.Log.e("IDEAS", "habitName TextView is NULL. Check item_suggested_habit.xml");
            return; // prevents crash
        }

        habitName.setText(habitNameText);


        item.findViewById(R.id.addIdeaBtn).setOnClickListener(v -> {

            // 1️⃣ Save habit to Firestore
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            Map<String, Object> habitData = new HashMap<>();
            habitData.put("title", habitNameText);
            habitData.put("streak", 0);
            habitData.put("completedToday", false);
            habitData.put("lastCompletedDate", null);
            habitData.put("createdAt", FieldValue.serverTimestamp());

            db.collection("users")
                    .document(uid)
                    .collection("habits")
                    .add(habitData)
                    .addOnSuccessListener(docRef -> {

                        // 2️⃣ Update activeHabits count
                        db.collection("users")
                                .document(uid)
                                .update("activeHabits", FieldValue.increment(1));

                        // 3️⃣ Send result back to UI
                        Intent data = new Intent();
                        data.putExtra("habit_name", habitNameText);
                        setResult(RESULT_OK, data);

                        // 4️⃣ Remove from UI
                        container.removeView(item);


                        fetchedIdeas.remove(habitNameText);

                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(AddHabitActivity.this,
                                "Failed to save habit: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    });
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