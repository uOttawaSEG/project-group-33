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

import java.time.ZonedDateTime;
import java.util.ArrayList;
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
    StudentLookupSessionAdapter adapter;

    //HELPER VARIABLES
    private HashMap<CalendarDay, List<TimeSlot>> sessionsByDay;
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
                Toast.makeText(StudentSearchAvailable.this, "please specify a course", Toast.LENGTH_SHORT).show();
                return;
            }

            currentCourse = currentCourse.trim().toUpperCase();
            currentCourse = currentCourse.replace(" ", "");

            if(currentCourse.length() != 7){
                currentCourse = "";
                Toast.makeText(StudentSearchAvailable.this, "invalid course code", Toast.LENGTH_SHORT).show();
                return;
            }

            //get the hashmap of <date, list of sessions of available tutors per date>
            sessionsByDay = new HashMap<>();
            highlightDates = new ArrayList<>();

            studHandle.searchSlotsByCourse(currentCourse, new SlotListCallback() {
                @Override
                public void onSuccess(List<TimeSlot> slots) {
                    for(TimeSlot slot: slots){
                        ZonedDateTime zdt = slot.getStartDate();
                        CalendarDay key = CalendarDay.from(
                                zdt.getYear(),
                                zdt.getMonthValue(),
                                zdt.getDayOfMonth()
                        );

                        if(!sessionsByDay.containsKey(key)){
                            List<TimeSlot> newList = new ArrayList<>();
                            newList.add(slot);
                            sessionsByDay.put(key, newList);
                            highlightDates.add(key);
                        }else{
                            List<TimeSlot> list = sessionsByDay.get(key);
                            list.add(slot);
                            sessionsByDay.put(key, list);
                        }
                    }

                    runOnUiThread(() -> {

                        // Add decorator once
                        calendar.addDecorator(new HighlightStudent(highlightDates));
                        CalendarDay selectedDay = calendar.getSelectedDate();
                        if (selectedDay == null) {
                            selectedDay = CalendarDay.today();
                        }
                        loadDay(sessionsByDay, selectedDay);
                    });
                }

                @Override
                public void onFailure(String error) {

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
                    Toast.makeText(StudentSearchAvailable.this, "no course chosen", Toast.LENGTH_SHORT).show();
                    return;
                }

                displayCourse.setText("Looking for tutoring sessions for: " + currentCourse);
                loadDay(sessionsByDay, date);

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

    public void loadDay( HashMap<CalendarDay, List<TimeSlot>> map, CalendarDay day) {
        List<TimeSlot> list = map.get(day);

        adapter = new StudentLookupSessionAdapter(list, (slot, pos) -> {
            bookSession(slot, day, map);
        });

        daySessions.setAdapter(adapter);

    }

    public void bookSession(TimeSlot s, CalendarDay d,  HashMap<CalendarDay, List<TimeSlot>> m){

        studHandle.bookSession(s, currentStudent, new AccountCallback() {
            @Override
            public void onSuccess(String msg) {
                Toast.makeText(StudentSearchAvailable.this, "applied for session" , Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onFailure(String msg) {
                Toast.makeText(StudentSearchAvailable.this, "failed to book", Toast.LENGTH_SHORT).show();
            }
        });

        List<TimeSlot> list = m.get(d);
        list.remove(s);
        m.put(d, list);
        adapter.notifyDataSetChanged();

    }
}
