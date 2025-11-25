package com.example.group_33_project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class StudentIn extends BaseActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen15_studentin);
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.studentinscreen), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //sets up the logout button
        setupLogoutButton();

        //FINDING THE ID OF BUTTONS
        Button Asessions = findViewById(R.id.screen15_searchavailsessions);
        Button Bsessions = findViewById(R.id.screen15_bsessions);
        // Button Rsessions = findViewById(R.id.screen15_rsessions);
        //Button Csessions = findViewById(R.id.screen15_csessions);

        // Different Buttons
        //SWITCH TO SEARCH AVAILABILITY SCREEN
        Asessions.setOnClickListener(v -> {
            Intent intent = new Intent(StudentIn.this, StudentSearchAvailable.class);
            startActivity(intent);

        });

        //SWITCH TO BOOKED SESSIONS SCREEN
        Bsessions.setOnClickListener(v -> {
            Intent intent = new Intent(StudentIn.this, StudentBookedSession.class);
            startActivity(intent);

        });

        //SWITCH TO REQUESTED SESSIONS SCREEN
        /*
        Rsessions.setOnClickListener(v -> {
            Intent intent = new Intent(StudentIn.this, StudentRequestedSession.class);
            startActivity(intent);
        });

        //SWITCH TO CANCEL SESSIONS SCREEN
        Csessions.setOnClickListener(v -> {
            Intent intent = new Intent(StudentIn.this, StudentCancelSession.class);
            startActivity(intent);
        });*/
    }
}
