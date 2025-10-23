package com.example.group_33_project;

import java.util.List;

public class Tutor extends Account{



    // Attributes for TUTORS:
    private String education, status;
    private List<String> courses;

    public Tutor(){};

    // Constructor FOR NEW SIGNUPS (
    Tutor(String firstName, String lastName, String email, String password,  String phone, String education, List<String> courses){
        super(firstName, lastName, email, password, phone); // call super for Account constructor -> AUTOMATICALLY SETS AS PENDING
        this.courses = courses;
        this.education = education;
        this.status = "pending"; // all new signups are pending by default
    }

    // Constructor FOR FIRESTORE
    Tutor(String firstName, String lastName, String email, String password,  String phone, String education, List<String> courses, String status){
        super(firstName, lastName, email, password, phone); // call super for Account constructor -> AUTOMATICALLY SETS AS PENDING
        this.courses = courses;
        this.education = education;
        this.status = status; // all new signups are pending by default
    }

    // Setters
    public void setEducation(String education) {
        this.education = education;
    }
    public void setCourses(List<String> courses){
        this.courses = courses;
    }
    public void setStatus(String s){ this.status = s;}

    // Getters for tutor-specific variables
    public String getEducation() {
        return education;
    }
    public List<String> getCourses(){
        return courses;
    }
    public String getStatus(){return status;}

}
