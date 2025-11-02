package com.example.group_33_project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.view.View;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class TutorUpcoming extends AppCompatActivity{

    private static final String SCREEN_SETTINGS = "ScreenSettings";
    private static final String BUTTON_VISIBILITY = "ButtonVisibility";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen11_upcomingtut);
        EdgeToEdge.enable(this);

        Button hiddenButton = findViewById(R.id.screen11_psessionsr);

        // RETRIEVE SHAREDPREFERENCES
        SharedPreferences prefs = getSharedPreferences(SCREEN_SETTINGS, Context.MODE_PRIVATE);

        // GET THE SAVED STATE OF THE CHECKBOX
        boolean shouldShowButton = prefs.getBoolean(BUTTON_VISIBILITY, false); // THE DEFAULT IS FALSE

        // SETS THE BUTTONS VISIBILITY
        if (shouldShowButton) {
            // CHECKBOX WAS CHECKED, ENSURES BUTTON IS VISIBLE
            hiddenButton.setVisibility(View.GONE);
        } else {
            // CHECKBOX WAS UNCHECKED, ENSURES BUTTON IS HIDDEN
            hiddenButton.setVisibility(View.VISIBLE);
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.tutorupcoming), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //FINDING THE ID OF BUTTONS
        Button Back = findViewById(R.id.screen11_back);
        Button Prequest = findViewById(R.id.screen11_psessionsr);

        //SWITCH TO PENDING SESSION REQUEST SCREEN
        Prequest.setOnClickListener(v -> {
            Intent intent = new Intent(TutorUpcoming.this, TutorPendingRequest.class);
            startActivity(intent);
            finish();
        });

        //GOES BACK TO TUTORIN SCREEN
        Back.setOnClickListener(v -> {
            Intent intent = new Intent(TutorUpcoming.this, TutorIn.class);
            startActivity(intent);
            finish();
        });
    }
}
