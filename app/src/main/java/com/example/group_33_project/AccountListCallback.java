package com.example.group_33_project;

import java.util.List;
// callback interface for the retrieval of student and tutor's accounts when obtaining a list of all the timeslots (in TutorHandling)
public interface AccountListCallback {
    void onSuccess(List<Object> accounts);
    void onFailure(String error);
}