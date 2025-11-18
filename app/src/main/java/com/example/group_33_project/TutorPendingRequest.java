package com.example.group_33_project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import java.util.ArrayList;
import java.util.List;

public class TutorPendingRequest extends AppCompatActivity{
    private List<TimeSlot> pendingRequests; // list of pending time slots
    private TutorHandling tutorHandling;
    private Tutor currentTutor;

    private RecyclerView rvSlots;
    private TutorPendingRequestAdapter adapter;
    private Button btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //INITIAL CREATING
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen12_prequesttut);
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.tutorprequest), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //GET TUTOR
        currentTutor = (Tutor) getIntent().getSerializableExtra("tutor");
        if (currentTutor == null) {
            Toast.makeText(this, "Error: No tutor logged in", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        //BIND UI
        rvSlots = findViewById(R.id.rv12);
        btnBack = findViewById(R.id.screen12_back);

        //GET THE INSTANCE VARIABLES
        tutorHandling = new TutorHandling();
        pendingRequests = new ArrayList<>();

        //SETUP LIST
        setupRecyclerView();

        //GET THE PENDING REQUESTS FROM DATABASE
        loadPendingRequests();

        //BACK BUTTON
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(TutorPendingRequest.this, TutorUpcoming.class);
            intent.putExtra("tutor", currentTutor);
            startActivity(intent);
            finish();
        });
    }

    //SETUP RECYCLERVIEW
    private void setupRecyclerView() {
        adapter = new TutorPendingRequestAdapter(pendingRequests, new TutorPendingRequestAdapter.ActionListener() {
            @Override
            public void onApprove(TimeSlot slot) {
                handleApprove(slot);
            }

            @Override
            public void onDeny(TimeSlot slot) {
                handleDeny(slot);
            }
        });

        rvSlots.setLayoutManager(new LinearLayoutManager(this));
        rvSlots.setAdapter(adapter);
    }

    //GET PENDING REQUESTS FROM THE DATABASE
    private void loadPendingRequests() {
        tutorHandling.getAllSlotsByStatus("pending", new SlotListCallback() {
            @Override
            public void onSuccess(List<TimeSlot> slots) {
                pendingRequests.clear();

                for (TimeSlot slot : slots) {
                    if (slot.getTutor() != null &&
                            slot.getTutor().getEmail().equalsIgnoreCase(currentTutor.getEmail())) {
                        pendingRequests.add(slot);
                    }
                }

                runOnUiThread(() -> {
                    adapter.notifyDataSetChanged();

                    Toast.makeText(TutorPendingRequest.this,
                            "Found " + pendingRequests.size() + " pending requests.",
                            Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(TutorPendingRequest.this,
                        "Error loading pending requests: " + error,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }


    //APPROVE / DENY REQUESTS
    private void handleApprove(TimeSlot slot) {

        tutorHandling.approveDenyPendingRequest(slot, "approve", new AccountCallback() {
            @Override
            public void onSuccess(String msg) {

            }

            @Override
            public void onFailure(String msg) {

            }
        });

        pendingRequests.remove(slot);

        runOnUiThread(() -> {adapter.notifyDataSetChanged();});
    }

    private void handleDeny(TimeSlot slot) {

        tutorHandling.approveDenyPendingRequest(slot, "deny", new AccountCallback() {
            @Override
            public void onSuccess(String msg) {

            }

            @Override
            public void onFailure(String msg) {

            }
        });

        pendingRequests.remove(slot);

        runOnUiThread(() -> {adapter.notifyDataSetChanged();});
    }
}
