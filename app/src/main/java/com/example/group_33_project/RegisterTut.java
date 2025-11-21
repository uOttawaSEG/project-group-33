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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RegisterTut extends AppCompatActivity {

    private EditText fname, lname, email, password, phone, degree, courses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.screen5_registertut);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.registertutscreen), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Finds textboxes for the information of tutor
        fname = findViewById(R.id.screen5_fname);
        lname = findViewById(R.id.screen5_lname);
        email = findViewById(R.id.screen5_email);
        password = findViewById(R.id.screen5_password);
        phone = findViewById(R.id.screen5_phone);
        degree = findViewById(R.id.screen5_degree);
        courses = findViewById(R.id.screen5_courses);

        Button back = findViewById(R.id.button_back);
        Button register = findViewById(R.id.screen5_register);

        // Back button
        back.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterTut.this, RegisterAs.class);
            startActivity(intent);
            finish();
        });

        // REGISTER button finds all the
        register.setOnClickListener(v -> {
            String f = fname.getText().toString().trim();
            String l = lname.getText().toString().trim();
            String e = email.getText().toString().trim();
            String p = password.getText().toString().trim();
            String ph = phone.getText().toString().trim();
            String d = degree.getText().toString().trim();
            String c = courses.getText().toString().trim();

            //Checks fields are empty (Fields cannot be empty)
            if (f.isEmpty() || l.isEmpty() || e.isEmpty() || p.isEmpty() || ph.isEmpty() || d.isEmpty() || c.isEmpty()) {
                Toast.makeText(RegisterTut.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            //email validation
            if (!e.contains("@") || !e.contains(".")) {
                Toast.makeText(RegisterTut.this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
                return;
            }
            String[] parts = e.split("@"); //make sure there are characters before and after the @ symbol and the . symbol
            if (parts.length != 2 || parts[0].isEmpty() || parts[1].isEmpty() || parts[1].lastIndexOf(".") == parts[1].length() -1 || parts[1].lastIndexOf(".") == 0){
                Toast.makeText(RegisterTut.this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
                return;
            }


            //phone validation
            String formattedPhone = ph.replaceAll("[\\s\\-]", ""); //removes dashes and spaces
            if (!formattedPhone.matches("\\d+")) { //if not all digits then toast message
                Toast.makeText(RegisterTut.this, "Please enter a valid phone number", Toast.LENGTH_SHORT).show();
                return;
            }

            // Courses logic since it could be list of size 1 or size n
            List<String> courseList;

            if (c.contains(",")) {
                // Split into multiple courses
                courseList = Arrays.asList(c.split("\\s*,\\s*"));
            } else {
                // Single course
                courseList = Collections.singletonList(c);
            }

            // Create Tutor object
            Tutor tutor = new Tutor(f, l, e, p, ph, d, courseList);

            // Save with AccountHandling
            //Attempts to add tutor account to the database collection
            //Uses Accounthandling to add tutor
            AccountHandling accHandle = new AccountHandling();
            accHandle.signUp(tutor, new AccountCallback() {
                //Displays the results if successful
                @Override
                public void onSuccess(String msg) {
                    Toast.makeText(RegisterTut.this, msg, Toast.LENGTH_SHORT).show();
                    // back to login screen
                    Intent intent = new Intent(RegisterTut.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onFailure(String msg) {
                    Toast.makeText(RegisterTut.this, msg, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
