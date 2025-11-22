package com.example.group_33_project;

public interface TutorRatingCallback {
    void onSuccess(double avg);
    void onFailure(String errorMessage);
}

