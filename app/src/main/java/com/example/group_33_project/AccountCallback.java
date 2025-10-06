package com.example.group_33_project;

// we must use callbacks because of the asynchronous behaviour of the firebase database -> if we try to throw exceptions or use other methods, it would freeze the main thread!
// thus we use callbacks to allow the main thread to run smoothly, and then receive the data when the queries are complete :)

// the onSuccess and onFailure methods must be defined for each usage, as it depends on the use case!
public interface AccountCallback { // to handle issues regarding SIGN UP/SIGN IN
    void onSuccess(String msg);
    void onFailure(String msg);
}
