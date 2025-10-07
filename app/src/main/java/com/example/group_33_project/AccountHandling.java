package com.example.group_33_project;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class AccountHandling {

    private FirebaseFirestore db;

    public AccountHandling() {
        db = FirebaseFirestore.getInstance();
    }

    //Checks to find if the account is in the approved list on Firebase
    //Currently login of pendingAccounts don't work. WILL IMPLEMENT FOR DELIVERABLE 2
    // Opens up Firebase collections (into approvedAccounts) -> goes to and gets emails
    //Checks to see if the document field of password is correct
    //If so, then login successfully
    public void queryAccountByEmail(String email, String password, AccountCallback callback) {
        db.collection("approvedAccounts")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        for (QueryDocumentSnapshot doc : querySnapshot) {
                            String dbPassword = doc.getString("password");
                            String type = doc.getString("type");

                            if (dbPassword != null && dbPassword.equals(password)) {
                                callback.onSuccess("Login successful: " + type);
                            } else {
                                callback.onFailure("Invalid password");
                            }
                        }
                    } else {
                        callback.onFailure("No account found with that email");
                    }
                })
                .addOnFailureListener(e -> callback.onFailure("Error: " + e.getMessage()));
    }



    //Implementation of Signup moved from Main activity to Account handling for simplicity
    //Adding either tutor or student into the pendingAccounts in Firebase
    //Opens Firebase collections -> sets documentID to student email -> attempts to add the student
    public void studentSignUp(Student student, AccountCallback callback) {
        db.collection("pendingAccounts")
                .document(student.getEmail()) // email as ID
                .set(student)
                .addOnSuccessListener(aVoid -> callback.onSuccess("Student registered, pending approval"))
                .addOnFailureListener(e -> callback.onFailure("Error: " + e.getMessage()));
    }

    //Opens Firebase collections -> sets documentID to tutor email -> attempts to add the tutor

    public void tutorSignUp(Tutor tutor, AccountCallback callback) {
        db.collection("pendingAccounts")
                .document(tutor.getEmail())
                .set(tutor)
                .addOnSuccessListener(aVoid -> callback.onSuccess("Tutor registered, pending approval"))
                .addOnFailureListener(e -> callback.onFailure("Error: " + e.getMessage()));
    }
}
