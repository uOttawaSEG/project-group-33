package com.example.group_33_project;

// we must use callbacks because of the asynchronous behaviour of the firebase database -> if we try to throw exceptions or use other methods, it would freeze the main thread!
// thus we use callbacks to allow the main thread to run smoothly, and then receive the data when the queries are complete :)

// the onSuccess and onFailure methods must be defined for each usage, as it depends on the use case!
public interface QueryCallback { // making a callback that will allow us to handle errors during QUERYING EMAILS(i.e. onSuccess means email FOUND, else there was no email associated)
        void onSuccess(Account account);
        void onFailure(String errorMessage);
}
