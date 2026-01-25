package com.example.finalstreakkeeper;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class MotivationActivity extends AppCompatActivity {

    private TextView quoteText;
    private TextView authorText;
    private ProgressBar progressBar;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_motivation);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        quoteText = findViewById(R.id.quoteText);
        authorText = findViewById(R.id.authorText);
        progressBar = findViewById(R.id.progressBar);
        requestQueue = Volley.newRequestQueue(this);

        findViewById(R.id.refreshQuoteBtn).setOnClickListener(v -> fetchQuote());
        fetchQuote(); // Initial fetch
    }

    private void fetchQuote() {
        progressBar.setVisibility(View.VISIBLE);
        String url = "https://zenquotes.io/api/random";

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    try {
                        JSONObject quoteObject = response.getJSONObject(0);
                        String quote = quoteObject.getString("q");
                        String author = quoteObject.getString("a");

                        quoteText.setText("“" + quote + "”");
                        authorText.setText("— " + author);
                    } catch (JSONException e) {
                        Toast.makeText(this, "Failed to parse quote.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to fetch quote.", Toast.LENGTH_SHORT).show();
                });
        requestQueue.add(request);
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
