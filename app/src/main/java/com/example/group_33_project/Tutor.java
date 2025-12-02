package com.example.group_33_project;
import java.io.Serializable;
import java.util.List;

public class Tutor extends Account implements Serializable {
    // Attributes for TUTORS:
    private String education, status;
    private List<String> courses;

    private int numRatings;
    private double rating;

    public Tutor(){}

    // Constructor FOR NEW SIGNUPS (
    Tutor(String firstName, String lastName, String email, String password,  String phone, String education, List<String> courses){
        super(firstName, lastName, email, password, phone); // call super for Account constructor -> AUTOMATICALLY SETS AS PENDING
        this.courses = courses;
        this.education = education;
        this.numRatings = 0; // starting with 0 ratings
        this.rating = 0; // so the rating is 0/5
        this.status = "pending"; // all new signups are pending by default
    }

    // Constructor FOR FIRESTORE
    Tutor(String firstName, String lastName, String email, String password,  String phone, String education, List<String> courses, String status, int numRatings, double rating){
        super(firstName, lastName, email, password, phone); // call super for Account constructor -> AUTOMATICALLY SETS AS PENDING
        this.courses = courses;
        this.education = education;
        this.status = status; // all new signups are pending by default
        this.numRatings = numRatings;
        this.rating = rating;
    }

    // Setters
    public void setEducation(String education) {
        this.education = education;
    }
    public void setCourses(List<String> courses){
        this.courses = courses;
    }
    public void setStatus(String s){ this.status = s;}
    public void rate(int ratingOutOfFive){
        this.rating = ((this.rating*this.numRatings)+ratingOutOfFive)/++this.numRatings; // increment the numRatings before computing the rating, since we just added the NEW rating
    }
    // Getters for tutor-specific variables
    public String getEducation() {
        return education;
    }
    public List<String> getCourses(){
        return courses;
    }
    public String getStatus(){return status;}
    public double getRating(){ return rating;}
    public int getNumRatings(){return numRatings;}

}
