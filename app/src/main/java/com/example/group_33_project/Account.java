package com.example.group_33_project;

import java.io.Serial;
import java.io.Serializable;

public abstract class Account implements Serializable { // implementing Serializable allows us to pass Account objects as intents between activities!
    private String firstName, lastName, email, password, phone = "", type; // instance variables

    public Account(){ // empty constructor
        this.type = this.getClass().getSimpleName(); // MUST include the type even with empty constructor (so that admin type is initialized!)
    }

    // parametrized constructor
    public Account(String firstName, String lastName, String email, String password,  String phone){
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
        this.setEmail(email);
        this.setPhone(phone);
        this.type = this.getClass().getSimpleName(); // getSimpleName gives us the simple name of the class, i.e. "Student" or "Tutor" (or admin)
    }

    //setters:
    public void setFirstName(String s){
        this.firstName = s;
    } public void setLastName(String s) {
        this.lastName = s;
    } public void setEmail(String s){
        this.email = s.toLowerCase();
    } public void setPassword(String s){
        this.password = s;
    }
    public void setPhone(String s){ // Sets the phone number, removing non-numeric characters
        this.phone = s.replaceAll("\\D", ""); // replace all digits ("\\D" is regex for NON digits) with ""
    }
    public void setType(){
        this.type = this.getClass().getSimpleName();
    }

    // getters
    public String getFirstName() {
        return firstName;
    } public String getLastName(){
        return lastName;
    } public String getEmail(){
        return email;
    } public String getPassword(){
        return password;
    } public String getPhone(){
        return phone;
    }
    //Gets type for the collection database to be added to ** WILL BE USED IN FUTURE IMPLEMENTATION
    public String getType(){
        return this.type; //getSimpleName() returns the string of the classname, without the .class :)
    }

}
