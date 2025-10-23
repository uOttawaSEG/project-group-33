package com.example.group_33_project;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AccountHandling {

    private FirebaseFirestore db;

    public AccountHandling() {
        db = FirebaseFirestore.getInstance();
    }


    public void queryAccount(String email, QueryCallback callback) { // method to query the database for an account
        db.collection("accounts")// check in the accounts collection
                .whereEqualTo("status", "approved") // FILTER BY APPROVED ACCOUNTS
                .whereEqualTo("email", email) // querying by email
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) { // if there is an account found,
                        for (QueryDocumentSnapshot doc : querySnapshot) {
                            String type = doc.getString("type");
                            if ("Student".equals(type)) {
                                callback.onSuccess(doc.toObject(Student.class));
                            } else if ("Tutor".equals(type)) {
                                callback.onSuccess(doc.toObject(Tutor.class)); // convert the document back to an account object to easily access attributes, and callback
                            }
                        }
                    } else {
                        callback.onFailure("No account found with the email " + email); // else, there was no accounts found. callback error
                    }
                })
                .addOnFailureListener(e -> callback.onFailure("Error: " + e.getMessage())); // safety net in case the database fails to query
    }

    public void getAccounts(String status, PendingCallback callback){ // status = "pending" or "denied"
        List<Account> accounts = new ArrayList<Account>(); // set up empty pending accounts list
        db.collection("accounts")
                .whereEqualTo("status", status) // TO FILTER FOR PENDING ACCOUNTS!
                .get()
                .addOnSuccessListener(querySnapshot -> { // query the database for ALL documents within pendingAccounts collection
                    if (!querySnapshot.isEmpty()) {
                        for (QueryDocumentSnapshot doc : querySnapshot) { // for each document (account) found,
                            String type = doc.getString("type");
                            if ("Student".equals(type)){
                                accounts.add(doc.toObject(Student.class)); // convert the document back to an account object to easily access attributes, and add to the array list!
                            } else if ("Tutor".equals(type)) {
                                accounts.add(doc.toObject(Tutor.class));
                            }
                        }
                        callback.onSuccess(accounts); // once we loop through each doc in the db collection, we can callback the arraylist
                    }
                    else{
                        callback.onFailure("No pending accounts found."); // if the snapshot IS empty, there are no accounts found in the db collection
                    }
                })
                .addOnFailureListener(e -> callback.onFailure("Error: " + e.getMessage())); //safety net
    }


    //Checks to find if the account is in the approved list on Firebase
    // First check if the admin is signing in; if not:
    // Opens up Firebase collections (into accounts) -> goes to and gets emails
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
    //Adding either tutor or student into  accounts in Firebase
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
                db.collection("accounts")
                        .document(account.getEmail()) // email as ID -> this ensures NO DUPLICATE ACCOUNTS (with same EMAIL) will exist!
                        .set(account) // store in the db
                        .addOnSuccessListener(aVoid -> callback.onSuccess("Account registration sent! Please wait for approval before signing in."))
                        .addOnFailureListener(e -> callback.onFailure("Error: " + e.getMessage()));
            }
        });
    }

    public void approve(Account acc){
        db.collection("accounts")
                .document(acc.getEmail())
                .update("status", "approved"); // UPDATE THE STATUS TO APPROVED!
    }

    public void deny(Account acc){
        db.collection("accounts")
                .document(acc.getEmail())
                .update("status", "denied"); // UPDATE THE STATUS TO DENIED!
    }

}


