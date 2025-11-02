package com.example.group_33_project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class TutorPendingRequest extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen12_prequesttut);
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.tutorprequest), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //FINDING THE ID OF BUTTONS
        Button Back = findViewById(R.id.screen12_back);

        //GOES BACK TO TUTORUPCOMING SCREEN
        Back.setOnClickListener(v -> {
            Intent intent = new Intent(TutorPendingRequest.this, TutorUpcoming.class);
            startActivity(intent);
            finish();
        });
    }
}
