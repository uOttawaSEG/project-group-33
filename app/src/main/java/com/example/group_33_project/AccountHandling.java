package com.example.group_33_project;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class AccountHandling {

    private FirebaseFirestore db;

    public AccountHandling() {
        db = FirebaseFirestore.getInstance();
    }

    public void queryAccountByEmail(String email, AccountCallback callback) {
        db.collection("approvedAccounts")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        for (QueryDocumentSnapshot doc : querySnapshot) {
                            String type = doc.getString("type");

                            Account account = null;
                            if ("ADMIN".equalsIgnoreCase(type)) {
                                account = doc.toObject(Admin.class);
                            }
//                            } else if ("STUDENT".equalsIgnoreCase(type)) {
//                                account = doc.toObject(Student.class);
//                            } else if ("TUTOR".equalsIgnoreCase(type)) {
//                                account = doc.toObject(Tutor.class);
//                            }

                            if (account != null) {
                                callback.onSuccess("Found account: " + type);
                            } else {
                                callback.onFailure("Account type unknown");
                            }
                        }
                    } else {
                        callback.onFailure("No account found with that email");
                    }
                })
                .addOnFailureListener(e -> callback.onFailure("Error: " + e.getMessage()));
    }

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
