package com.example.group_33_project;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class AccountHandling {

    private FirebaseFirestore db;

    public AccountHandling() {
        db = FirebaseFirestore.getInstance();
    }

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



    //Adding the different types into the database
    public void studentSignUp(Student student, AccountCallback callback) {
        db.collection("pendingAccounts")
                .document(student.getEmail()) // email as ID
                .set(student)
                .addOnSuccessListener(aVoid -> callback.onSuccess("Student registered, pending approval"))
                .addOnFailureListener(e -> callback.onFailure("Error: " + e.getMessage()));
    }

    public void tutorSignUp(Tutor tutor, AccountCallback callback) {
        db.collection("pendingAccounts")
                .document(tutor.getEmail())
                .set(tutor)
                .addOnSuccessListener(aVoid -> callback.onSuccess("Tutor registered, pending approval"))
                .addOnFailureListener(e -> callback.onFailure("Error: " + e.getMessage()));
    }
}
