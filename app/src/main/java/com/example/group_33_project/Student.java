package com.example.group_33_project;

import java.util.ArrayList;

public class Student extends Account {
    private String program, status;
    private ArrayList<String> sessionTokens, rejectedSessionTokens; // to store the ID of the student's sessions

    public Student(){}

    // DEFAULT CONSTRUCTOR FOR NEW SIGNUPS
    Student(String firstName, String lastName, String email, String password,  String phone, String program) {
        super(firstName, lastName, email, password, phone);
        this.program = program;
        this.status = "pending"; // new signups are all PENDING by default
        this.sessionTokens = null;
        this.rejectedSessionTokens = null;
    }
    // CONSTRUCTOR FOR FIRESTORE
    Student(String firstName, String lastName, String email, String password,  String phone, String program, String status, ArrayList<String> sessionTokens, ArrayList<String> rejectedSessionTokens) {
        super(firstName, lastName, email, password, phone);
        this.program = program;
        this.status = status; // new signups are all PENDING by default
        this.sessionTokens = sessionTokens;
        this.rejectedSessionTokens = rejectedSessionTokens;

    }

    //setters
    public void setProgram(String s){
        program = s;
    }
    public void setStatus(String s){
        status = s;
    }
    public void addSessionToken(String token) {
        if (sessionTokens == null) {
            sessionTokens = new ArrayList<>();
        }
        sessionTokens.add(token);
    }
    public void addRejectedSessionToken(String token){
        this.rejectedSessionTokens.add(token);
    }
    public void removeSessionToken(String token){
        this.sessionTokens.remove(token);
    }
    //getters
    public String getProgram(){
        return program;
    }
    public String getStatus(){
        return status;
    }
    public ArrayList<String> getSessionTokens() {
        if (sessionTokens == null) {
            sessionTokens = new ArrayList<>();
        }
        return sessionTokens;
    }
    public ArrayList<String> getRejectedSessionTokens(){
        return rejectedSessionTokens;
    }

}
