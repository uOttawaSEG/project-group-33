package com.example.group_33_project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;


import java.util.ArrayList;
import java.util.List;

public class TutorPendingRequest extends AppCompatActivity{
    private List<TimeSlot> pendingRequests; //storing all the pending timeslots
    private TutorHandling tutorHandling;
    private Tutor currentTutor;

    private RecyclerView rvSlots;

    Button btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen12_prequesttut);
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.tutorprequest), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        currentTutor = (Tutor) getIntent().getSerializableExtra("tutor"); //get tutor as an object
        if (currentTutor == null) {
            Toast.makeText(this, "Error: No tutor logged in", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        //rvSlots = findViewById(R.id.rv12);

        //FINDING THE ID OF BUTTON TO GO BACK
        btnBack = findViewById(R.id.screen12_back);

         btnBack.setOnClickListener(v -> {
               Intent intent = new Intent(TutorPendingRequest.this, TutorUpcoming.class);
               intent.putExtra("tutor", currentTutor);
               startActivity(intent);
               finish();
         });

         pendingRequests = new ArrayList<>();
        tutorHandling = new TutorHandling();

        loadPendingRequests();


    }

        //GET ALL THE PENDING REQUESTS FROM THE DATABASE
        private void loadPendingRequests() {
            tutorHandling.getAllSlotsByStatus("pending", new SlotListCallback() {
                @Override
                public void onSuccess(List<TimeSlot> slots) { // ALL SLOTS PROVIDED IN PENDINGREQUEST ARRAY
                    pendingRequests.clear();
                    for (TimeSlot slot : slots) {
                        if (slot.getTutor() != null &&
                                slot.getTutor().getEmail().equalsIgnoreCase(currentTutor.getEmail())) {
                            pendingRequests.add(slot);
                        }
                    }

                    // Just show how many pending requests were found
                    Toast.makeText(TutorPendingRequest.this,
                            "Found " + pendingRequests.size() + " pending requests.",
                            Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(String error) {
                    Toast.makeText(TutorPendingRequest.this,
                            "Error loading pending requests: " + error,
                            Toast.LENGTH_SHORT).show();
                }
            });
        }


}
