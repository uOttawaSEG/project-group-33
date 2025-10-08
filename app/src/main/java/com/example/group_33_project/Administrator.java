package com.example.group_33_project;

public class Administrator extends Account{
    private static final Administrator INSTANCE = new Administrator(); // set up a FINAL ADMIN INSTANCE -> only ONE admin account which is hard-coded (not just another random account in the db)
    private Administrator(){ // the singleton instance of Admin will have a hard-coded email and password.
        setEmail("admin33@seg.com");
        setPassword("admin2105");

    }

    public static Administrator getInstance(){ // in order to access the email/pw for attemptLogIn and signups
        return INSTANCE;
    }
}
