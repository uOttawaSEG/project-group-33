package com.example.group_33_project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class StudentPastSession extends AppCompatActivity {
    public static final String CURRENT_STUDENT = "CURRENT_STUDENT";
    private Student currentStudent;

    private Button back;
    private RecyclerView pastSessions;
    private StudentHandling studHandle;
    private StudentPastSessionAdapter adapter;
    private List<TimeSlot> slots = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen18_pastsessionstud);

        // Window adjustments
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.studentpastsession), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // GET CURRENT STUDENT
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(CURRENT_STUDENT)) {
            currentStudent = (Student) intent.getSerializableExtra(CURRENT_STUDENT);
        }

        if (currentStudent == null) {
            Toast.makeText(this, "Error: Student information not found.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // FIND VIEWS
        back = findViewById(R.id.button_back);
        pastSessions = findViewById(R.id.rv_studentpast);

        studHandle = new StudentHandling();

        pastSessions.setLayoutManager(new LinearLayoutManager(this));

        // INITIALIZE EMPTY ADAPTER FIRST
        adapter = new StudentPastSessionAdapter(slots, (slot, rating, pos) -> {
            Tutor t = slot.getTutor();
            t.rate(rating);
            slot.isRated();
        });
        pastSessions.setAdapter(adapter);

        loadPastSessions();

        back.setOnClickListener(v -> {
            Intent bIntent = new Intent(StudentPastSession.this, StudentBookedSession.class);
            bIntent.putExtra(CURRENT_STUDENT, currentStudent);
            startActivity(bIntent);
            finish();
        });
    }

    private void loadPastSessions() {
        studHandle.getStudentSlots(currentStudent, null, new SlotListCallback() {
            @Override
            public void onSuccess(List<TimeSlot> allSlots) {

                ZonedDateTime now = ZonedDateTime.now();

                List<TimeSlot> past = new ArrayList<>();

                for (TimeSlot slot : allSlots) {
                    if (slot.getEndDate().isBefore(now)) {
                        past.add(slot);
                    }
                }

                slots.clear();
                slots.addAll(past);

                runOnUiThread(() -> adapter.notifyDataSetChanged());
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(StudentPastSession.this, "Failed to load sessions", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
