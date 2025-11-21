package com.example.group_33_project;

import android.content.Intent;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class BaseActivity extends AppCompatActivity {

    protected void setupLogoutButton() {
        MaterialButton logoutButton = findViewById(R.id.button_logout);

        if (logoutButton != null) {
            logoutButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(BaseActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
        }
    }

}