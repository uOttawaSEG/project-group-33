// ADMIN UTILITY: USED TO VIEW AND OPTIONALLY APPROVE DENIED ACCOUNTS
package com.example.group_33_project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class AdminRejected extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen8_adminin_rejected);

        //FINDING THE ID OF BUTTONS
        Button Back = findViewById(R.id.screen8_back);

        //GOES BACK TO ADMININ SCREEN
        Back.setOnClickListener(v -> {
            Intent intent = new Intent(AdminRejected.this, AdminIn.class);
            startActivity(intent);
            finish();
        });


        // RETRIEVING ALL DENIED ACCOUNTS
        AccountHandling accHandle = new AccountHandling();
        List<Account> deniedAccounts = new ArrayList<>(); // initialize array list to store pending accounts
        accHandle.getAccounts("denied", new PendingCallback() { // gets the pending accounts from account handling
            @Override
            public void onSuccess(List<Account> accounts) {
                deniedAccounts.clear(); // make sure it's empty first
                deniedAccounts.addAll(accounts); // add ALL of the accounts into the list of pending accounts
                // update UI if needed
            }

            @Override
            public void onFailure(String msg) {
                // msg = "No pending accounts" -> let me (colin) know if you would rather an empty array to be passed instead!
                // show error
            }
        });
        Account[] denied = deniedAccounts.toArray(new Account[0]);
        //TODO: DISPLAY A LIST OF ALL OF THE DENIED ACCOUNTS: all denied accounts are stored in list "denied"
        // TODO: IMPLEMENT A BUTTON TO APPROVE THE ACCOUNTS (using method accHandle.approve(Account acc);)




    }
}
