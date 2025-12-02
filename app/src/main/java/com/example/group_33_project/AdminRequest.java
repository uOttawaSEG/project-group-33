// ADMIN UTILITY: USED TO APPROVE OR DENY PENDING ACCOUNTS
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

public class AdminRequest extends AppCompatActivity implements RequestAccountAdapter.OnAccountActionListener {

    private AccountHandling accHandle;
    private final List<Account> pendingAccounts = new ArrayList<>();
    private RequestAccountAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen7_adminin_request);
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.admininrequestscreen), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //FINDING THE ID OF BUTTONS
        Button Back = findViewById(R.id.button_back);

        //create the scrolling list
        RecyclerView recyclerView = findViewById(R.id.pendingAccountsRecyclerView);
        //make the account adapter
        //takes each account and transforms it into a UI element
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RequestAccountAdapter(pendingAccounts, (RequestAccountAdapter.OnAccountActionListener) this);
        //add all the UI elements to the scroll list
        //empty for now
        recyclerView.setAdapter(adapter);

        //GOES BACK TO ADMIN SCREEN
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
                // update UI
                //add all the accounts now
                adapter.notifyDataSetChanged();
                //no need for an array
                //Account[] pending = pendingAccounts.toArray(new Account[0]); // convert to an array
            }

            @Override
            public void onFailure(String msg){
                // msg = "No pending accounts" -> let me (colin) know if you would rather an empty array to be passed instead!
                // show error

            }
        });// pendingAccounts is updated and can be accessed/used!

    }

    //called from frontend when approved button is pressed
    @Override
    public void onApprove(Account acc){
        if (pendingAccounts.isEmpty()){
            Toast.makeText(this, "no pending accounts to approve/deny", Toast.LENGTH_SHORT).show();
            return;
        }
        accHandle.approve(acc); // update the status in database to approved
        removeFromPending(acc); // remove the account from the pending list
        adapter.notifyDataSetChanged(); // remove the account from the scroll list UI
        Toast.makeText(this, acc.getEmail() + " approved.", Toast.LENGTH_SHORT).show();
    }

    //called from frontend when denied button is pressed
    @Override
    public void onDelete(Account acc){
        if (pendingAccounts.isEmpty()){
            Toast.makeText(this, "no pending accounts to approve/deny", Toast.LENGTH_SHORT).show();
            return;
        }
        accHandle.deny(acc); // update the status in database to denied
        removeFromPending(acc); // remove the account from the pending list
        adapter.notifyDataSetChanged(); // remove the account from the scroll list
        Toast.makeText(this, acc.getEmail() + " denied.", Toast.LENGTH_SHORT).show();
    }

    //internal method that removes the account from pending
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
