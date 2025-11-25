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

public class StudentSearchAvailable extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen16_searchavailstud);
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.studentsearchsession), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //FINDING THE ID OF BUTTONS
        Button back = findViewById(R.id.button_back);

        MaterialCalendarView calendar = findViewById(R.id.calendarView);

        calendar.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget,
                                       @NonNull CalendarDay date, boolean selected) {

                int year = date.getYear();
                int month = date.getMonth() + 1; // Month is 0-indexed
                int day = date.getDay();

                String selectedDate = day + "/" + month + "/" + year;
                Log.d("Calendar", "User selected: " + selectedDate);
            }
        });

        calendar.setCurrentDate(CalendarDay.today());

        ArrayList<CalendarDay> highlightDates = new ArrayList<>();
        highlightDates.add(CalendarDay.from(2025, 11, 15)); // Jan 15 2025
        highlightDates.add(CalendarDay.from(2025, 11, 20));

        calendar.addDecorator(new HighlightStudent(highlightDates));


        // back button → return to studentIn
        back.setOnClickListener(v -> {
            Intent intent = new Intent(StudentSearchAvailable.this, StudentIn.class);
            startActivity(intent);
            finish();
        });
    }
}
