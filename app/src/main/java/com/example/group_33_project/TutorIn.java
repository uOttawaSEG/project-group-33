package com.example.group_33_project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class TutorIn extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Tutor currentTutor = (Tutor) getIntent().getSerializableExtra("tutor"); // get tutor as an object

        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen9_tutorin);
        EdgeToEdge.enable(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.tutorinscreen), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // FINDING THE ID OF BUTTONS
        Button Availability = findViewById(R.id.screen9_availability);
        Button Usessions = findViewById(R.id.screen9_usessions);
        Button Psessions = findViewById(R.id.screen9_psessions);
        Button deleteAvailability = findViewById(R.id.screen9_deleteavailability);

        // SWITCH TO TUTOR AVAILABILITY SCREEN
        Availability.setOnClickListener(v -> {
            Intent intent = new Intent(TutorIn.this, TutorAvailability.class);
            intent.putExtra("tutor", currentTutor);  // pass tutor object
            startActivity(intent);
        });

        // SWITCH TO UPCOMING SESSIONS SCREEN
        Usessions.setOnClickListener(v -> {
            Intent intent = new Intent(TutorIn.this, TutorUpcoming.class);
            intent.putExtra("tutor", currentTutor);  // pass tutor object
            startActivity(intent);
        });

        // SWITCH TO PAST SESSIONS SCREEN
        Psessions.setOnClickListener(v -> {
            Intent intent = new Intent(TutorIn.this, TutorPast.class);
            intent.putExtra("tutor", currentTutor);  // pass tutor object
            startActivity(intent);
        });

        // SWITCH TO DELETE AVAILABILITY SCREEN
        deleteAvailability.setOnClickListener(v -> {
            Intent intent = new Intent(TutorIn.this, TutorDeleteAvailability.class);
            intent.putExtra("tutor", currentTutor);  // pass tutor object
            startActivity(intent);
        });

        // sets up the logout button
        setupLogoutButton();
    }
}
