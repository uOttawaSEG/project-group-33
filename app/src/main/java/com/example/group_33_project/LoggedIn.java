package com.example.group_33_project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LoggedIn extends AppCompatActivity {
    //LoggedIn Screen after either
    //SCREEN2

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.screen2_inside);  // keep your screen2.xml layout

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.loggedscreen), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Grab the account object passed from MainActivity! (possible since Account implements Serializable)
        Account account = (Account) getIntent().getSerializableExtra("account"); // Cast the serializable intent back to Account type

        // Locates textboxes
        TextView welcomeText = findViewById(R.id.screen2_welcome);
        TextView bodyText = findViewById(R.id.screen2_body);

        welcomeText.setText("Welcome!");

        //ROLES are used to show what role you are when logged in
        //Concatenate role string for proper formatting depending on grammar (a vs an)
        String role = " ";
        if (account.getClass() == Administrator.class){
            role = "n ";
        }
        role += account.getType();

        bodyText.setText("You are logged in as a" + role); // final format: You are logged in as a"n Admin" or " Student/Tutor"


        //Logout or Back button implementation
        Button logout = findViewById(R.id.screen2_logout);

        logout.setOnClickListener(v -> {
            Intent intent = new Intent(LoggedIn.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
