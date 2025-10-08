package com.example.group_33_project;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class AccountHandling {

    private FirebaseFirestore db;

    public AccountHandling() {
        db = FirebaseFirestore.getInstance();
    }


    public void queryAccount(String email, QueryCallback callback) { // method to query the database for an account
        db.collection("approvedAccounts")// check in the approvedAccounts collection
                .whereEqualTo("email", email) // querying by email
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) { // if there is an account found,
                        for (QueryDocumentSnapshot doc : querySnapshot) {
                            String type = doc.getString("type");
                            if (type.equals("Student")) {
                                callback.onSuccess(doc.toObject(Student.class));
                            } else if (type.equals("Tutor")) {
                                callback.onSuccess(doc.toObject(Tutor.class)); // convert the document back to an account object to easily access attributes, and callback
                            }
                        }
                    } else {
                        callback.onFailure("No account found with the email " + email); // else, there was no accounts found. callback error
                    }
                })
                .addOnFailureListener(e -> callback.onFailure("Error: " + e.getMessage())); // safety net in case the database fails to query
    }

    //Checks to find if the account is in the approved list on Firebase
    //Currently login of pendingAccounts doesn't work. WILL IMPLEMENT FOR DELIVERABLE 2
    // First check if the admin is signing in; if not:
    // Opens up Firebase collections (into approvedAccounts) -> goes to and gets emails
    // Checks to see if the document field of password is correct
    // If so, then login successfully
    public void attemptLogIn(String email, String password, QueryCallback callback) {
        String emailLowerCase = email.toLowerCase(); // standard convention
        // FIRST check if the admin is logging in:
        if (emailLowerCase.equals(Administrator.getInstance().getEmail())) {
            if (password.equals(Administrator.getInstance().getPassword())) { // if the admin email and passwords match:
                callback.onSuccess(Administrator.getInstance()); // Pass the instance of ADMIN
                return;
            } else {
                callback.onFailure("Invalid password");
                return;
            } // make sure we return if we tried to login the admin, or else we will get error messages saying the account can't be found
        }
        // else, search through the database instead, querying by EMAIL (each email should be UNIQUE, but passwords for example may not be unique)
        queryAccount(emailLowerCase, new QueryCallback() { // must define the QueryCallback abstract methods
            public void onSuccess(Account account) { // if we successfully queried an account, we can attempt to log in:
                if (password.equals(account.getPassword())) { // if the passwords match,
                    callback.onSuccess(account); // successful callback -> i.e. login success
                } else {
                    callback.onFailure("Invalid password"); // else, invalid password
                }
            }

            public void onFailure(String msg) { // if we could not query an account,
                callback.onFailure("No account found with the email " + email); // no account exists with the email
            }
        });
    }


    //Implementation of Signup moved from Main activity to Account handling for simplicity
    //Adding either tutor or student into the approvedAccounts in Firebase
    //Opens Firebase collections -> sets documentID to email -> attempts to add the account
    public void signUp(Account account, AccountCallback callback) {
        // first check if we are trying to sign up with the ADMIN EMAIL (not allowed!)
        if (account.getEmail().equals(Administrator.getInstance().getEmail())) {
            callback.onFailure("An account with this email already exists.");
            return;
        }

        // next check if there is an email already associated to an account:
        queryAccount(account.getEmail(), new QueryCallback() {
            @Override
            public void onSuccess(Account account) { // onSuccess means that we found an account, thus we can't proceed!
                callback.onFailure("An account with this email already exists.");
            }

            @Override
            public void onFailure(String errorMessage) { // if we couldn't find an account, time to sign up!
                db.collection("approvedAccounts")
                        .document(account.getEmail()) // email as ID -> this ensures NO DUPLICATE ACCOUNTS (with same EMAIL) will exist!
                        .set(account) // store in the db
                        .addOnSuccessListener(aVoid -> callback.onSuccess("Account registered! Please sign in."))
                        .addOnFailureListener(e -> callback.onFailure("Error: " + e.getMessage()));
            }
        });
    }
}


