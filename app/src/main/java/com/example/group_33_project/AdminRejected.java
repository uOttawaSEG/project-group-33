// ADMIN UTILITY: USED TO VIEW AND OPTIONALLY APPROVE DENIED ACCOUNTS
package com.example.group_33_project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AdminRejected extends AppCompatActivity implements RejectedAccountAdapter.OnAccountActionListener{

    AccountHandling accHandle = new AccountHandling();
    List<Account> deniedAccounts = new ArrayList<>();

    private RejectedAccountAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen8_adminin_rejected);
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.admininrejected), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //FINDING THE ID OF BUTTONS
        Button Back = findViewById(R.id.screen8_back);

        //make the account adapter
        //takes each account and transforms it into a UI element
        RecyclerView recyclerView = findViewById(R.id.rejectedAccountsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //add all the UI elements to the scroll list
        //empty for now
        adapter = new RejectedAccountAdapter(deniedAccounts, this);
        recyclerView.setAdapter(adapter);

        //GOES BACK TO ADMIN SCREEN
        Back.setOnClickListener(v -> {
            Intent intent = new Intent(AdminRejected.this, AdminIn.class);
            startActivity(intent);
            finish();
        });


        // RETRIEVING ALL DENIED ACCOUNTS
        // initialize array list to store pending accounts
        accHandle.getAccounts("denied", new PendingCallback() { // gets the pending accounts from account handling
            @Override
            public void onSuccess(List<Account> accounts) {
                deniedAccounts.clear(); // make sure it's empty first
                deniedAccounts.addAll(accounts); // add ALL of the accounts into the list of pending accounts
                // update UI
                //add all the accounts now
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(String msg) {
                // msg = "No pending accounts" -> let me (colin) know if you would rather an empty array to be passed instead!
                // show error
            }
        });
        //Account[] denied = deniedAccounts.toArray(new Account[0]);

    }

    //approves the account
    @Override
    public void onApprove(Account acc) {
        if (deniedAccounts.isEmpty()) {
            Toast.makeText(this, "No accounts to approve", Toast.LENGTH_SHORT).show();
            return;
        }
        accHandle.approve(acc); // update the status in database to approved
        removeFromDenied(acc); // remove the account from the pending list
        adapter.notifyDataSetChanged(); // remove the account from the scroll list UI
        Toast.makeText(this, acc.getEmail() + " approved.", Toast.LENGTH_SHORT).show();
    }



    //internal method for removal from the pending list
    private void removeFromDenied(Account acc) {
        for (Iterator<Account> it = deniedAccounts.iterator(); it.hasNext(); ) {
            Account a = it.next();
            if (a.getEmail().equalsIgnoreCase(acc.getEmail())) {
                it.remove();
                break;
            }
        }
    }
}
