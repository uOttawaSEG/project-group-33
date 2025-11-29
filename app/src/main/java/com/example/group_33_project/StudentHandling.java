package com.example.group_33_project;

import static com.example.group_33_project.TutorHandling.getAccounts;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// this class is to handle the app interactions with a student, like booking, viewing upcoming sessions, searching, etc..
public class StudentHandling {
    private final FirebaseFirestore db;

    public StudentHandling() {
        db = FirebaseFirestore.getInstance();
    }

    // method to obtain a list of open slots QUERIED BY COURSE
    public static void searchSlotsByCourse(String course, SlotListCallback callback){
        // format course:
        course = course.trim().toUpperCase(); // remove whitespace and make lowercase (like how tutors have it stored)
        // FIRST, get a list of all the tutors which offer tutoring for the desired course:
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("accounts")
                .whereEqualTo("type", "Tutor")
                .whereArrayContains("courses", course)
                .get()
                .addOnFailureListener(e ->
                        callback.onFailure("Error fetching tutors: " + e.getMessage()))
                .addOnSuccessListener(tutorQs -> {
                    List<String> tutorEmails = new ArrayList<>();

                    for (DocumentSnapshot tutorDoc : tutorQs.getDocuments()) { // collect the emails of all the tutors which have that specific course offering
                        String email = tutorDoc.getString("email");
                        if (email != null && !email.isBlank()) {
                            tutorEmails.add(email);
                        }
                    }

                    // No tutors found means no tutors that teach that course, so return an empty list
                    if (tutorEmails.isEmpty()) {
                        callback.onSuccess(new ArrayList<>());
                        return;
                    }

                    // because of firebase constraints, we must query at most 10 at a time, so we need to use tasks to split into sub-tasks of 10 max
                    // first get smaller chunks of the tutor email list
                    List<List<String>> chunks = new ArrayList<>();
                    for (int i = 0; i < tutorEmails.size(); i += 10) {
                        int end = Math.min(i + 10, tutorEmails.size());
                        chunks.add(tutorEmails.subList(i, end));
                    }

                   // now make smaller query snapshot tasks that use the above chunks of 10 emails max
                    List<Task<QuerySnapshot>> slotTasks = new ArrayList<>();
                    for (List<String> chunk : chunks) {
                        Query slotQuery = db.collectionGroup("timeSlots")
                                .whereEqualTo("status", "open") // must be open slots
                                .whereIn("tutorEmail", chunk) // must be a tutor that offers the course
                                .whereGreaterThan("startInstant", Timestamp.now()) // must be in the FUTURE
                                .orderBy("startInstant"); // sort by most recent first

                        slotTasks.add(slotQuery.get());
                    }

                    // when EVERY task is done, we can merge everything to one big list for the search result
                    Tasks.whenAllSuccess(slotTasks)
                            .addOnFailureListener(e ->
                                    callback.onFailure("Error fetching slots: " + e.getMessage()))
                            .addOnSuccessListener(results -> {
                                List<DocumentSnapshot> allDocs = new ArrayList<>();

                                // results is a List<Object> where each Object is a QuerySnapshot since we broke everything into subtasks
                                for (Object r : results) {
                                    QuerySnapshot qs = (QuerySnapshot) r;
                                    allDocs.addAll(qs.getDocuments());
                                }

                                List<TimeSlot> slotList = new ArrayList<>();

                                if (allDocs.isEmpty()) {
                                    callback.onSuccess(slotList); // no slots available -> empty list
                                    return;
                                }

                                // reused from TutorHandling
                                final int[] remaining = { allDocs.size() };

                                for (DocumentSnapshot doc : allDocs) {
                                    Timestamp sTs = doc.getTimestamp("startInstant");
                                    Timestamp eTs = doc.getTimestamp("endInstant");
                                    String zoneId = doc.getString("zoneId");
                                    Boolean requireApproval = doc.getBoolean("requireApproval");
                                    String docStatus = doc.getString("status");
                                    String tutorEmail = doc.getString("tutorEmail");
                                    String studentEmail = doc.getString("studentEmail");

                                    if (sTs == null || eTs == null || zoneId == null) {
                                        if (--remaining[0] == 0) {
                                            slotList.sort(Comparator.comparing(TimeSlot::getStartDate));
                                            callback.onSuccess(slotList);
                                        }
                                        continue;
                                    }

                                    ZonedDateTime start = ZonedDateTime.ofInstant(
                                            sTs.toDate().toInstant(), ZoneId.of(zoneId));
                                    ZonedDateTime end = ZonedDateTime.ofInstant(
                                            eTs.toDate().toInstant(), ZoneId.of(zoneId));

                                    getAccounts(tutorEmail, studentEmail, new AccountListCallback() { //reused
                                        @Override
                                        public void onSuccess(List<Object> accounts) {
                                            Tutor t = (Tutor) accounts.get(0);
                                            Student s = null;
                                            if (accounts.get(1) != null) {
                                                s = (Student) accounts.get(1);
                                            }

                                            slotList.add(new TimeSlot(t, requireApproval, start, end, s, docStatus, doc.getId()));

                                            if (--remaining[0] == 0) {
                                                slotList.sort(Comparator.comparing(TimeSlot::getStartDate)); // sort for UI
                                                callback.onSuccess(slotList); // return the list of slots that have been filtered!! these should ALL be slots created by tutors who offer the requested course.
                                            }
                                        }

                                        @Override
                                        public void onFailure(String error) {
                                            // Skip this slot but still finish when all docs processed
                                            if (--remaining[0] == 0) {
                                                slotList.sort(Comparator.comparing(TimeSlot::getStartDate));
                                                callback.onSuccess(slotList);
                                            }
                                        }
                                    });
                                }
                            });
                });
    }





    // method to obtain a list of a specific student's sessions, where status = null for BOOKED/PENDING sessions, status = "rejected" or status = "cancelled"
    public static void getStudentSlots(Student student, String status, SlotListCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        List<String> ids;
        boolean filterByCancelled = false; // the cancelled slots still have a reference to the student, so we need to query them differently!

        if (status == null) {
            // “normal” active sessions: booked/pending/etc., all in sessionTokens
            ids = student.getSessionTokens();
        } else if ("rejected".equals(status)) {
            // rejected sessions must come from the student’s own list
            ids = student.getRejectedSessionTokens();
        } else if ("cancelled".equals(status)) {
            // cancelled sessions are still in sessionTokens, but we filter by slot.status == "cancelled"
            ids = student.getSessionTokens();
            filterByCancelled = true;
        } else {
            callback.onFailure("Unknown status: " + status);
            return;
        }

        if (ids == null || ids.isEmpty()) {
            callback.onSuccess(new ArrayList<>());
            return;
        }

        List<Task<QuerySnapshot>> tasks = new ArrayList<>();

        // batch into chunks of 10 for whereIn(FieldPath.documentId()) since firebase limits a max of 10 at once! so we need subtasks
        for (int i = 0; i < ids.size(); i += 10) {
            List<String> batch = ids.subList(i, Math.min(i + 10, ids.size())); // break the id's into batches of 10 max

            Query q = db.collectionGroup("timeSlots").whereIn(FieldPath.documentId(), batch);

            if (filterByCancelled) {
                q = q.whereEqualTo("status", "cancelled");
            }

            tasks.add(q.get()); // add the task to the queue
        }

        Tasks.whenAllSuccess(tasks) // when everything has finished:
                .addOnSuccessListener(results -> {
                    List<DocumentSnapshot> docs = new ArrayList<>();
                    for (Object r : results) {
                        QuerySnapshot qs = (QuerySnapshot) r;
                        docs.addAll(qs.getDocuments());
                    }

                    if (docs.isEmpty()) {
                        callback.onSuccess(new ArrayList<>());
                        return;
                    }

                    // map everything back to the proper format (TimeSlot)
                    List<TimeSlot> slotList = new ArrayList<>();
                    final int[] remaining = { docs.size() };
                    // below is mostly reused from TutorHandling
                    for (DocumentSnapshot doc : docs) {
                        Timestamp sTs = doc.getTimestamp("startInstant");
                        Timestamp eTs = doc.getTimestamp("endInstant");
                        String zoneId = doc.getString("zoneId");
                        Boolean requireApproval = doc.getBoolean("requireApproval");

                        String docStatus = doc.getString("status");
                        String effectiveStatus;

                        // if we are explicitly asking for REJECTED sessions, we must show them as "rejected" to the student!
                        if ("rejected".equals(status)) {
                            effectiveStatus = "rejected"; // so we use an 'effective' status, thus when we query for rejected slots, we return a timeslot with status = rejected
                            // even though the TRUE database status is open, so someone else may book!
                        } else {
                            effectiveStatus = docStatus;
                        }

                        String tutorEmail = doc.getString("tutorEmail");
                        String studentEmail = doc.getString("studentEmail");

                        // skip cancelled if we're in the "normal" (null) case
                        if (status == null && "cancelled".equals(docStatus)) {
                            if (--remaining[0] == 0) {
                                slotList.sort(Comparator.comparing(TimeSlot::getStartDate));
                                callback.onSuccess(slotList);
                            }
                            continue;
                        }
                        assert sTs != null;
                        assert eTs != null;

                        ZonedDateTime start = ZonedDateTime.ofInstant(sTs.toDate().toInstant(), ZoneId.of(zoneId));
                        ZonedDateTime end = ZonedDateTime.ofInstant(eTs.toDate().toInstant(), ZoneId.of(zoneId));

                        getAccounts(tutorEmail, studentEmail, new AccountListCallback() { // reused helper
                            @Override
                            public void onSuccess(List<Object> accounts) {
                                Tutor t = (Tutor) accounts.get(0);
                                Student s = null;
                                if (accounts.get(1) != null) {
                                    s = (Student) accounts.get(1);
                                }

                                slotList.add(new TimeSlot(t, requireApproval, start, end, s, effectiveStatus, doc.getId()));

                                if (--remaining[0] == 0) {
                                    slotList.sort(Comparator.comparing(TimeSlot::getStartDate));
                                    callback.onSuccess(slotList);
                                }
                            }

                            @Override
                            public void onFailure(String err) {
                                if (--remaining[0] == 0) {
                                    slotList.sort(Comparator.comparing(TimeSlot::getStartDate));
                                    callback.onSuccess(slotList);
                                }
                            }
                        });
                    }
                })
                .addOnFailureListener(e ->
                        callback.onFailure("Error fetching student slots: " + e.getMessage())
                );
    }


    // method for a student to book a session
    public void bookSession(TimeSlot slot, Student student, AccountCallback callback) {
        // first, make sure the session doesn't overlap with an existing booked/pending session! cancelled or rejected is fine
        // query all pending/booked:
        getStudentSlots(student, null, new SlotListCallback() {
            @Override
            public void onSuccess(List<TimeSlot> slots) {
                // 1) Check for overlaps
                if (slots != null) {
                    for (TimeSlot existing : slots) {
                        if (TimeSlot.isOverlap(slot, existing)) {
                            callback.onFailure("This session overlaps with an already booked or pending session.");
                            return;
                        }
                    }
                }

                // success - no overlaps, proceed:
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    // Find tutor's doc by email
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

                                // Read current status, then update if open
                                slotRef.get()
                                        .addOnFailureListener(e -> callback.onFailure("Failed to find slot: " + e.getMessage()))
                                        .addOnSuccessListener(foundSlot -> {
                                            if (!foundSlot.exists()) {
                                                callback.onFailure("Time slot not found");
                                                return;
                                            }

                                            String status = foundSlot.getString("status"); // get the status of the slot (open, booked, cancelled)

                                            if (!"open".equals(status)) {
                                                callback.onFailure("Time slot is already booked, or pending."); // callback if the slot is already booked/pending
                                                return;
                                            }



                                            Map<String, Object> updates = new HashMap<>(); // make a hashmap that has all the changes

                                            Boolean requireApproval = foundSlot.getBoolean("requireApproval");
                                            if(Boolean.TRUE.equals(requireApproval)){
                                                updates.put("status", "pending");
                                            }
                                            else{ updates.put("status", "booked");}

                                            updates.put("studentEmail", student.getEmail());

                                            slotRef.update(updates) // update the info that we changed :)
                                                    .addOnSuccessListener(v ->{
                                                        callback.onSuccess("Slot booked");
                                                        // add the session ID to the student's object to make querying easy
                                                        updateBookedSession(student, slotRef.getId());})
                                                    .addOnFailureListener(e -> callback.onFailure("Booking failed: " + e.getMessage()));
                                        });
                            });
                }

            @Override
            public void onFailure(String error) {
                callback.onFailure("An error occurred while querying your existing sessions.");
            }
        });


    }
    public void updateBookedSession(Student student, String docRef){
        if (student == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        student.addSessionToken(docRef);

        DocumentReference studentRef = db.collection("accounts").document(student.getEmail());
        studentRef.update("sessionTokens", FieldValue.arrayUnion(docRef));
    }

    public void rateTutor(TimeSlot slot, int rating){ // where slot = the reference to the completed slot, rating belongs to [1, 2, 3, 4, 5]
        Tutor tutor = slot.getTutor();
        tutor.rate(rating); // .rate already updates the tutor object's instance vars rating & numRatings

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference tutorRef = db.collection("accounts").document(tutor.getEmail());

        Map<String, Object> updates = new HashMap<>(); // put changes in a hashmap
        updates.put("rating", tutor.getRating());
        updates.put("numRatings", tutor.getNumRatings());

        tutorRef.update(updates); // update the rating & num ratings for the tutor

    }

    public void cancelSession(TimeSlot slot, AccountCallback callback){
        // get current time & see if it is within 24hrs:
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("America/Toronto"));
        boolean within24hrs = Math.abs(Duration.between(now, slot.getStartDate()).toSeconds()) < 24 * 3600; // check to see if its within 24 hours of each other
        // we can safely use the absolute value as long as we are not allowing any PAST sessions to be cancelled (should be handled through UI)
        if(within24hrs){
            callback.onFailure("The session begins in less than 24 hours and cannot be cancelled.");
        }else{
            TutorHandling.cancelTimeSlot(slot, new AccountCallback() { // reuse the cancel method from TutorHandling
                @Override
                public void onSuccess(String msg) {
                    slot.setStatus("cancelled");
                    callback.onSuccess("Cancelled booking successfully.");
                }

                @Override
                public void onFailure(String msg) {
                    callback.onFailure(msg);
                }
            });
        }

    }



}
