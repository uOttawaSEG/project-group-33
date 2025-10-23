// ADMIN UTILITY: USED TO APPROVE OR DENY PENDING ACCOUNTGS
package com.example.group_33_project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class AdminRequest extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen7_adminin_request);

        //FINDING THE ID OF BUTTONS
        Button Back = findViewById(R.id.screen7_back);

        //GOES BACK TO ADMININ SCREEN
        Back.setOnClickListener(v -> {
            Intent intent = new Intent(AdminRequest.this, AdminIn.class);
            startActivity(intent);
            finish();
        });

        // RETRIEVING LIST OF PENDING ACCOUNTS
        AccountHandling accHandle = new AccountHandling();
        List<Account> pendingAccounts = new ArrayList<>(); // initialize array list to store pending accounts
        accHandle.getAccounts("pending", new PendingCallback() { // gets the pending accounts from account handling
            @Override
            public void onSuccess(List <Account> accounts) {
                pendingAccounts.clear(); // make sure it's empty first
                pendingAccounts.addAll(accounts); // add ALL of the accounts into the list of pending accounts
                // update UI if needed
            }

            @Override
            public void onFailure(String msg){
                // msg = "No pending accounts" -> let me (colin) know if you would rather an empty array to be passed instead!
                // show error
            }
        });// pendingAccounts is updated and can be accessed/used!

        Account[] pending = pendingAccounts.toArray(new Account[0]); // convert to an array

        //TODO: DISPLAY EACH ACCOUNT, CREATE AN APPROVE AND DENY BUTTON TO IMPLEMENT FOLLOWING CODE
        // note: it seems like RecyclerView is the easiest way to do this, it makes a scrolling list and allows you to make approve/deny buttons for each individual account
        // which are automatically mapped to the specific account? do more research on this...


        // ALL PENDING ACCOUNTS ARE STORED IN THE ARRAY 'pending'
        // TO DENY AN ACCOUNT; method accHandle.deny(Account acc) is implemented, and will update the database! REMEMBER TO REMOVE THE DENIED ACCOUNT FROM THE PENDING LIST!
        // TO APPROVE AN ACCOUNT; method  accHandle.approve(Account acc) is implemented -> REMEMBER TO REMOVE THE APPROVED ACC!




    }
}
