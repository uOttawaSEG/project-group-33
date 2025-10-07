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

public class RegisterStu extends AppCompatActivity {

    private EditText fname, lname, email, password, phone, program;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.screen4_registerstu);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.registerstuscreen), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // link XML → Java
        fname = findViewById(R.id.screen4_fname);
        lname = findViewById(R.id.screen4_lname);
        email = findViewById(R.id.screen4_email);
        password = findViewById(R.id.screen4_password);
        phone = findViewById(R.id.screen4_phone);
        program = findViewById(R.id.screen4_program);

        Button back = findViewById(R.id.screen4_back);
        Button register = findViewById(R.id.screen4_register);

        // back button → return to RegisterAs
        back.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterStu.this, RegisterAs.class);
            startActivity(intent);
            finish();
        });

        // register button → save student
        register.setOnClickListener(v -> {
            String f = fname.getText().toString().trim();
            String l = lname.getText().toString().trim();
            String e = email.getText().toString().trim();
            String p = password.getText().toString().trim();
            String ph = phone.getText().toString().trim();
            String pr = program.getText().toString().trim();

            if (f.isEmpty() || l.isEmpty() || e.isEmpty() || p.isEmpty() || ph.isEmpty() || pr.isEmpty()) {
                Toast.makeText(RegisterStu.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // create Student object
            Student student = new Student(f, l, e, p, ph, pr);

            // save to Firestore via AccountHandling
            AccountHandling accHandle = new AccountHandling();
            accHandle.studentSignUp(student, new AccountCallback() {
                @Override
                public void onSuccess(String msg) {
                    Toast.makeText(RegisterStu.this, msg, Toast.LENGTH_SHORT).show();
                    // go back to login
                    Intent intent = new Intent(RegisterStu.this, LoggedIn.class);
                    intent.putExtra("role", "Student");
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onFailure(String msg) {
                    Toast.makeText(RegisterStu.this, msg, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
