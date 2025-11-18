package com.example.group_33_project;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.time.*;

public class TutorHandling {

    private final FirebaseFirestore db;

    public TutorHandling() {
        db = FirebaseFirestore.getInstance();
    }

    // method to check whether a tutor already has a timeslot that overlaps with a requested new timeslot
    public void checkTutorOverlap(TimeSlot newTS, AccountCallback callback) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String tutorEmail = newTS.getTutor().getEmail();

        db.collection("accounts") // query the tutor's timeslots only
                .whereEqualTo("email", tutorEmail)
                .limit(1)
                .get()
                .addOnSuccessListener(query -> {
                    if (query.isEmpty()) {
                        callback.onFailure("Tutor not found");
                        return;
                    }

                    DocumentSnapshot tutorDoc = query.getDocuments().get(0); // limit 1, so the desired doc is index 0 (the tutor we queried)
                    CollectionReference slotsRef = tutorDoc.getReference().collection("timeSlots"); // get the timeslots from the tutor

                    slotsRef.get()
                            .addOnSuccessListener(qs -> {
                                boolean overlapFound = false;

                                for (DocumentSnapshot doc : qs.getDocuments()) {
                                    Timestamp sTs = doc.getTimestamp("startInstant"); // since we store in UTC for firebase, we must convert back to ZonedDateTime
                                    Timestamp eTs = doc.getTimestamp("endInstant");
                                    String zoneId = doc.getString("zoneId");

                                    if (sTs == null || eTs == null || zoneId == null) continue; // skip the document

                                    ZonedDateTime s = ZonedDateTime.ofInstant(sTs.toDate().toInstant(), ZoneId.of(zoneId)); // use methods to convert back to a ZonedDateTime object
                                    ZonedDateTime e = ZonedDateTime.ofInstant(eTs.toDate().toInstant(), ZoneId.of(zoneId));

                                    TimeSlot existing = new TimeSlot(null, null, s, e, null, null, null); // a dummy time slot with just the times

                                    if (TimeSlot.isOverlap(newTS, existing)) { // use the created method to see if its overlapping
                                        overlapFound = true;
                                        break;
                                    }
                                }

                                if (overlapFound)
                                    callback.onFailure("Overlap detected");
                                else
                                    callback.onSuccess("No overlaps detected");
                            })
                            .addOnFailureListener(e -> callback.onFailure("Error: " + e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onFailure("Error: " + e.getMessage()));
    }

    // method to be called upon attempting to create NEW availability
    public void createNewAvailability(Tutor tutor, ZonedDateTime start, ZonedDateTime end, boolean requiresApproval, AccountCallback callback) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // validate our parameters
        if (tutor.getEmail() == null || tutor.getEmail().isEmpty()) {
            callback.onFailure("tutorEmail required");
            return;
        }
        if (start == null || end == null || !end.isAfter(start)) {
            callback.onFailure("End date must be after start date");
            return;
        }
        if (!isFutureDate(start)){ // to check if the start date is in the future
            callback.onFailure("The start date must be in the future");
            return;
        }

        // valid parameters, create a timeslot and see if the tutor has any overlapping slots
        TimeSlot newSlot = new TimeSlot(tutor, requiresApproval, start, end);

        checkTutorOverlap(newSlot, new AccountCallback() {
            @Override
            public void onSuccess(String msg) { // onSuccess is called IF THERE ARE NO OVERLAPS! good!
                // proceed to store the timeslot in the db
                db.collection("accounts")
                        .whereEqualTo("email", newSlot.getTutor().getEmail())
                        .limit(1)
                        .get()
                        .addOnFailureListener(e -> callback.onFailure("Issue while querying tutor account in database."))
                        .addOnSuccessListener(tutorQuery -> {
                            if (tutorQuery.isEmpty()) { // if there's no tutor found, error
                                callback.onFailure("Tutor not found for email: " + newSlot.getTutor().getEmail());
                                return;
                            }

                            DocumentSnapshot tutorDoc = tutorQuery.getDocuments().get(0); // get the tutor's document
                            CollectionReference slotsRef = tutorDoc.getReference().collection("timeSlots"); // get a reference to where the timeslots are stored

                           // firestore prefers UTC times, so we'll convert here:
                            Timestamp startTs = new Timestamp(Date.from(start.toInstant())); // using java Date and firebase timestamp
                            Timestamp endTs = new Timestamp(Date.from(end.toInstant()));
                            String zoneId = start.getZone().getId(); // save the zone so that we can remake the ZonedDateTime object later when accessing

                            Map<String, Object> data = new HashMap<>(); // store everything in a hashmap for firebase
                            data.put("tutorEmail", newSlot.getTutor().getEmail());
                            data.put("startInstant", startTs);
                            data.put("endInstant", endTs);
                            data.put("zoneId", zoneId);
                            data.put("isBooked", false); // false by default (upon creation of new slot)
                            data.put("requireApproval", requiresApproval);
                            data.put("studentEmail", null); // not yet booked
                            data.put("status", "open");

                            slotsRef.add(data)

                                    .addOnSuccessListener(ref ->{
                                            newSlot.setID(ref.getId()); // SAVE the firestore id for the slot!
                                            callback.onSuccess("Created new timeslot successfully.");}) // success :)
                                    .addOnFailureListener(e ->
                                            callback.onFailure("Write failed: " + e.getMessage()));


                        });
            }

            @Override
            public void onFailure(String msg) {
                callback.onFailure(msg); // relay the failure message
            }
        });

    }

    // method for a tutor to delete an availability they created
    public void deleteAvailability(TimeSlot slot, AccountCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Validate all the fields first
        if (slot == null) {
            callback.onFailure("TimeSlot required");
            return;
        }
        if (slot.getID() == null || slot.getID().isEmpty()) {
            callback.onFailure("Slot ID missing in TimeSlot object");
            return;
        }

        if (!slot.getStatus().equals("open")){
            callback.onFailure("This timeslot has already been requested by a student.");
            return; // STOP if we try to delete a booked/pending session
        }

        // query tutor document by email
        db.collection("accounts")
                .whereEqualTo("email", slot.getTutor().getEmail())
                .limit(1)
                .get()
                .addOnFailureListener(e -> callback.onFailure("Failed to find tutor: " + e.getMessage()))
                .addOnSuccessListener(query -> {
                    if (query.isEmpty()) {
                        callback.onFailure("Tutor not found in database");
                        return;
                    }

                    DocumentSnapshot tutorDoc = query.getDocuments().get(0); // get the tutor's doc
                    DocumentReference slotRef = tutorDoc.getReference() // get the REF to the slot we want to delete
                            .collection("timeSlots")
                            .document(slot.getID());

                    // delete the slot :)
                    slotRef.delete()
                            .addOnSuccessListener(aVoid -> callback.onSuccess("Deleted time slot successfully"))
                            .addOnFailureListener(e -> callback.onFailure("Error deleting slot: " + e.getMessage()));
                });
    }


    public static void cancelTimeSlot(TimeSlot slot, AccountCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (slot.getStatus().equals("cancelled")){
            callback.onFailure("The timeslot has already been cancelled.");
            return; // STOP if we try to cancel a previously cancelled timeslot
        }
        // find tutor's doc by email
        db.collection("accounts")
                .whereEqualTo("email", slot.getTutor().getEmail())
                .limit(1)
                .get()
                .addOnFailureListener(e -> callback.onFailure("Lookup failed: " + e.getMessage()))
                .addOnSuccessListener(q -> {
                    if (q.isEmpty()) {
                        callback.onFailure("Tutor not found");
                        return;
                    }

                    DocumentSnapshot tutorDoc = q.getDocuments().get(0); // get the tutor's doc
                    DocumentReference slotRef = tutorDoc.getReference()
                            .collection("timeSlots")
                            .document(slot.getID()); // find the slot using the ID


                    slotRef.get()
                            .addOnFailureListener(e -> callback.onFailure("Failed to load slot: " + e.getMessage()))
                            .addOnSuccessListener(foundSlot -> {
                                if (!foundSlot.exists()) {
                                    callback.onFailure("Time slot not found");
                                    return;
                                }
                                // store updates in a new hashmap
                                Map<String, Object> updates = new HashMap<>();
                                updates.put("status", "cancelled"); // Only thing we will change is the status (we can keep the student, so we can notify them of a cancellation, for ex)

                                slotRef.update(updates) // update change
                                        .addOnSuccessListener(v -> callback.onSuccess("Slot cancelled"))
                                        .addOnFailureListener(e -> callback.onFailure("Cancel failed: " + e.getMessage()));
                            });
                });
    }



    // method to approve or deny a pending request
    public void approveDenyPendingRequest(TimeSlot slot, String choice, AccountCallback callback) { // choice = "approve" or "deny"
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // find tutor's doc by email
        db.collection("accounts")
                .whereEqualTo("email", slot.getTutor().getEmail())
                .limit(1)
                .get()
                .addOnFailureListener(e -> callback.onFailure("Lookup failed: " + e.getMessage()))
                .addOnSuccessListener(q -> {
                    if (q.isEmpty()) {
                        callback.onFailure("Tutor not found");
                        return;
                    }

                    DocumentSnapshot tutorDoc = q.getDocuments().get(0); // get the tutor's doc
                    DocumentReference slotRef = tutorDoc.getReference()
                            .collection("timeSlots")
                            .document(slot.getID()); // find the slot using the ID


                    slotRef.get()
                            .addOnFailureListener(e -> callback.onFailure("Failed to load slot: " + e.getMessage()))
                            .addOnSuccessListener(foundSlot -> {

                                if (!foundSlot.exists()) {
                                    callback.onFailure("Time slot not found");
                                    return;
                                }
                                // an extra check to make sure the status wasn't changed while querying! (
                                String currentStatus = foundSlot.getString("status");
                                if (!"pending".equals(currentStatus)) {
                                    callback.onFailure("Slot is no longer pending.");
                                    return;
                                }

                                // store updates in a new hashmap
                                Map<String, Object> updates = new HashMap<>();
                                if (choice.equals("approve")){
                                    updates.put("status", "booked");
                                }
                                else { // choice = deny
                                    String studentEmail = foundSlot.getString("studentEmail");
                                    if (studentEmail != null) {
                                        updateDeniedSession(studentEmail, foundSlot.getId()); // call helper method
                                    }

                                    updates.put("status", "open");
                                    updates.put("studentEmail", null);
                                }

                                slotRef.update(updates) // update change
                                        .addOnSuccessListener(v -> callback.onSuccess("Status updated successfully."))
                                        .addOnFailureListener(e -> callback.onFailure("Error: " + e.getMessage()));
                            });
                });
    }

    // method to move a rejected/denied session into the student's rejected session token list
    public void updateDeniedSession(String studentEmail, String docRef){
        if (studentEmail == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        AccountHandling.queryAccount(studentEmail, new QueryCallback() {
            @Override
            public void onSuccess(Account account) {
                Student student = (Student) account;


                student.addRejectedSessionToken(docRef);
                student.removeSessionToken(docRef);

               // update on FIRESTORE:
                DocumentReference studentRef = db.collection("accounts").document(student.getEmail()); // find ref to the student

                Map<String, Object> updates = new HashMap<>();
                updates.put("rejectedSessionTokens", FieldValue.arrayUnion(docRef)); // add the ID to the REJECTED list
                updates.put("sessionTokens", FieldValue.arrayRemove(docRef)); // remove the ID from the REGULAR list

                studentRef.update(updates); // update
            }

            @Override
            public void onFailure(String errorMessage) {

            }
        });
    }

    // Method to get a list of ALL timeslots from ALL tutors, with the status specified (open, booked, pending, cancelled, null -> ALL slots with ALL status)
    public static void getAllSlotsByStatus(String status, SlotListCallback callback) { // for ALL slots, call getAllSlotsByStatus(null, callback);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Query query = db.collectionGroup("timeSlots"); // firebase helper class to query by a collection group

        // if we want to get the slots by a specific status, set the query filter!!
        if (status != null && !status.isBlank()) {
            String[] statuses = status.split(" "); // SPLIT so we can use MULTIPLE query filters!
            query = query.whereIn("status", Arrays.asList(statuses));
        }

         // otherwise, we will just get ALL of the slots, regardless of status (query is just filtering through timeslots)

        query.orderBy("startInstant") // using orderBy(startInstant) gives us the earliest bookings FIRST in the list :)
                .get()
                .addOnFailureListener(e -> callback.onFailure("Error fetching slots: " + e.getMessage()))
                .addOnSuccessListener(qs -> {
                            List<TimeSlot> slotList = new ArrayList<>(); // initialize a new array list to hold the timeslots

                            List<DocumentSnapshot> docs = qs.getDocuments();
                            if (docs.isEmpty()) { callback.onSuccess(slotList); return; }

                            final int[] remaining = { docs.size() }; // countdown to know when we are done with all the slots
                            // we must do a final int[] since this is an async task!! otherwise it will get lost. so we can do a final int array, and just modify the contents which will get saved in memory

                            for (DocumentSnapshot doc : docs) { // loop through all the docs
                                // reconstruct the timeslots
                                Timestamp sTs = doc.getTimestamp("startInstant");
                                Timestamp eTs = doc.getTimestamp("endInstant");
                                String zoneId = doc.getString("zoneId");
                                Boolean requireApproval = doc.getBoolean("requireApproval");
                                String docStatus = doc.getString("status");
                                String tutorEmail = doc.getString("tutorEmail");
                                String studentEmail = doc.getString("studentEmail");


                                assert sTs != null;
                                assert eTs != null;
                                ZonedDateTime start = ZonedDateTime.ofInstant(sTs.toDate().toInstant(), ZoneId.of(zoneId)); // convert the UTC times back to ZonedDateTime!
                                ZonedDateTime end = ZonedDateTime.ofInstant(eTs.toDate().toInstant(), ZoneId.of(zoneId));

                                // we are given the tutor & student emails, but we want to have the entire tutor and student objects in the timeslots!
                                // Fetch account objects with helper method getAccounts()
                                getAccounts(tutorEmail, studentEmail, new AccountListCallback() {
                                    @Override
                                    public void onSuccess(List<Object> accounts) {
                                        Tutor t = (Tutor) accounts.get(0); // first index is the tutor account
                                        Student s = null;
                                        if (accounts.get(1) != null){ // make sure we handle the case of a null student (not booked yet)
                                            s = (Student) accounts.get(1);
                                        }

                                        slotList.add(new TimeSlot(t, requireApproval, start, end, s, docStatus, doc.getId())); // add the completed timeslot to the slot list :)


                                        if (--remaining[0] == 0) {
                                            // if theres no docs remaining to process, callback success :)
                                            slotList.sort(Comparator.comparing(TimeSlot::getStartDate)); // sort it by the start date so that it's ordered nicely for UI implementation
                                            callback.onSuccess(slotList);
                                        }
                                    }

                                    @Override
                                    public void onFailure(String error) {
                                        // skip this slot but still finish when all docs processed
                                        if (--remaining[0] == 0) {
                                            slotList.sort(Comparator.comparing(TimeSlot::getStartDate)); // sort it by the start date so that it's ordered nicely for UI implementation
                                            callback.onSuccess(slotList);
                                        }
                                    }
                                });
                            }
                        });
    }
    // helper method for getAllSlotsByStatus() for retrieving the tutor and student's accounts by emails
    public static void getAccounts(String tutorEmail, String studentEmail, AccountListCallback callback) {
        Account[] accounts = new Account[2]; // initialize an array to store the student and tutor's accounts

        // Fetch the tutor first
        AccountHandling.queryAccount(tutorEmail, new QueryCallback() {
            @Override
            public void onSuccess(Account account) {
                if (account instanceof Tutor) {
                    accounts[0] = account; // set index 0 to the tutor
                }
                // Then fetch the student (if applicable!! it might be null for open or cancelled slots)
                if (studentEmail != null) {
                    AccountHandling.queryAccount(studentEmail, new QueryCallback() {
                        @Override
                        public void onSuccess(Account account) {
                            if (account instanceof Student) {
                                accounts[1] = account;
                            }
                            callback.onSuccess(Arrays.asList(accounts[0], accounts[1])); // can't actually just pass the array, must turn it into a list
                        }

                        @Override
                        public void onFailure(String err) {
                            callback.onSuccess(Arrays.asList(accounts[0], accounts[1])); // return tutor even if student failed
                        }
                    });
                } else {
                    // no student attached
                    accounts[1] = null; // so set the student as null
                    callback.onSuccess(Arrays.asList(accounts[0], accounts[1]));
                }
            }

            @Override
            public void onFailure(String err) {
                callback.onFailure("Tutor lookup failed: " + err);
            }
        });
    }

    // Fast: fetch ONLY this tutor’s slots from /accounts/{tutorDoc}/timeSlots -> to resolve bug with querying taking too long
    public void queryTutorSlots(Tutor tutor, SlotListCallback callback) {

        db.collection("accounts")
                .whereEqualTo("email", tutor.getEmail())
                .limit(1)
                .get()
                .addOnFailureListener(e -> callback.onFailure("Lookup failed: " + e.getMessage()))
                .addOnSuccessListener(q -> {
                    if (q.isEmpty()) {
                        callback.onSuccess(new ArrayList<>());
                        return;
                    }

                    DocumentSnapshot tutorDoc = q.getDocuments().get(0);
                    tutorDoc.getReference()
                            .collection("timeSlots")
                            .orderBy("startInstant") // order by earliest first!
                            .get()
                            .addOnFailureListener(e -> callback.onFailure("Slots load failed: " + e.getMessage()))
                            .addOnSuccessListener(qs -> {
                                List<DocumentSnapshot> docs = qs.getDocuments();
                                if (docs.isEmpty()) {
                                    callback.onSuccess(new ArrayList<>());
                                    return;
                                }

                                List<TimeSlot> slots = new ArrayList<>();
                                final int[] remaining = { docs.size() };
                                // reused
                                for (DocumentSnapshot doc : docs) {
                                    Timestamp sTs = doc.getTimestamp("startInstant");
                                    Timestamp eTs = doc.getTimestamp("endInstant");
                                    String zoneId = doc.getString("zoneId");
                                    Boolean requireApproval = doc.getBoolean("requireApproval");
                                    String status = doc.getString("status");
                                    String studentEmail = doc.getString("studentEmail");
                                    String slotId = doc.getId();

                                    // handle bad data safely
                                    if (sTs == null || eTs == null || zoneId == null) {
                                        if (--remaining[0] == 0) {
                                            slots.sort(Comparator.comparing(TimeSlot::getStartDate));
                                            callback.onSuccess(slots);
                                        }
                                        continue;
                                    }

                                    ZonedDateTime start = ZonedDateTime.ofInstant(sTs.toDate().toInstant(), ZoneId.of(zoneId));
                                    ZonedDateTime end = ZonedDateTime.ofInstant(eTs.toDate().toInstant(), ZoneId.of(zoneId));

                                    // if the student email is null, the slot isnt booked so we don't need to find a student!
                                    if (studentEmail == null) {
                                        slots.add(new TimeSlot(tutor, requireApproval, start, end, null, status, slotId));

                                        if (--remaining[0] == 0) {
                                            slots.sort(Comparator.comparing(TimeSlot::getStartDate));
                                            callback.onSuccess(slots);
                                        }
                                    } else {
                                        // need to fetch the student asynchronously, since the timeslot is linked to a student object
                                        AccountHandling.queryAccount(studentEmail, new QueryCallback() { // query the account
                                            @Override
                                            public void onSuccess(Account account) {
                                                Student student = null;
                                                if (account instanceof Student) {
                                                    student = (Student) account;
                                                }

                                                slots.add(new TimeSlot(tutor, requireApproval, start, end, student, status, slotId)); // then add it

                                                if (--remaining[0] == 0) {
                                                    slots.sort(Comparator.comparing(TimeSlot::getStartDate));
                                                    callback.onSuccess(slots);
                                                }
                                            }

                                            @Override
                                            public void onFailure(String errorMessage) {
                                                // if student lookup fails, still add slot with null student
                                                slots.add(new TimeSlot(tutor, requireApproval, start, end, null, status, slotId));

                                                if (--remaining[0] == 0) {
                                                    slots.sort(Comparator.comparing(TimeSlot::getStartDate));
                                                    callback.onSuccess(slots);
                                                }
                                            }
                                        });
                                    }
                                }
                            });
                });
    } // wow that's a lot of });'s


    // helper method to check if a given date is in the future
    public boolean isFutureDate(ZonedDateTime start){
        ZonedDateTime today = ZonedDateTime.now(ZoneId.of("America/New_York")); // using java time module to get the current date IN EST!! (if we use local client, we might be in the wrong timezone)

        return start.isAfter(today); // it's a future date if the start date is AFTER today's date (also compares hour/min/sec)

    }

}
