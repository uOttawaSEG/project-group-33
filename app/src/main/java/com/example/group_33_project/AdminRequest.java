package com.example.group_33_project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class AdminRequest extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen7_adminin_request);

        //FINDING THE ID OF BUTTONS
        Button Back = findViewById(R.id.screen7_back);

        //GOES BACK TO ADMININ SCREEN
        Back.setOnClickListener(v -> {
            Intent intent = new Intent(AdminRequest.this, AdminIn.class);
            startActivity(intent);
            finish();
        });
    }
}
