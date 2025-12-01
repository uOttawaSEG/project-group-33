package com.example.group_33_project;
import java.time.*;

// This class stores a timeslot, which consists of a start and end date, the tutor whom created the slot, the student which has booked, and the status of the slot.
public class TimeSlot {
    ZonedDateTime startDate, endDate;

    private Tutor tutor; // the tutor who has created the slot
    private Student student; // the student who has booked the slot, if applicable
    private String status; // status = "booked", "open", "pending" (if requires approval), or "cancelled"
    private final Boolean requireApproval; // whether or not the booking must be approved by an admin
    private String ID; // stores the FIRESTORE ID from the database
    private boolean rated = false; // false by default

    // constructor for NEW TIMESLOTS
    TimeSlot(Tutor tutor, Boolean requireApproval, ZonedDateTime start, ZonedDateTime end){
        this.tutor = tutor;
        this.requireApproval = requireApproval;
        this.startDate = start;
        this.endDate = end;
        this.status = "open"; // upon creation, the slot should not be booked
    }

    // Parametrized constructor (for firestore)
    TimeSlot(Tutor tutor, Boolean requireApproval, ZonedDateTime start, ZonedDateTime end, Student student, String status, String ID, boolean rated){ // constructor for NEW TIMESLOTS
        this.tutor = tutor;
        this.requireApproval = requireApproval;
        this.startDate = start;
        this.endDate = end;
        this.student = student;
        this.status = status;
        this.ID = ID;
        this.rated = rated;
    }

    // Method to determine if a new timeslot overlaps with an existing slot
    public static boolean isOverlap(TimeSlot newSlot, TimeSlot existingSlot){
        return newSlot.startDate.isBefore(existingSlot.endDate) && newSlot.endDate.isAfter(existingSlot.startDate);
    }



    // Getters
    public ZonedDateTime getStartDate(){
        return this.startDate;
    }

    public ZonedDateTime getEndDate(){
        return this.endDate;
    }

    public int getStartDay(){
        return this.startDate.getDayOfMonth();
    }
    public int getEndDay(){
        return this.endDate.getDayOfMonth();
    }
    public int getStartMonth(){
        return this.startDate.getMonthValue();
    }
    public int getEndMonth(){
        return this.endDate.getMonthValue();
    }
    public Boolean getRequireApproval() {
        return requireApproval;
    }

    public Student getStudent(){
        return student;
    }

    public Tutor getTutor(){
        return tutor;
    }

    public String getStatus(){
        return this.status;
    }
    public String getID() { return ID; }

    public boolean isRated(){ return rated;}

    // Setters


    public void setStartDate(ZonedDateTime date){
        this.startDate = date;
    }

    public void setEndDate(ZonedDateTime date){
        this.endDate = date;
    }

    public void setStatus(String status){
        this.status = status;
    }

    public void setStudent(Student student){
        this.student = student;
        this.status = "booked"; // when we assign a student to the slot, the slot is BOOKED
    }

    public void setID(String id) { this.ID = id; }

    public void setTutor(Tutor tutor){
        this.tutor = tutor;
    }
    public void wasRated(){ this.rated = true;}
}
