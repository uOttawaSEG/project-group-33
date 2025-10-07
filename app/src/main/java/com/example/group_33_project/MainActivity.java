package com.example.group_33_project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private EditText loginEmail, loginPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.screen1_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainscreen), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loginEmail = findViewById(R.id.screen1_email);
        loginPassword = findViewById(R.id.screen1_password);
        Button signIn = findViewById(R.id.screen1_signIn);
        Button register = findViewById(R.id.screen1_register);

        // Sign in button
        signIn.setOnClickListener(v -> {
            String email = loginEmail.getText().toString().trim();
            String password = loginPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                return;
            }
            //Checks to see if the login is valid from the collection of approved users
            AccountHandling accHandle = new AccountHandling();
            accHandle.queryAccountByEmail(email, password, new AccountCallback() {
                @Override
                public void onSuccess(String msg) {
                    // Extract role from message
                    String role = msg.replace("Login successful: ", "").trim();

                    Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(MainActivity.this, LoggedIn.class);
                    intent.putExtra("role", role); // Pass Student/Tutor/Admin to LoggedIn screen
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onFailure(String msg) {
                    Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Register button to the next page
        register.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegisterAs.class);
            startActivity(intent);
            finish();
        });
    }
}
