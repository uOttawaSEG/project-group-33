// ADMIN UTILITY: USED TO APPROVE OR DENY PENDING ACCOUNTGS
package com.example.group_33_project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AdminRequest extends AppCompatActivity {

    private AccountHandling accHandle;
    private final List<Account> pendingAccounts = new ArrayList<>();

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
        accHandle = new AccountHandling();
        accHandle.getAccounts("pending", new PendingCallback() {
            @Override
            public void onSuccess(List<Account> accounts) {
                pendingAccounts.clear(); // make sure it's empty first
                pendingAccounts.addAll(accounts); // add ALL of the accounts into the list of pending accounts
                // update UI if needed
                Account[] pending = pendingAccounts.toArray(new Account[0]); // convert to an array
            }

            @Override
            public void onFailure(String msg){
                // msg = "No pending accounts" -> let me (colin) know if you would rather an empty array to be passed instead!
                // show error
            }
        });// pendingAccounts is updated and can be accessed/used!

        //TODO: DISPLAY EACH ACCOUNT, CREATE AN APPROVE AND DENY BUTTON TO IMPLEMENT FOLLOWING CODE
        // note: it seems like RecyclerView is the easiest way to do this, it makes a scrolling list and allows you to make approve/deny buttons for each individual account
        // which are automatically mapped to the specific account? do more research on this...

        // ALL PENDING ACCOUNTS ARE STORED IN THE ARRAY 'pending'
        // TO DENY AN ACCOUNT; method accHandle.deny(Account acc) is implemented, and will update the database! REMEMBER TO REMOVE THE DENIED ACCOUNT FROM THE PENDING LIST!
        // TO APPROVE AN ACCOUNT; method  accHandle.approve(Account acc) is implemented -> REMEMBER TO REMOVE THE APPROVED ACC!
    }

    //called from frontend when approved button is pressed
    public void approveAccount(Account acc){
        if (pendingAccounts.isEmpty()){
            Toast.makeText(this, "no pending accounts to approve/deny", Toast.LENGTH_SHORT).show();
            return;
        }
        accHandle.approve(acc); // update the status in database to approved
        removeFromPending(acc); // remove the account from the pending list
        Toast.makeText(this, acc.getEmail() + " approved.", Toast.LENGTH_SHORT).show();
    }

    //called from frontend when denied button is pressed
    public void denyAccount(Account acc){
        if (pendingAccounts.isEmpty()){
            Toast.makeText(this, "no pending accounts to approve/deny", Toast.LENGTH_SHORT).show();
            return;
        }
        accHandle.deny(acc); // update the status in database to denied
        removeFromPending(acc); // remove the account from the pending list
        Toast.makeText(this, acc.getEmail() + " denied.", Toast.LENGTH_SHORT).show();
    }

    private void removeFromPending(Account acc){
        for (Iterator<Account> it = pendingAccounts.iterator(); it.hasNext();) {
            Account a = it.next();
            if (a.getEmail().equalsIgnoreCase(acc.getEmail())) {
                it.remove();
                break;
            }
        }
    }
}
