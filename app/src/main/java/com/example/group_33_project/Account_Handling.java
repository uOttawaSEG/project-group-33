package com.example.group_33_project;

public class Account_Handling { // to deal with sign in, sign up

    void logIn(String email, String password) throws AuthenticationException, IllegalArgumentException{ // logIn method will return auth exception if it can't sign in,
        // or illegal arg exception if email is not found
        // parse database for correct password

        if (EMAIL NOT IN DATABASE){
            throw new IllegalArgumentException("An account could not be found with the associated email " + email);
        } else{
            if (password == CORRECT_PW){
                return;
            }
            if (password != CORRECT_PW){
                throw new AuthenticationException("Incorrect password.");
            }
        }
    }


}
