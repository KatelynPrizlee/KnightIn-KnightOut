package com.example.knightinknightout.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.knightinknightout.MainActivity;
import com.example.knightinknightout.R;
import com.example.knightinknightout.repositories.UserRepository;

/**
 * Handles user sign-in, password reset requests, and navigation to account registration.
 */
public class LoginActivity extends AppCompatActivity {

    private UserRepository userRepository;
    private EditText etEmail;
    private EditText etPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private TextView tvForgotPassword;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userRepository = new UserRepository();
        if (userRepository.isSignedIn()) {
            navigateToHome();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        progressBar = findViewById(R.id.progressBar);

        btnLogin.setOnClickListener(v -> attemptLogin());
        tvForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());
        tvRegister.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));
    }

    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email address");
            etEmail.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        setLoading(true);
        userRepository.login(email, password)
                .addOnSuccessListener(authResult -> {
                    setLoading(false);
                    navigateToHome();
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    String message = e.getLocalizedMessage() != null
                            ? e.getLocalizedMessage()
                            : "Unable to sign in. Please check your credentials.";
                    Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
                });
    }

    private void showForgotPasswordDialog() {
        EditText resetEmailInput = new EditText(this);
        resetEmailInput.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        resetEmailInput.setHint("Email address");
        String currentEmail = etEmail.getText().toString().trim();
        if (!currentEmail.isEmpty()) {
            resetEmailInput.setText(currentEmail);
        }

        new AlertDialog.Builder(this)
                .setTitle("Reset Password")
                .setMessage("Enter the email address for your account.")
                .setView(resetEmailInput)
                .setPositiveButton("Send Reset Email", (dialog, which) -> {
                    String resetEmail = resetEmailInput.getText().toString().trim();
                    if (resetEmail.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(resetEmail).matches()) {
                        Toast.makeText(this, "Please enter a valid email address.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    userRepository.sendPasswordResetEmail(resetEmail)
                            .addOnSuccessListener(unused ->
                                    Toast.makeText(this, "Password reset email sent.", Toast.LENGTH_LONG).show())
                            .addOnFailureListener(e ->
                                    Toast.makeText(this,
                                            e.getLocalizedMessage() != null ? e.getLocalizedMessage() : "Could not send reset email.",
                                            Toast.LENGTH_LONG).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!loading);
        tvRegister.setEnabled(!loading);
        tvForgotPassword.setEnabled(!loading);
    }

    private void navigateToHome() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
