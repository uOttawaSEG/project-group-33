package com.example.group_33_project;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class AccountHandling { // to deal with sign in, sign up
    FirebaseFirestore db = FirebaseFirestore.getInstance(); // to access firestore database

    void queryAccountByEmail(String email, QueryCallback callback){
        db.collection("approvedAccounts").whereEqualTo("email", email.toLowerCase()) // query filters
                .get().addOnSuccessListener(querySnapshot -> { // save all instances of matching accounts to a 'snapshot'
                    if (!querySnapshot.isEmpty()){ // if the snapshot is NOT empty, we must have a match!
                        DocumentSnapshot doc = querySnapshot.getDocuments().get(0); // because of sign up restrictions, there should only be 1 account per email
                        Account account = doc.toObject(Account.class); // convert back to an account object (so we can access things) :)
                        callback.onSuccess(account); // callback the found account so that it may be used
                    }
                    else{
                        callback.onFailure("An account could not be found with the associated email " + email); // otherwise no account found (GOOD for signUp, BAD for logIn)
                    }
                });
    }


    void studentSignUp(String firstName, String lastName, String email, String password,  String phone, String program, AccountCallback callback){ // to sign up for an account + add to the database
        // first check if an account with the given email already exists (query the database)
        queryAccountByEmail(email, new QueryCallback(){ // define the callback methods for this case!
            public void onSuccess(Account account){ // if theres an email found, that's a problem!!
                callback.onFailure("An account with the email " + email + " already exists. Please try to sign in.\""); // callback to indicate error!
            }
            public void onFailure(String message){ // no email found -> good to make a new account :)
                Account account = new Student(firstName, lastName, email, password, phone, program);
                db.collection("approvedAccounts").add(account); // add the account to the database
                //NOTE: functionality for approving accounts has not been made yet, so all are approved by default (for now)

                callback.onSuccess("The account was successfully created. Please sign in."); // callback for success!

            }
        });


    }

    void tutorSignUp(String firstName, String lastName, String email, String password,  String phone, String education, String[] courses, AccountCallback callback){ // to sign up for an account + add to the database
        // first check if an account with the given email already exists (query the database)
        queryAccountByEmail(email, new QueryCallback(){ // define the callback methods for this case!
            public void onSuccess(Account account){ // if theres an email found, that's a problem!!
                callback.onFailure("An account with the email " + email + " already exists. Please try to sign in.\""); // callback to indicate error!
            }
            public void onFailure(String message){ // no email found -> good to make a new account :)
                Account account = new Tutor(firstName, lastName, email, password, phone, education, courses);
                db.collection("approvedAccounts").add(account); // add the account to the database
                //NOTE: functionality for approving accounts has not been made yet, so all are approved by default (for now)

                callback.onSuccess("The account was successfully created. Please sign in."); // callback for success!

            }
        });


    }

    void logIn(String email, String password, AccountCallback callback) { // method to SIGN INTO an established account
        queryAccountByEmail(email, new QueryCallback(){ // define the callback methods for this case!
            public void onSuccess(Account account){ // if theres an email found, we need to check if the password matches!
                if (password.equals(account.getPassword())){ // if the passwords match
                    callback.onSuccess("Successfully logged in."); //callback to main to say that we have logged in!
                }
                else{
                    callback.onFailure("Invalid password. Try again."); // otherwise, call back as a failure!
                }
            }
            public void onFailure(String message){ // if there isn't an email found:
                callback.onFailure("No account found with the provided email address."); // call the message
            }
        });
    }
}
