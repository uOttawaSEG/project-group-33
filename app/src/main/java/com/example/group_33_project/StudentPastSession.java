package com.example.group_33_project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class StudentPastSession extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen18_pastsessionstud);
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.studentpastsession), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //FINDING THE ID OF BUTTONS
        Button back = findViewById(R.id.button_back);
        //Button ratetutor = findViewById(R.id.screen19_rtutor);

        // back button → return to studentBookedSession
        back.setOnClickListener(v -> {
            finish();
        });

        //SWITCH TO RATE TUTOR SCREEN

        /*
        ratetutor.setOnClickListener(v -> {
            Intent intent = new Intent(StudentPastSession.this, StudentRate.class);
            startActivity(intent);
        });*/
    }
}
