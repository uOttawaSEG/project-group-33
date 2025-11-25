package com.example.group_33_project;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.util.ArrayList;

public class StudentBookedSession extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen17_stud_upcoming);
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.studentbookdsession), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //FINDING THE ID OF BUTTONS
        Button back = findViewById(R.id.button_back);
        Button Psessions = findViewById(R.id.screen17_psessions);

        // back button → return to studentIn
        back.setOnClickListener(v -> {
            Intent intent = new Intent(StudentBookedSession.this, StudentIn.class);
            startActivity(intent);
            finish();
        });

        //SWITCH TO PAST SESSIONS SCREEN
        Psessions.setOnClickListener(v -> {
            Intent intent = new Intent(StudentBookedSession.this, StudentPastSession.class);
            startActivity(intent);
        });
    }
}
