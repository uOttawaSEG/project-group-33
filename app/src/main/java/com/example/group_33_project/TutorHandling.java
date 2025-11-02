package com.example.group_33_project;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/// tutor will have additional information added to database: availabilities and sessions

/// availabilities: slotID {date, start, end, approve}
///  if approve=true then student booked this time and this slot will be sent to booked.
///  tutors can make multiple slots

///sessions: sessionID {studentEmail, date, start, end, status}
///  if status= pending/approved/denied/cancelled
///  if tutor cancels session it get deleted. If tutor denies student then session goes back to availabilities so that another student can book it


/// addOnSuccessListener is to do smt with data if success. addOnFailureListener handle firebase error
/// override callback so that it just prints message or when data is ready
public class TutorHandling {
    private FirebaseFirestore db;

    public TutorHandling() {
        db = FirebaseFirestore.getInstance();
    }

    public void createAvailability(String tutorEmail, String date, String start, String end, TutorCallBack callback){

        //cannot create slot in the past
        //slot must be 30 mins increment
        //slot cannot overlap with another slot

        try {
            if (!isValidSlot(start, end)) {
                callback.onFailure("Invalid slot");
                return;
            }
            if (!isFutureDate(date, start, end)) {
                callback.onFailure("Invalid date");
                return;
            }

            //checking for overlaps
            db.collection("accounts")
                    .document(tutorEmail)
                    .collection("availabilities")
                    .whereEqualTo("date", date)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (!querySnapshot.isEmpty()) {
                            for (QueryDocumentSnapshot doc : querySnapshot) { //comparing with every existing slots to find any overlaps
                                String start1 = doc.getString("start");
                                String end1 = doc.getString("end");

                                if (isOverlap(start, end, start1, end1)) {
                                    callback.onFailure("Slot overlaps with another slot");
                                    return;
                                }
                            }

                        }

                        //VALID SLOT SO INSERT SLOT TO DATABASE
                        //create slot object to add to database
                        Map<String, Object> slot = new HashMap<>();
                        String slotID = date +"_"+ start +"_"+ end;
                        slot.put("date", date);
                        slot.put("start", start);
                        slot.put("end", end);
                        slot.put("approve", false); //false means this slot is not booked

                        db.collection("accounts")
                                .document(tutorEmail)
                                .collection("availabilities")
                                .document(slotID)
                                .set(slot)
                                .addOnSuccessListener(aVoid -> {
                                    callback.onSuccess("Slot created successfully with ID: " + slotID);
                                })
                                .addOnFailureListener(e -> callback.onFailure("Error: " + e.getMessage()));

                    })

                    .addOnFailureListener(e -> callback.onFailure("Error: " + e.getMessage()));
        }
        catch (Exception e){
            callback.onFailure("Error: " + e.getMessage());
        }


    }
    public void deleteAvailability(String tutorEmail, String slotId, TutorCallBack callback){
        db.collection("accounts")
                .document(tutorEmail)
                .collection("availabilities")
                .document(slotId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    callback.onSuccess("Slot deleted successfully");
                })
                .addOnFailureListener(e -> callback.onFailure("Error: " + e.getMessage()));

    }

    //gets all slots that are not booked yet by student. TO BE DISPLAYED IN UI FOR STUDENT TO SELECT
    //all available slots will be put in a list
    public void getAvailabilities(String tutorEmail, TutorSessionCallBack callback){

        db.collection("accounts")
                .document(tutorEmail)
                .collection("availabilities")
                .whereEqualTo("approve", false)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        List<Map<String, Object>> slots = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : querySnapshot) {
                            slots.add(doc.getData());

                        }
                        callback.onSuccess(slots);
                    }
                })
                .addOnFailureListener(e -> callback.onFailure("Error: " + e.getMessage()));

    }


    private boolean isValidSlot(String start, String end){
        //MUST BE 30 minutes INCREMENT
        if (Integer.parseInt(end) - Integer.parseInt(start) != 30){
            return false;
        }
        else{
            return true;
        }
    }

    private boolean isFutureDate(String date, String start, String end){
        //to be implemented. for tutor to make a new slot, it must be in the future.
        return true;
    }
    private boolean isOverlap(String start1, String end1, String start2, String end2){
        return !(end1.compareTo(start2) <= 0 || start1.compareTo(end2) >= 0);
    }

    public void deleteOldSlots(String tutorEmail, TutorCallBack callback){
        //to be implemented. old slots that are outdated should be deleted automatically.


    }







}
