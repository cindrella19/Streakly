package com.example.finalstreakkeeper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class IdeasAdapter extends RecyclerView.Adapter<IdeasAdapter.ViewHolder> {

    Context context;
    List<String> ideas;

    public IdeasAdapter(Context context, List<String> ideas) {
        this.context = context;
        this.ideas = ideas;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_idea, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        String idea = ideas.get(position);
        holder.title.setText(idea);

        holder.addBtn.setOnClickListener(v -> {

            String uid = FirebaseAuth.getInstance().getUid();
            if (uid == null) {
                Toast.makeText(context, "Not logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            Habit habit = new Habit(idea);

            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid)
                    .collection("habits")
                    .add(habit)
                    .addOnSuccessListener(doc ->
                            Toast.makeText(context, "Habit added", Toast.LENGTH_SHORT).show()
                    )
                    .addOnFailureListener(e ->
                            Toast.makeText(context, "Failed to add", Toast.LENGTH_SHORT).show()
                    );
        });
    }

    @Override
    public int getItemCount() {
        return ideas.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        ImageView addBtn;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.ideaTitle);
            addBtn = itemView.findViewById(R.id.addBtn);
        }
    }
}
