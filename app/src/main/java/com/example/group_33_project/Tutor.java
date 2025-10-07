package com.example.group_33_project;

public class Tutor extends Account{


    // Attributes for TUTORS:
    private String education, type;
    private String[] courses;

    // Constructor
    Tutor(String firstName, String lastName, String email, String password,  String phone, String education, String[] courses){
        super(firstName, lastName, email, password, phone); // call super for com.example.group_33_project.Account constructor
        this.courses = courses;
        this.education = education;
        this.type = "Tutor";
    }
    // Setters
    public void setEducation(String education) {
        this.education = education;
    }
    public void setCourses(String[] courses){
        this.courses = courses;
    }

    // Getters for tutor-specific variables
    public String getEducation() {
        return education;
    }
    public String[] getCourses(){
        return courses;
    }
}
