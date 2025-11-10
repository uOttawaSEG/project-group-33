package com.example.group_33_project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.content.SharedPreferences;
import android.content.Context;
import android.widget.CheckBox;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

public class TutorIn extends AppCompatActivity {

    private static final String SCREEN_SETTINGS = "ScreenSettings";
    private static final String BUTTON_VISIBILITY = "ButtonVisibility";
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Tutor currentTutor = (Tutor) getIntent().getSerializableExtra("tutor"); //get tutor as an object

        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen9_tutorin);
        EdgeToEdge.enable(this);

        CheckBox showButtonCheckbox = findViewById(R.id.auto_approve_checkbox);

        // LOADS THE PREVIOUSLY SAVED STATE, CHECKS THE CHECKBOX
        SharedPreferences prefs = getSharedPreferences(SCREEN_SETTINGS, Context.MODE_PRIVATE);
        boolean isChecked = prefs.getBoolean(BUTTON_VISIBILITY, false); // THE DEFAULT IS FALSE
        showButtonCheckbox.setChecked(isChecked);

        // LISTEN FOR IF CHECKBOX IS CHECKED, UPDATES AS NECESSARY
        showButtonCheckbox.setOnCheckedChangeListener((buttonView, isCheckedNew) -> {
            SharedPreferences.Editor editor = prefs.edit();

            // SAVES THE CURRENT CHECKED STATE
            editor.putBoolean(BUTTON_VISIBILITY, isCheckedNew);
            editor.apply();
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.tutorinscreen), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //FINDING THE ID OF BUTTONS
        Button Logout = findViewById(R.id.screen9_logout);
        Button Availability = findViewById(R.id.screen9_availability);
        Button Usessions = findViewById(R.id.screen9_usessions);
        Button Psessions = findViewById(R.id.screen9_psessions);
        Button deleteAvailability = findViewById(R.id.screen9_deleteavailability);

        // Different Buttons
        //SWITCH TO TUTOR AVAILABILITY SCREEN
        Availability.setOnClickListener(v -> {
            Intent intent = new Intent(TutorIn.this, TutorAvailability.class);
            intent.putExtra("tutor", currentTutor);  //pass tutor object
            startActivity(intent);

        });

        //SWITCH TO UPCOMING SESSIONS SCREEN
        Usessions.setOnClickListener(v -> {
            Intent intent = new Intent(TutorIn.this, TutorUpcoming.class);
            intent.putExtra("tutor", currentTutor);  //pass tutor object
            startActivity(intent);

        });

        //SWITCH TO PAST SESSIONS SCREEN
        Psessions.setOnClickListener(v -> {
            Intent intent = new Intent(TutorIn.this, TutorPast.class);
            intent.putExtra("tutor", currentTutor);  //pass tutor object
            startActivity(intent);
        });

        //LOGOUT BUTTON
        Logout.setOnClickListener(v -> {
            Intent intent = new Intent(TutorIn.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        //SWITCH TO DELETE AVAILABILITY SCREEN
        deleteAvailability.setOnClickListener(v -> {
            Intent intent = new Intent(TutorIn.this, TutorDeleteAvailability.class);
            intent.putExtra("tutor", currentTutor);  //pass tutor object
            startActivity(intent);

        });
    }
}
