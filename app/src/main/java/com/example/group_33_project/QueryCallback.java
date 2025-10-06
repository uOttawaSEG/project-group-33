package com.example.group_33_project;

public interface QueryCallback { // making a callback that will allow us to handle errors during QUERYING EMAILS(i.e. onSuccess means email FOUND, else there was no email associated)
        void onSuccess(Account account);
        void onFailure(String errorMessage);
}
