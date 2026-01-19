package com.example.finalstreakkeeper;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String PREF_NAME = "streak_prefs";
    private static final String HABITS_KEY = "habits_list";
    private static final String LAST_RESET_KEY = "last_reset_time";

    private DrawerLayout drawerLayout;
    private RecyclerView habitRecyclerView;
    private HabitAdapter adapter;
    private ArrayList<Habit> habitList;

    private SharedPreferences prefs;
    private FirebaseAuth mAuth;

    private TextView completedStreaksCount;
    private TextView activeStreaksCount;

    private final String[] quotes = {
            "Small steps every day lead to big change.",
            "Discipline is choosing between what you want now and what you want most.",
            "Your future self will thank you."
    };

    private final ActivityResultLauncher<Intent> addHabitLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            String habitName = result.getData().getStringExtra("habit_name");
                            if (habitName == null) return;

                            for (Habit h : habitList) {
                                if (h.getTitle().equalsIgnoreCase(habitName)) {
                                    Toast.makeText(this, "Habit already tracked!", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            }

                            habitList.add(new Habit(habitName));
                            adapter.notifyItemInserted(habitList.size() - 1);
                            saveHabits();
                            updateSummary();
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 🔐 Auth
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // 🧭 Toolbar + Drawer
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navView = findViewById(R.id.nav_view);
        navView.setNavigationItemSelectedListener(this);
        updateNavHeader(navView, user);

        ActionBarDrawerToggle toggle =
                new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                        R.string.app_name, R.string.app_name);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // 📊 Summary
        completedStreaksCount = findViewById(R.id.completedStreaksCount);
        activeStreaksCount = findViewById(R.id.activeStreaksCount);

        // 📅 Date + Quote
        TextView dayText = findViewById(R.id.dayText);
        TextView dateText = findViewById(R.id.dateText);
        TextView quoteText = findViewById(R.id.quoteText);

        Date now = new Date();
        dayText.setText(new SimpleDateFormat("EEEE", Locale.getDefault()).format(now));
        dateText.setText(new SimpleDateFormat("dd MMMM", Locale.getDefault()).format(now));

        long seed = System.currentTimeMillis() / (1000 * 60 * 60 * 24);
        quoteText.setText(quotes[new Random(seed).nextInt(quotes.length)]);

        // 💾 Local storage
        prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        habitList = loadHabits();

        // 📋 RecyclerView
        habitRecyclerView = findViewById(R.id.habitRecycler);
        habitRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HabitAdapter(this, habitList);
        adapter.setOnHabitChangeListener(this::updateSummary);
        habitRecyclerView.setAdapter(adapter);

        // 🗑 Swipe delete
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override public boolean onMove(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder vh, @NonNull RecyclerView.ViewHolder t) { return false; }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder vh, int dir) {
                int pos = vh.getAdapterPosition();
                Habit removed = habitList.remove(pos);
                adapter.notifyItemRemoved(pos);
                saveHabits();
                updateSummary();

                Snackbar.make(habitRecyclerView, "Habit deleted", Snackbar.LENGTH_LONG)
                        .setAction("UNDO", v -> {
                            habitList.add(pos, removed);
                            adapter.notifyItemInserted(pos);
                            saveHabits();
                            updateSummary();
                        }).show();
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView rv,
                                    @NonNull RecyclerView.ViewHolder vh,
                                    float dX, float dY, int state, boolean active) {
                if (dX < 0) {
                    View item = vh.itemView;
                    Paint p = new Paint();
                    p.setColor(Color.parseColor("#EF4444"));
                    c.drawRect(item.getRight() + dX, item.getTop(),
                            item.getRight(), item.getBottom(), p);

                    Drawable icon = ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_delete);
                    if (icon != null) {
                        int margin = (item.getHeight() - icon.getIntrinsicHeight()) / 2;
                        icon.setBounds(
                                item.getRight() - margin - icon.getIntrinsicWidth(),
                                item.getTop() + margin,
                                item.getRight() - margin,
                                item.getBottom() - margin
                        );
                        icon.draw(c);
                    }
                }
                super.onChildDraw(c, rv, vh, dX, dY, state, active);
            }
        }).attachToRecyclerView(habitRecyclerView);

        // ➕ Add habit
        FloatingActionButton addHabitBtn = findViewById(R.id.addHabitBtn);
        addHabitBtn.setOnClickListener(v -> showAddDialog());

        // 🔥 Firestore sync
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .collection("habits")
                .addSnapshotListener((value, error) -> {
                    if (value == null) return;
                    habitList.clear();
                    for (QueryDocumentSnapshot doc : value) {
                        Habit h = doc.toObject(Habit.class);
                        h.setId(doc.getId());
                        habitList.add(h);
                    }
                    adapter.notifyDataSetChanged();
                    updateSummary();
                });

        resetIfNewDay();
        updateSummary();
    }

    // ===== CORE METHODS =====

    private void updateSummary() {
        int completed = 0;
        int active = 0;

        for (Habit h : habitList) {
            if (h.isCompletedToday()) completed++;
            if (h.getStreak() > 0) active++;
        }

        completedStreaksCount.setText(String.valueOf(completed));
        activeStreaksCount.setText(String.valueOf(active));
    }

    private void showAddDialog() {
        EditText input = new EditText(this);
        new AlertDialog.Builder(this)
                .setTitle("New Habit")
                .setView(input)
                .setPositiveButton("Save", (d, w) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty()) {
                        habitList.add(new Habit(name));
                        adapter.notifyItemInserted(habitList.size() - 1);
                        saveHabits();
                        updateSummary();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void saveHabits() {
        prefs.edit().putString(HABITS_KEY, new Gson().toJson(habitList)).apply();
    }

    private ArrayList<Habit> loadHabits() {
        String json = prefs.getString(HABITS_KEY, null);
        Type type = new TypeToken<ArrayList<Habit>>() {}.getType();
        return json == null ? new ArrayList<>() : new Gson().fromJson(json, type);
    }

    private void resetIfNewDay() {
        long last = prefs.getLong(LAST_RESET_KEY, 0);
        long now = System.currentTimeMillis();
        if (now - last > 86400000L) {
            for (Habit h : habitList) {
                if (!h.isCompletedToday() && !h.wasCompletedYesterday()) {
                    h.setStreak(0);
                }
            }
            prefs.edit().putLong(LAST_RESET_KEY, now).apply();
            saveHabits();
        }
    }

    // ===== MENU & NAV =====

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateNavHeader(NavigationView nav, FirebaseUser user) {
        View h = nav.getHeaderView(0);
        ((TextView) h.findViewById(R.id.userName)).setText("Streakly User");
        ((TextView) h.findViewById(R.id.userEmail)).setText(user.getEmail());
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_streaks) {
            startActivity(new Intent(this, StreaksActivity.class));

        } else if (id == R.id.nav_stats) {
            startActivity(new Intent(this, StatsActivity.class));

        } else if (id == R.id.nav_settings) {
            startActivity(new Intent(this, SettingsActivity.class));

        } else if (id == R.id.nav_about) {
            startActivity(new Intent(this, AboutActivity.class));

        } else if (id == R.id.nav_logout) {
            mAuth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

}

