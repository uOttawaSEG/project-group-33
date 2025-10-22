package com.example.group_33_project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class AdminRejected extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen8_adminin_rejected);

        //FINDING THE ID OF BUTTONS
        Button Back = findViewById(R.id.screen8_back);

        //GOES BACK TO ADMININ SCREEN
        Back.setOnClickListener(v -> {
            Intent intent = new Intent(AdminRejected.this, AdminIn.class);
            startActivity(intent);
            finish();
        });
    }
}
