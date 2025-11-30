package com.example.group_33_project;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class StudentSearchAvailable extends AppCompatActivity {
    public static final String CURRENT_STUDENT = "CURRENT_STUDENT";
    private Student currentStudent;
    private Button back, search;
    private MaterialCalendarView calendar;
    private RecyclerView  daySessions;
    private EditText course;
    private TextView displayCourse;

    private StudentHandling studHandle;
    private StudentLookupSessionAdapter adapter;

    private String status = "f";

    //HELPER VARIABLES
    private HashMap<LocalDate, List<TimeSlot>> sessionsByDay;
    private ArrayList<CalendarDay> highlightDates;
    private String currentCourse = "";

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

        //GET THE THE CURRENT STUDENT

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(CURRENT_STUDENT)) {
            currentStudent = (Student) intent.getSerializableExtra(CURRENT_STUDENT);
        }

        if (currentStudent == null) {
            Toast.makeText(this, "Error: Student information not found.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        studHandle = new StudentHandling();

        //FINDING THE ID THE ITEMS
        back = findViewById(R.id.button_back);
        search = findViewById(R.id.bSearch);
        calendar = findViewById(R.id.calendarView);
        daySessions  = findViewById(R.id.rv_potentialsessions);
        course = findViewById(R.id.courseGetter);
        displayCourse = findViewById(R.id.tvDisplayCourse);

        daySessions.setLayoutManager(new LinearLayoutManager(this));

        //OPEN CALENDAR ON TODAY'S DATE
        calendar.setCurrentDate(CalendarDay.today());

        //THE LOGIC

        //when search is clicked, verify the course and find all the days with tutors who teach the corresponding course
        //preferably store as hashmap< day, list of sessions >
        //change the display tv to the course we are looking for
        //when a day is clicked/ load and show the corresponding sessions

        search.setOnClickListener(v -> {
            //get course
            currentCourse = course.getText().toString().trim();

            //verify the course
            if (currentCourse.isEmpty() ) {
                currentCourse = "";
                Toast.makeText(StudentSearchAvailable.this, "please specify a subject", Toast.LENGTH_SHORT).show();
                return;
            }

            currentCourse = currentCourse.trim().toUpperCase();
            currentCourse = currentCourse.replace(" ", "");

            if(currentCourse.length() != 3){
                currentCourse = "";
                Toast.makeText(StudentSearchAvailable.this, "invalid subject code", Toast.LENGTH_SHORT).show();
                return;
            }


            //get the hashmap of <date, list of sessions of available tutors per date>
            sessionsByDay = new HashMap<>();
            highlightDates = new ArrayList<>();

            studHandle.searchSlotsByCourse(currentCourse, new SlotListCallback() {
                @Override
                public void onSuccess(List<TimeSlot> slots) {

                    if (slots.isEmpty()){
                        status = "n";
                        displayCourse.setText("");
                        calendar.removeDecorators();
                        CalendarDay selected = calendar.getSelectedDate();
                        if (selected == null) selected = CalendarDay.today();

                        int month = selected.getMonth();
                        //if (month < 1) month += 1;  // handles older versions

                        LocalDate l = LocalDate.of(
                                selected.getYear(),
                                month,
                                selected.getDay()
                        );

                        loadDay(l);
                        Toast.makeText(StudentSearchAvailable.this, "did not find sessions", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    status = "f";
                    Toast.makeText(StudentSearchAvailable.this, "found " + slots.size(), Toast.LENGTH_SHORT).show();

                    for(TimeSlot slot: slots){

                        // Convert ZonedDateTime to device local date
                        LocalDate key = slot.getStartDate().toLocalDate();

                        // Add to map
                        sessionsByDay.putIfAbsent(key, new ArrayList<>());
                        sessionsByDay.get(key).add(slot);

                        // Add to highlight list
                        highlightDates.add(CalendarDay.from(key.getYear(), key.getMonthValue()-1, key.getDayOfMonth()));

                    }

                    displayCourse.setText(currentCourse);

                    runOnUiThread(() -> {

                        // Add decorator once
                        calendar.removeDecorators();       // remove old highlights
                        calendar.addDecorator(new HighlightStudent(highlightDates));
                        calendar.invalidateDecorators();   // force redraw

                        CalendarDay selected = calendar.getSelectedDate();
                        if (selected == null) selected = CalendarDay.today();

                        int month = selected.getMonth() + 1;
                        //if (month < 1) month += 1;  // handles older versions

                        LocalDate l = LocalDate.of(
                                selected.getYear(),
                                month,
                                selected.getDay()
                        );

                        loadDay(l);


                    });
                }

                @Override
                public void onFailure(String error) {
                    Toast.makeText(StudentSearchAvailable.this, "failed", Toast.LENGTH_SHORT).show();
                    Toast.makeText(StudentSearchAvailable.this, error ,Toast.LENGTH_SHORT).show();
                }
            });
            //highlight the days on the calendar


            //IF DOES NOT UPDATE AUTOMATICALLY
            //get the date currently chosen on the calendar(one is definitely chosen)
            //update the recyclerview with new list of daily sessions

        });

        calendar.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget,
                                       @NonNull CalendarDay date, boolean selected) {

                //LOGIC
                //if a course has been chosen:
                //get the date currently chosen on the calendar
                //update the recyclerview with new list of daily sessions

                if("".equals(currentCourse)){
                    Toast.makeText(StudentSearchAvailable.this, "no subject chosen", Toast.LENGTH_SHORT).show();
                    return;
                }

                if("n".equals(status)){
                    Toast.makeText(StudentSearchAvailable.this, "no sessions today", Toast.LENGTH_SHORT).show();
                    return;
                }

                //CalendarDay selected = calendar.getSelectedDate();
                int month = date.getMonth()+1;
                //if (month < 1) month += 1;  // handles older versions

                LocalDate l = LocalDate.of(
                        date.getYear(),
                        month,
                        date.getDay()
                );

                loadDay(l);

            }
        });

        // back button → return to studentIn
        back.setOnClickListener(v -> {
            Intent bIntent = new Intent(StudentSearchAvailable.this, StudentIn.class);
            bIntent.putExtra(CURRENT_STUDENT, currentStudent);
            startActivity(bIntent);
            finish();
        });
    }

    public void loadDay( LocalDate day) {
        List<TimeSlot> list = sessionsByDay.get(day);

        if (adapter == null) {
            adapter = new StudentLookupSessionAdapter(list != null ? list : new ArrayList<>(),
                    (slot, pos) -> bookSession(pos, slot, day));
            daySessions.setAdapter(adapter);
        } else {
            adapter = new StudentLookupSessionAdapter(list != null ? list : new ArrayList<>(),
                    (slot, pos) -> bookSession(pos, slot, day));
            daySessions.setAdapter(adapter);
        }

        if (list == null || list.isEmpty()) {
            Toast.makeText(this, "No sessions today", Toast.LENGTH_SHORT).show();
        }
    }

    public void bookSession(int pos,TimeSlot s, LocalDate d) {
        studHandle.bookSession(s, currentStudent, new AccountCallback() {
            @Override
            public void onSuccess(String msg) {
                runOnUiThread(() -> {
                    Toast.makeText(StudentSearchAvailable.this, "Applied for session", Toast.LENGTH_SHORT).show();

                    List<TimeSlot> list = sessionsByDay.get(d);
                    if (list != null){
                        list.remove(pos);
                        if(list.isEmpty()){
                            sessionsByDay.remove(d);

                            // Remove the highlight day properly
                            CalendarDay toRemove = CalendarDay.from(d.getYear(), d.getMonthValue()-1, d.getDayOfMonth());
                            highlightDates.removeIf(cd -> cd.equals(toRemove));

                            calendar.removeDecorators();
                            calendar.addDecorator(new HighlightStudent(highlightDates));
                            calendar.invalidateDecorators();
                        }
                    }

                    loadDay(d);

                });
            }

            @Override
            public void onFailure(String msg) {
                runOnUiThread(() -> Toast.makeText(StudentSearchAvailable.this, "Failed to book", Toast.LENGTH_SHORT).show());
            }
        });
    }
}
