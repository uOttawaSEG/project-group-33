package com.example.group_33_project;

public class Student extends Account {
    private String program, status;

    public Student(){};

    // DEFAULT CONSTRUCTOR FOR NEW SIGNUPS
    Student(String firstName, String lastName, String email, String password,  String phone, String program) {
        super(firstName, lastName, email, password, phone);
        this.program = program;
        this.status = "pending"; // new signups are all PENDING by default
    }
    // CONSTRUCTOR FOR FIRESTORE
    Student(String firstName, String lastName, String email, String password,  String phone, String program, String status) {
        super(firstName, lastName, email, password, phone);
        this.program = program;
        this.status = status; // new signups are all PENDING by default
    }

    //setters
    public void setProgram(String s){
        program = s;
    }
    public void setStatus(String s){
        status = s;
    }

    //getters
    public String getProgram(){
        return program;
    }
    public String getStatus(){
        return status;
    }
}
