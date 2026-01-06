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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String PREF_NAME = "streak_prefs";
    private static final String HABITS_KEY = "habits_list";
    private static final String LAST_RESET_KEY = "last_reset_time";

    private SharedPreferences prefs;
    private ArrayList<Habit> habitList;
    private HabitAdapter adapter;
    private DrawerLayout drawerLayout;

    private TextView completedStreaksCount;
    private TextView activeStreaksCount;
    private FirebaseAuth mAuth;

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
                            if (habitName != null && habitList != null) {
                                boolean alreadyExists = false;
                                for (Habit h : habitList) {
                                    if (h.name.equalsIgnoreCase(habitName)) {
                                        alreadyExists = true;
                                        break;
                                    }
                                }
                                if (!alreadyExists) {
                                    habitList.add(new Habit(habitName));
                                    adapter.notifyItemInserted(habitList.size() - 1);
                                    saveHabits();
                                    updateSummary();
                                    Toast.makeText(MainActivity.this, "Habit added 🌱", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(MainActivity.this, "Habit already tracked!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        // -------- TOOLBAR & DRAWER --------
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);
            updateNavHeader(navigationView, currentUser);
        }

        if (drawerLayout != null && toolbar != null) {
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                    R.string.app_name, R.string.app_name);
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();
        }

        // -------- BACK NAVIGATION --------
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                    setEnabled(true);
                }
            }
        });

        // -------- FIND VIEWS --------
        completedStreaksCount = findViewById(R.id.completedStreaksCount);
        activeStreaksCount = findViewById(R.id.activeStreaksCount);
        TextView quoteText = findViewById(R.id.quoteText);
        
        Button ideasNavBtn = findViewById(R.id.ideasNavBtn);
        FloatingActionButton addHabitBtn = findViewById(R.id.addHabitBtn);
        
        TextView dayText = findViewById(R.id.dayText);
        TextView dateText = findViewById(R.id.dateText);
        RecyclerView recyclerView = findViewById(R.id.habitRecycler);

        // -------- DATE HEADER --------
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM", Locale.getDefault());
        Date now = new Date();
        if (dayText != null) dayText.setText(dayFormat.format(now));
        if (dateText != null) dateText.setText(dateFormat.format(now));

        // -------- DAILY QUOTE --------
        if (quoteText != null) {
            long today = System.currentTimeMillis() / (1000 * 60 * 60 * 24);
            Random random = new Random(today);
            int index = random.nextInt(quotes.length);
            quoteText.setText(quotes[index]);
        }

        // -------- LOAD DATA --------
        prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        habitList = loadHabits();

        if (habitList.isEmpty()) {
            habitList.add(new Habit("Drink 2 Liters Of Water", "water"));
            habitList.add(new Habit("Exercise", "exercise"));
            habitList.add(new Habit("Study English", "default"));
            saveHabits();
        }

        // -------- RECYCLER VIEW --------
        if (recyclerView != null) {
            adapter = new HabitAdapter(this, habitList);
            adapter.setOnHabitChangeListener(this::updateSummary);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(adapter);

            ItemTouchHelper.SimpleCallback swipeCallback =
                    new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
                        @Override
                        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                            return false;
                        }

                        @Override
                        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                            int position = viewHolder.getAdapterPosition();
                            if (position != RecyclerView.NO_POSITION) {
                                Habit deletedHabit = habitList.get(position);
                                habitList.remove(position);
                                adapter.notifyItemRemoved(position);
                                saveHabits();
                                updateSummary();

                                Snackbar.make(recyclerView, "Habit deleted", Snackbar.LENGTH_LONG)
                                        .setAction("UNDO", v -> {
                                            habitList.add(position, deletedHabit);
                                            adapter.notifyItemInserted(position);
                                            saveHabits();
                                            updateSummary();
                                        }).show();
                            }
                        }

                        @Override
                        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                            View itemView = viewHolder.itemView;
                            Paint paint = new Paint();
                            paint.setColor(Color.parseColor("#EF4444"));
                            if (dX < 0) {
                                c.drawRect(itemView.getRight() + dX, itemView.getTop(), itemView.getRight(), itemView.getBottom(), paint);
                                Drawable icon = ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_delete);
                                if (icon != null) {
                                    int margin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
                                    int top = itemView.getTop() + margin;
                                    int bottom = top + icon.getIntrinsicHeight();
                                    int left = itemView.getRight() - margin - icon.getIntrinsicWidth();
                                    int right = itemView.getRight() - margin;
                                    icon.setBounds(left, top, right, bottom);
                                    icon.draw(c);
                                }
                            }
                            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                        }
                    };
            new ItemTouchHelper(swipeCallback).attachToRecyclerView(recyclerView);
        }

        // -------- LISTENERS --------
        if (addHabitBtn != null) {
            addHabitBtn.setOnClickListener(v -> showAddDialog());
        }
        
        if (ideasNavBtn != null) {
            ideasNavBtn.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, AddHabitActivity.class);
                ArrayList<String> existingNames = new ArrayList<>();
                for (Habit h : habitList) existingNames.add(h.name);
                intent.putStringArrayListExtra("existing_habits", existingNames);
                addHabitLauncher.launch(intent);
            });
        }

        resetIfNewDay();
        updateSummary();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_profile) {
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateNavHeader(NavigationView navigationView, FirebaseUser user) {
        if (navigationView.getHeaderCount() > 0) {
            View headerView = navigationView.getHeaderView(0);
            TextView userName = headerView.findViewById(R.id.userName);
            TextView userEmail = headerView.findViewById(R.id.userEmail);
            
            if (userName != null) {
                if (user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
                    userName.setText(user.getDisplayName());
                } else {
                    userName.setText("Streakly User");
                }
            }
            if (userEmail != null) userEmail.setText(user.getEmail());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAuth != null && mAuth.getCurrentUser() != null) {
            if (habitList != null) {
                habitList.clear();
                habitList.addAll(loadHabits());
                updateSummary();
                if (adapter != null) adapter.notifyDataSetChanged();
            }
        }
    }

    public void updateSummary() {
        if (completedStreaksCount == null || activeStreaksCount == null || habitList == null) return;
        int completed = 0;
        int active = 0;
        for (Habit h : habitList) {
            if (h.isCompletedToday()) completed++;
            if (h.streak > 0) active++;
        }
        completedStreaksCount.setText(String.valueOf(completed));
        activeStreaksCount.setText(String.valueOf(active));
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_home) {
            // Already on Home
        } else if (id == R.id.nav_streaks) {
            startActivity(new Intent(this, StreaksActivity.class));
        } else if (id == R.id.nav_stats) {
            startActivity(new Intent(this, StatsActivity.class));
        } else if (id == R.id.nav_help) {
            startActivity(new Intent(this, HelpActivity.class));
        } else if (id == R.id.nav_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        } else if (id == R.id.nav_about) {
            startActivity(new Intent(this, AboutActivity.class));
        } else if (id == R.id.nav_logout) {
            logoutUser();
        }
        if (drawerLayout != null) drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void logoutUser() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    mAuth.signOut();
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showAddDialog() {
        EditText input = new EditText(this);
        input.setHint("Habit name");
        new AlertDialog.Builder(this)
                .setTitle("New Habit")
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty()) {
                        habitList.add(new Habit(name, "default"));
                        adapter.notifyItemInserted(habitList.size() - 1);
                        saveHabits();
                        updateSummary();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void saveHabits() {
        if (prefs == null || habitList == null) return;
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        editor.putString(HABITS_KEY, gson.toJson(habitList));
        editor.apply();
    }

    private ArrayList<Habit> loadHabits() {
        try {
            Gson gson = new Gson();
            String json = prefs.getString(HABITS_KEY, null);
            Type type = new TypeToken<ArrayList<Habit>>() {}.getType();
            ArrayList<Habit> list = json == null ? new ArrayList<>() : gson.fromJson(json, type);
            return list != null ? list : new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private void resetIfNewDay() {
        if (prefs == null || habitList == null) return;
        long lastReset = prefs.getLong(LAST_RESET_KEY, 0);
        long now = System.currentTimeMillis();
        java.util.Calendar last = java.util.Calendar.getInstance();
        java.util.Calendar current = java.util.Calendar.getInstance();
        last.setTimeInMillis(lastReset);
        current.setTimeInMillis(now);

        if (last.get(java.util.Calendar.DAY_OF_YEAR) != current.get(java.util.Calendar.DAY_OF_YEAR)
            || last.get(java.util.Calendar.YEAR) != current.get(java.util.Calendar.YEAR)) {
            
            for (Habit h : habitList) {
                if (!h.isCompletedToday() && !h.wasCompletedYesterday()) {
                    h.setStreak(0);
                }
            }
            
            prefs.edit().putLong(LAST_RESET_KEY, now).apply();
            saveHabits();
            if (adapter != null) adapter.notifyDataSetChanged();
            updateSummary();
        }
    }
}