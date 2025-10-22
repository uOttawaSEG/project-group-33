package com.example.group_33_project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class AdminIn extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen6_adminin);

        //FINDING THE ID OF BUTTONS
        Button logout = findViewById(R.id.screen6_logout);
        Button request = findViewById(R.id.screen6_requests);
        Button reject = findViewById(R.id.screen6_rejected);

        // Different Buttons
        //SWITCH TO REGISTRATION REQUESTS SCREEN
        request.setOnClickListener(v -> {
            Intent intent = new Intent(AdminIn.this, AdminRequest.class);
            startActivity(intent);
            finish();
        });

        //SWITCH TO REJECTED REQUESTS SCREEN
        reject.setOnClickListener(v -> {
            Intent intent = new Intent(AdminIn.this, AdminRejected.class);
            startActivity(intent);
            finish();
        });

        //LOGOUT BUTTON
        logout.setOnClickListener(v -> {
            Intent intent = new Intent(AdminIn.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
