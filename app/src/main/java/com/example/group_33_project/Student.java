package com.example.group_33_project;

public class Student extends Account{
    private String program;

    Student(String firstName, String lastName, String email, String password,  String phone, String program) {
        super(firstName, lastName, email, password, phone);
        this.program = program;
    }

    //setters
    public void setProgram(String s){
        program = s;
    }

    //getters
    public String getProgram(){
        return program;
    }





}
