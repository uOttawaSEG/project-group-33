package com.example.group_33_project;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class StudentBookedSession extends AppCompatActivity implements StudentSessionAdapter.OnCancelListener {
    private static final String TAG = "StudentBookedSession";
    public static final String CURRENT_STUDENT = "CURRENT_STUDENT";

    private StudentHandling studHandle;
    private Student currentStudent;
    private StudentSessionAdapter adapter;
    private RecyclerView studRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen17_stud_upcoming);

        studHandle = new StudentHandling();

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(CURRENT_STUDENT)) {
            currentStudent = (Student) intent.getSerializableExtra(CURRENT_STUDENT);
        }

        if (currentStudent == null) {
            Toast.makeText(this, "Error: Student information not found.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        studRecyclerView = findViewById(R.id.rv_studentStatus);
        studRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new StudentSessionAdapter(new ArrayList<>(), this);
        studRecyclerView.setAdapter(adapter);


        Button Psessions = findViewById(R.id.screen17_psessions);
        //SWITCH TO PAST SESSIONS SCREEN
        Psessions.setOnClickListener(v -> {
            Intent pSesIntent = new Intent(StudentBookedSession.this, StudentPastSession.class);
            pSesIntent.putExtra(CURRENT_STUDENT, currentStudent);
            startActivity(pSesIntent);
        });

        Button back = findViewById(R.id.button_back);
        // back button → return to studentIn
        back.setOnClickListener(v -> {
            Intent bIntent = new Intent(StudentBookedSession.this, StudentIn.class);
            bIntent.putExtra(CURRENT_STUDENT, currentStudent);
            startActivity(bIntent);
            finish();
        });

        fetchStudentSessions();
    }

    private void fetchStudentSessions() {
        // status = null fetches active sessions
        StudentHandling.getStudentSlots(currentStudent, null, new SlotListCallback() {
            @Override
            public void onSuccess(List<TimeSlot> slots) {
                // Filter to only show future sessions
                List<TimeSlot> upcomingSlots = new ArrayList<>();
                for (TimeSlot slot : slots) {
                    if (slot.getStartDate().isAfter(ZonedDateTime.now(slot.getStartDate().getZone()))) {
                        upcomingSlots.add(slot);
                    }
                }
                adapter.setSessions(upcomingSlots);
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Error fetching slots: " + error);
                Toast.makeText(StudentBookedSession.this, "Failed to load sessions: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    public void onCancelSession(TimeSlot slot) {

        studHandle.cancelSession(slot, new AccountCallback() {
            @Override
            public void onSuccess(String msg) {
                Toast.makeText(StudentBookedSession.this, msg, Toast.LENGTH_LONG).show();
                // Refresh the list immediately after successful cancellation
                fetchStudentSessions();
            }

            @Override
            public void onFailure(String msg) {
                Toast.makeText(StudentBookedSession.this, "Cancellation Failed: " + msg, Toast.LENGTH_LONG).show();
            }
        });
    }
}
