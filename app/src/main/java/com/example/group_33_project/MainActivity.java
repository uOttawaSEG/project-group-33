package com.example.group_33_project;

import static com.example.group_33_project.R.*;

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

import com.google.firebase.firestore.FirebaseFirestore;

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
        Button signIn = findViewById(id.screen1_signIn);

        // 'listening' for a button click for signIn button
        signIn.setOnClickListener(v -> {
            String email = loginEmail.getText().toString().trim();
            String password = loginPassword.getText().toString().trim();

            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("approvedAccounts")
                    .document("ADMIN") // only using ADMIN doc for now
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String dbEmail = documentSnapshot.getString("email");
                            String dbPassword = documentSnapshot.getString("password");

                            if (email.equals(dbEmail) && password.equals(dbPassword)) {
                                // login success -> move to screen2
                                Intent intent = new Intent(MainActivity.this, LoggedIn.class);
                                intent.putExtra("role", "Admin");
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(MainActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "Account not found", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        });

        Button register = findViewById(R.id.screen1_register);

        register.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, RegisterAs.class);
                startActivity(intent);
                finish();
        });

        // EVERYTHING FROM HERE AND BELOW WILL NEED TO BE ON SPECIFIC PAGES FOR SIGNUP AND SIGNIN!!
        // This is ONLY THE LOGIC, please move it to a new file if you need!
        // Make sure to link the fields (name/email/etc...) to the text boxes!

        // code for STUDENTS TO SIGN UP
//        String firstName = "",  lastName = "",  email = "",  password = "",   phone = "",  program= ""; // link to text boxes!!
//        AccountHandling accHandle = new AccountHandling();
//        accHandle.studentSignUp(firstName, lastName, email, password,  phone, program, new AccountCallback(){
//            public void onSuccess(String msg){
//                // DISPLAY SUCCESS MESSAGE (msg) !!!
//                // MOVE TO SIGN IN PAGE (so user can SIGN IN)
//            }
//            public void onFailure(String msg){
//                // DISPLAY FAILURE MESSAGE (msg)
//                // STAY ON sign up page
//            }
//        });
//
//        // code for TUTORS TO SIGN UP
//        String education = ""; // link to text boxes!!
//        String[] courses = new String[1];
//        accHandle.tutorSignUp(firstName, lastName, email, password,  phone, education, courses, new AccountCallback(){
//            public void onSuccess(String msg){
//                // DISPLAY SUCCESS MESSAGE (msg) !!!
//                // MOVE TO SIGN IN PAGE (so user can SIGN IN)
//            }
//            public void onFailure(String msg){
//                // DISPLAY FAILURE MESSAGE (msg)
//                // STAY ON sign up page
//            }
//        });


    }
}
