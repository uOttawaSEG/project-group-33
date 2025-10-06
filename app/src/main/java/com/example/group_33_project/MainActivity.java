package com.example.group_33_project;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;



public class MainActivity extends AppCompatActivity {




    private EditText titleEmail, titlePassword;
    private Button signIn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.screen1_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });



        titleEmail = findViewById(R.id.screen1_email); // to access the titleEmail
        titlePassword = findViewById(R.id.screen1_password); // to access the titlePassword

        // 'listening' for a button click for signIn button
        signIn.setOnClickListener(v -> {

            // call the logIn method (email, password) from Account_Handling class
            AccountHandling accHandle = new AccountHandling();
            accHandle.logIn(titleEmail.getText().toString(), titlePassword.getText().toString(), new AccountCallback() { // define the callback methods for this case
                public void onSuccess(String msg) {
                    // print message (msg = "Successfully logged in")
                    // MOVE TO WELCOME PAGE
                }
                public void onFailure(String msg) {
                    // print error message (msg = either "email not found" or "password doesn't match")
                    // STAY ON SIGN IN PAGE (so user can retry/press sign up button)
                }
            });

        });

        // EVERYTHING FROM HERE AND BELOW WILL NEED TO BE ON SPECIFIC PAGES FOR SIGNUP AND SIGNIN!! This is ONLY THE LOGIC, please move it to a new file if you need! Make sure to link the fields (name/email/etc...) to the text boxes!
        // code for STUDENTS TO SIGN UP, implement with interface later:
        String firstName = "",  lastName = "",  email = "",  password = "",   phone = "",  program= ""; // link to text boxes!!
        AccountHandling accHandle = new AccountHandling(); // make an instance of account handling so that we can use sign up/in methods
        accHandle.studentSignUp(firstName, lastName, email, password,  phone, program, new AccountCallback(){ // call the signUp method, but also define the callbacks to display the results!
            public void onSuccess(String msg){
                // DISPLAY SUCCESS MESSAGE (msg) !!!
                // MOVE TO SIGN IN PAGE (so user can SIGN IN)
            }
            public void onFailure(String msg){
                // DISPLAY FAILURE MESSAGE (msg)
                // STAY ON sign up page

            }
        });

        // code for TUTORS TO SIGN UP, implement with interface later:
        // String firstName = "",  lastName = "",  email = "",  password = "",   phone = "",
        String education = ""; // link to text boxes!!
        String[] courses = new String[1];
        //AccountHandling accHandle = new AccountHandling(); // make an instance of account handling so that we can use sign up/in methods
        accHandle.tutorSignUp(firstName, lastName, email, password,  phone, education, courses, new AccountCallback(){ // call the signUp method, but also define the callbacks to display the results!
            public void onSuccess(String msg){
                // DISPLAY SUCCESS MESSAGE (msg) !!!
                // MOVE TO SIGN IN PAGE (so user can SIGN IN)
            }
            public void onFailure(String msg){
                // DISPLAY FAILURE MESSAGE (msg)
                // STAY ON sign up page

            }
        });

    }
}