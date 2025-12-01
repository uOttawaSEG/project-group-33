package com.example.group_33_project;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import java.util.HashMap;
import java.util.Map;
import android.util.Base64;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


public class AccountHandling {
    private FirebaseFirestore db;

    public AccountHandling() {
        db = FirebaseFirestore.getInstance();
    }

    public static void queryAccount(String email, QueryCallback callback) { // method to query the database for an account
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("accounts")// check in the accounts collection
                // .whereEqualTo("status", "approved") // FILTER BY APPROVED ACCOUNTS  <-- REMOVED so we can see pending/denied too
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
                            } else {
                                callback.onFailure("Unknown account type for " + email);
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

                // --- NEW: read status/password directly from Firestore (no getStatus() needed) ---
                db.collection("accounts")
                        .whereEqualTo("email", emailLowerCase) // re-fetch the same doc to read raw fields
                        .limit(1)
                        .get()
                        .addOnSuccessListener(snap -> {
                            if (snap.isEmpty()) {
                                callback.onFailure("No account found with the email " + email);
                                return;
                            }

                            DocumentSnapshot doc = snap.getDocuments().get(0);
                            String status = doc.getString("status"); // "pending", "approved", "denied"/"rejected"
                            String storedPassword = doc.getString("password");

                            // --- check password FIRST so wrong creds are reported even for pending users ---
                            if (storedPassword == null || !storedPassword.equals(password)) {
                                callback.onFailure("Invalid password"); // else, invalid password
                                return;
                            }

                            // --- handle status messaging AFTER password is confirmed ---
                            if ("pending".equalsIgnoreCase(status)) {
                                // if the user's account is pending, tell them clearly
                                callback.onFailure("Your account is awaiting approval. Please try again later.");
                                return;
                            } else if ("denied".equalsIgnoreCase(status) || "rejected".equalsIgnoreCase(status)) {
                                // if rejected/denied, block login with a clear reason
                                callback.onFailure("Your account is rejected. Please contact the administrator at +1 (734) 334-7687");
                                return;
                            } else if (!"approved".equalsIgnoreCase(status)) {
                                // fallback if status is missing/unknown
                                callback.onFailure("Your account is not approved yet.");
                                return;
                            }

                            // if approved and password already matched above, proceed
                            callback.onSuccess(account); // successful callback -> i.e. login success

                        })
                        .addOnFailureListener(e -> callback.onFailure("Error: " + e.getMessage()));
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
        sendModerationEmail(acc.getEmail(), true);
    }

    public void deny(Account acc){
        db.collection("accounts")
                .document(acc.getEmail())
                .update("status", "denied"); // UPDATE THE STATUS TO DENIED!
        sendModerationEmail(acc.getEmail(), false);
    }

    //The way that the email notifications work is when the
    // status of the user account changes from approve or reject
    //Via. (Firebase Trigger Email Extension through a SMTP Email server created by me (Daniel Nguyen)
    private void sendModerationEmail(String toEmail, boolean approved) {
        String subject = approved ? "Your account was approved"
                : "Your account was rejected";

        // Plain-text version
        String text = approved
                ? "Hi,\n\nYour account has been approved.\n\n— SEG33 Project Team"
                : "Hi,\n\nUnfortunately your account was rejected.\n\n— SEG33 Project Team";

        // HTML version
        String html = approved
                ? "<p>Hi,</p><p>Your account has been <b>approved</b>. <br>You can now log in.</p><p>Have a beautiful day — SEG33 Project Team</p>"
                : "<p>Hi,</p><p>Unfortunately your account was <b>rejected</b>. <br>Please contact the administrator at <b>+1 (734) 334-7687</b> </p><p>Have a beautiful day — SEG33 Project Team</p>";

        //NOTE: Plaintext and html must be sent so that the email doesn't get filtered into spam right away
        Map<String, Object> message = new HashMap<>();
        message.put("subject", subject);
        message.put("text", text);
        message.put("html", html);
        message.put("replyTo", "seg33project@gmail.com"); // safe to set for Gmail

        Map<String, Object> mailDoc = new HashMap<>();
        mailDoc.put("to", toEmail);
        mailDoc.put("from", "seg33project@gmail.com"); // MUST match Gmail sender
        mailDoc.put("message", message);

        db.collection("mail").add(mailDoc);
    }

    // Send an email with an .ics attachment via Firestore "mail" collection (Trigger Email extension)
    public Task<DocumentReference> sendCalendarEmail(String toEmail,
                                                     String subject,
                                                     String bodyText,
                                                     String icsContent) {
        if (toEmail == null || toEmail.isBlank()) {
            return null;
        }
        if (icsContent == null || icsContent.isBlank()) {
            return null;
        }

        String text = (bodyText != null && !bodyText.isBlank())
                ? bodyText
                : "Attached is your tutoring schedule (.ics) you can import into your calendar.";
        String html = "<p>" + text + "</p>";

        // Base64 encode ICS for the extension
        String base64Ics = Base64.encodeToString(
                icsContent.getBytes(StandardCharsets.UTF_8),
                Base64.NO_WRAP
        );

        // Standard message fields
        Map<String, Object> message = new HashMap<>();
        message.put("subject", subject);
        message.put("text", text);
        message.put("html", html);
        message.put("replyTo", "seg33project@gmail.com");

        // Attachments array
        List<Map<String, Object>> attachments = new ArrayList<>();
        Map<String, Object> attachment = new HashMap<>();
        attachment.put("filename", "tutoring_sessions.ics");
        attachment.put("content", base64Ics);
        attachment.put("encoding", "base64");
        attachments.add(attachment);

        message.put("attachments", attachments);

        Map<String, Object> mailDoc = new HashMap<>();
        mailDoc.put("to", toEmail);
        mailDoc.put("from", "seg33project@gmail.com");
        mailDoc.put("message", message);

        // Return Task so caller can show success/failure Toasts
        return db.collection("mail").add(mailDoc);
    }

}


