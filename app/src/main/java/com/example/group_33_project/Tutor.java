package com.example.group_33_project;

import java.util.List;

public class Tutor extends Account{



    // Attributes for TUTORS:
    private String education;
    private List<String> courses;

    public Tutor(){};

    // Constructor
    Tutor(String firstName, String lastName, String email, String password,  String phone, String education, List<String> courses){
        super(firstName, lastName, email, password, phone); // call super for com.example.group_33_project.Account constructor
        this.courses = courses;
        this.education = education;
    }
    // Setters
    public void setEducation(String education) {
        this.education = education;
    }
    public void setCourses(List<String> courses){
        this.courses = courses;
    }

    // Getters for tutor-specific variables
    public String getEducation() {
        return education;
    }
    public List<String> getCourses(){
        return courses;
    }

}
