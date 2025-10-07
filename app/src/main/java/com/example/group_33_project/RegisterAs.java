package com.example.group_33_project;

import static com.example.group_33_project.R.*;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class RegisterAs extends AppCompatActivity {
    //Screen for registration before differentiating
    //SCREEN3

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(layout.screen3_registeras);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(id.registerasscreen), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //FINDING THE ID OF BUTTONS
        Button Student = findViewById(R.id.screen3_student);
        Button Tutor = findViewById(R.id.screen3_tutor);
        Button Back = findViewById(R.id.screen3_back);

        // Different Buttons
        //SWITCH TO REGISTER AS STUDENT SCREEN
        Student.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterAs.this, RegisterStu.class);
            startActivity(intent);
            finish();
        });

        //SWITCH TO REGISTER AS TUTOR SCREEN
        Tutor.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterAs.this, RegisterTut.class);
            startActivity(intent);
            finish();
        });

        //GOES BACK TO MAIN SCREEN
        Back.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterAs.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
