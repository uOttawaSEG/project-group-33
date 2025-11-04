package com.example.group_33_project;
import java.time.*;

// This class stores a timeslot, which consists of a start and end date, the tutor whom created the slot, the student which has booked, and the status of the slot.
public class TimeSlot {
    ZonedDateTime startDate, endDate;

    private final Tutor tutor; // the tutor who has created the slot
    private Student student; // the student who has booked the slot, if applicable
    private Boolean isBooked; // booked = T or F
    private final Boolean requireApproval; // whether or not the booking must be approved by an admin


    // constructor for NEW TIMESLOTS
    TimeSlot(Tutor tutor, Boolean requireApproval, ZonedDateTime start, ZonedDateTime end){
        this.tutor = tutor;
        this.requireApproval = requireApproval;
        this.startDate = start;
        this.endDate = end;
        this.isBooked = false; // upon creation, the slot should not be booked
    }

    // Parametrized constructor (for firestore)
    TimeSlot(Tutor tutor, Boolean requireApproval, ZonedDateTime start, ZonedDateTime end, Student student, Boolean isBooked){ // constructor for NEW TIMESLOTS
        this.tutor = tutor;
        this.requireApproval = requireApproval;
        this.startDate = start;
        this.endDate = end;
        this.student = student;
        this.isBooked = isBooked;
    }

    // Method to determine if a new timeslot overlaps with an existing slot
    private static boolean isOverlap(TimeSlot newSlot, TimeSlot existingSlot){
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

    public boolean isBooked(){
        return isBooked;
    }

    // Setters
    public void setStartDate(ZonedDateTime date){
        this.startDate = date;
    }

    public void setEndDate(ZonedDateTime date){
        this.endDate = date;
    }

    public void setStudent(Student student){
        this.student = student;
        this.isBooked = true; // when we assign a student to the slot, the slot is BOOKED
    }



}
