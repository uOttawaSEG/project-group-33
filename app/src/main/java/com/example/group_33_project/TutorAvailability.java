package com.example.group_33_project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class TutorAvailability extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen10_availabilitytut);
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.tutoravailability), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //FINDING THE ID OF BUTTONS
        Button Back = findViewById(R.id.screen10_back);

        //GOES BACK TO TUTORIN SCREEN
        Back.setOnClickListener(v -> {
            Intent intent = new Intent(TutorAvailability.this, TutorIn.class);
            startActivity(intent);
            finish();
        });
    }
}
