package com.example.finalstreakkeeper;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;

import java.util.List;

public class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.ViewHolder> {

    private List<Habit> habits;
    private Context context;
    private OnHabitChangeListener listener;

    public interface OnHabitChangeListener {
        void onHabitChanged();
    }

    public HabitAdapter(Context context, List<Habit> habits) {
        this.context = context;
        this.habits = habits;
    }

    public void setOnHabitChangeListener(OnHabitChangeListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_habit, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Habit habit = habits.get(position);

        holder.habitName.setText(habit.getTitle());
        holder.streak.setText("🔥 " + habit.getStreak());

        boolean completedToday = habit.isCompletedToday();
        holder.check.setText(completedToday ? "✓" : "○");
        holder.itemView.setAlpha(completedToday ? 0.4f : 1f);

        holder.check.setOnClickListener(v -> {

            int currentPos = holder.getAdapterPosition();
            if (currentPos == RecyclerView.NO_POSITION) return;

            Habit h = habits.get(currentPos);
            if (h.isCompletedToday()) return;

            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("users")
                    .document(uid)
                    .collection("habits")
                    .document(h.getId())
                    .update(
                            "streak", FieldValue.increment(1),
                            "completedToday", true,
                            "lastCompletedTime", System.currentTimeMillis()
                    )
                    .addOnSuccessListener(aVoid -> {
                        if (listener != null) listener.onHabitChanged();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(context, "Failed to update habit", Toast.LENGTH_SHORT).show()
                    );
        });

        holder.itemView.setOnLongClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Habit")
                    .setMessage("Are you sure you want to delete this habit?")
                    .setPositiveButton("Delete", (dialog, which) -> {

                        int currentPos = holder.getAdapterPosition();
                        if (currentPos == RecyclerView.NO_POSITION) return;

                        Habit h = habits.get(currentPos);

                        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(uid)
                                .collection("habits")
                                .document(h.getId())
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    if (listener != null) listener.onHabitChanged();
                                });
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return habits.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView check, habitName, streak;

        ViewHolder(View itemView) {
            super(itemView);
            check = itemView.findViewById(R.id.check);
            habitName = itemView.findViewById(R.id.habitName);
            streak = itemView.findViewById(R.id.streak);
        }
    }
}
