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

import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private EditText titleEmail, titlePassword;
    private Button signUp;

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

        titleEmail = findViewById(R.id.screen1_email);
        titlePassword = findViewById(R.id.screen1_password);
        signUp = findViewById(R.id.screen1_signUp);

        signUp.setOnClickListener(v -> {
            String email = titleEmail.getText().toString().trim();
            String password = titlePassword.getText().toString().trim();

            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("approvedAccounts")
                    .whereEqualTo("email", email)   // ✅ query by email instead of ADMIN doc
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (!querySnapshot.isEmpty()) {
                            // For simplicity, get first match
                            var doc = querySnapshot.getDocuments().get(0);
                            String dbEmail = doc.getString("email");
                            String dbPassword = doc.getString("password");
                            String type = doc.getString("type"); // ADMIN / STUDENT / TUTOR

                            if (dbEmail != null && dbPassword != null &&
                                    dbEmail.equals(email) && dbPassword.equals(password)) {

                                if ("ADMIN".equalsIgnoreCase(type)) {
                                    // ✅ Go to Admin screen
                                    Intent intent = new Intent(MainActivity.this, screen2.class);
                                    intent.putExtra("role", "Administrator");
                                    startActivity(intent);
                                    finish();
                                } else if ("STUDENT".equalsIgnoreCase(type)) {
                                    // Example: Go to Student screen
                                    Intent intent = new Intent(MainActivity.this, screen2.class);
                                    intent.putExtra("role", "Student");
                                    startActivity(intent);
                                    finish();
                                } else if ("TUTOR".equalsIgnoreCase(type)) {
                                    // Example: Go to Tutor screen
                                    Intent intent = new Intent(MainActivity.this, screen2.class);
                                    intent.putExtra("role", "Tutor");
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(this, "Unknown account type", Toast.LENGTH_SHORT).show();
                                }

                            } else {
                                Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(this, "Account not found", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        });
    }
}
