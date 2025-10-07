package com.example.group_33_project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LoggedIn extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.screen2_inside);  // keep your screen2.xml layout

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.loggedscreen), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Grab the role passed from MainActivity
        String role = getIntent().getStringExtra("role");

        // Set text views
        TextView welcomeText = findViewById(R.id.screen2_welcome);
        TextView bodyText = findViewById(R.id.screen2_body);

        welcomeText.setText("Welcome!");

        if ("Admin".equalsIgnoreCase(role)) {
            bodyText.setText("You are logged in as an Administrator");
        } else if ("Student".equalsIgnoreCase(role)) {
            bodyText.setText("You are logged in as a Student");
        } else if ("Tutor".equalsIgnoreCase(role)) {
            bodyText.setText("You are logged in as a Tutor");
        } else {
            bodyText.setText("Role information unavailable.");
        }

        Button logout = findViewById(R.id.screen2_logout);

        logout.setOnClickListener(v -> {
            Intent intent = new Intent(LoggedIn.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
