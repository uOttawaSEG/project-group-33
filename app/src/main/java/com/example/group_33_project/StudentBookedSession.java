package com.example.group_33_project;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class StudentBookedSession extends AppCompatActivity implements StudentSessionAdapter.OnCancelListener {

    private static final String TAG = "StudentBookedSession";
    public static final String CURRENT_STUDENT = "CURRENT_STUDENT";

    private StudentHandling studHandle;
    private Student currentStudent;
    private StudentSessionAdapter adapter;
    private RecyclerView studRecyclerView;

    // Upcoming sessions for export/email
    private final List<TimeSlot> upcomingSlots = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.screen17_stud_upcoming);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.studentbookdsession), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        studHandle = new StudentHandling();

        // Get current student from intent
        Intent intent = getIntent();
        if (intent != null) {
            Object accountExtra = intent.getSerializableExtra(CURRENT_STUDENT);
            if (accountExtra instanceof Student) {
                currentStudent = (Student) accountExtra;
            } else {
                accountExtra = intent.getSerializableExtra("student");
                if (accountExtra instanceof Student) {
                    currentStudent = (Student) accountExtra;
                }
            }
        }

        if (currentStudent == null) {
            Toast.makeText(this, "Error: no student found.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        studRecyclerView = findViewById(R.id.rv_studentStatus);
        studRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StudentSessionAdapter(new ArrayList<>(), this);
        studRecyclerView.setAdapter(adapter);

        Button pastSessionsButton = findViewById(R.id.screen17_psessions);
        pastSessionsButton.setOnClickListener(v -> {
            Intent pSesIntent = new Intent(StudentBookedSession.this, StudentPastSession.class);
            pSesIntent.putExtra(CURRENT_STUDENT, currentStudent);
            startActivity(pSesIntent);
        });

        Button backButton = findViewById(R.id.button_back);
        backButton.setOnClickListener(v -> {
            Intent bIntent = new Intent(StudentBookedSession.this, StudentIn.class);
            bIntent.putExtra(CURRENT_STUDENT, currentStudent);
            startActivity(bIntent);
            finish();
        });

        // Local export button (optional, keep or remove if you want)
        Button exportCalendarButton = findViewById(R.id.btn_export_calendar);
        exportCalendarButton.setOnClickListener(v -> exportSessionsToCalendar());

        // NEW: email calendar button
        Button emailCalendarButton = findViewById(R.id.btn_email_calendar);
        emailCalendarButton.setOnClickListener(v -> emailCalendarFile());

        fetchStudentSessions();
    }

    private void fetchStudentSessions() {
        StudentHandling.getStudentSlots(currentStudent, null, new SlotListCallback() {
            @Override
            public void onSuccess(List<TimeSlot> slots) {
                List<TimeSlot> futureSlots = new ArrayList<>();
                ZonedDateTime now = ZonedDateTime.now(
                        slots.isEmpty()
                                ? ZonedDateTime.now().getZone()
                                : slots.get(0).getStartDate().getZone()
                );

                for (TimeSlot slot : slots) {
                    if (slot.getStartDate().isAfter(now)) {
                        futureSlots.add(slot);
                    }
                }

                upcomingSlots.clear();
                upcomingSlots.addAll(futureSlots);

                adapter.setSessions(futureSlots);
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Error fetching slots: " + error);
                Toast.makeText(StudentBookedSession.this,
                        "Failed to load sessions: " + error,
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onCancelSession(TimeSlot slot) {
        studHandle.cancelSession(slot, new AccountCallback() {
            @Override
            public void onSuccess(String msg) {
                Toast.makeText(StudentBookedSession.this, msg, Toast.LENGTH_LONG).show();
                fetchStudentSessions();
            }

            @Override
            public void onFailure(String msg) {
                Toast.makeText(StudentBookedSession.this,
                        "Cancellation Failed: " + msg,
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    // ---------- ICS helpers ----------

    private String buildIcsForUpcomingSlots() {
        if (upcomingSlots.isEmpty()) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("BEGIN:VCALENDAR\r\n");
        sb.append("VERSION:2.0\r\n");
        sb.append("PRODID:-//Group33//TutoringApp//EN\r\n");
        sb.append("CALSCALE:GREGORIAN\r\n");
        sb.append("METHOD:PUBLISH\r\n");

        DateTimeFormatter icsFormatter =
                DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");
        ZonedDateTime nowUtc = ZonedDateTime.now(ZoneOffset.UTC);

        for (TimeSlot slot : upcomingSlots) {
            ZonedDateTime startUtc = slot.getStartDate().withZoneSameInstant(ZoneOffset.UTC);
            ZonedDateTime endUtc = slot.getEndDate().withZoneSameInstant(ZoneOffset.UTC);

            String uid = (slot.getID() != null ? slot.getID()
                    : String.valueOf(startUtc.toEpochSecond())) + "@group33-tutoring";

            String title = "Tutoring Session";
            if (slot.getTutor() != null) {
                title = "Tutoring with " +
                        slot.getTutor().getFirstName() + " " +
                        slot.getTutor().getLastName();
            }

            sb.append("BEGIN:VEVENT\r\n");
            sb.append("UID:").append(uid).append("\r\n");
            sb.append("DTSTAMP:").append(nowUtc.format(icsFormatter)).append("\r\n");
            sb.append("DTSTART:").append(startUtc.format(icsFormatter)).append("\r\n");
            sb.append("DTEND:").append(endUtc.format(icsFormatter)).append("\r\n");
            sb.append("SUMMARY:").append(escapeIcsText(title)).append("\r\n");
            sb.append("STATUS:CONFIRMED\r\n");
            sb.append("END:VEVENT\r\n");
        }

        sb.append("END:VCALENDAR\r\n");
        return sb.toString();
    }

    private String escapeIcsText(String text) {
        if (text == null) return "";
        return text
                .replace("\\", "\\\\")
                .replace(",", "\\,")
                .replace(";", "\\;")
                .replace("\n", "\\n");
    }

    // ---------- Export to local .ics via share sheet (existing feature) ----------

    private void exportSessionsToCalendar() {
        String ics = buildIcsForUpcomingSlots();
        if (ics == null) {
            Toast.makeText(this, "No upcoming sessions to export.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            File file = new File(getCacheDir(), "tutoring_sessions.ics");
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(ics.getBytes(StandardCharsets.UTF_8));
            }

            Uri uri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",
                    file
            );

            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            sendIntent.setType("text/calendar");
            sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
            sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(sendIntent, "Export sessions to calendar"));

        } catch (IOException e) {
            Log.e(TAG, "Failed to export sessions", e);
            Toast.makeText(this, "Failed to export sessions.", Toast.LENGTH_LONG).show();
        }
    }

    // ---------- Email .ics using Firestore + Trigger Email ----------

    private void emailCalendarFile() {
        String ics = buildIcsForUpcomingSlots();
        if (ics == null) {
            Toast.makeText(this, "No upcoming sessions to email.", Toast.LENGTH_SHORT).show();
            return;
        }

        AccountHandling accHandle = new AccountHandling();
        String subject = "Your tutoring sessions calendar file";
        String body = "Attached is an .ics file with your upcoming tutoring sessions.";

        accHandle.sendCalendarEmail(currentStudent.getEmail(), subject, body, ics)
                .addOnSuccessListener(docRef -> {
                    // Firestore write success (email queued for sending)
                    Toast.makeText(
                            StudentBookedSession.this,
                            "Calendar email queued successfully.",
                            Toast.LENGTH_LONG
                    ).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(
                            StudentBookedSession.this,
                            "Failed to queue calendar email: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }
}
