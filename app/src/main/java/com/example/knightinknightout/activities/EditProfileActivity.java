package com.example.knightinknightout.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.knightinknightout.R;
import com.example.knightinknightout.models.User;
import com.example.knightinknightout.repositories.UserRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Allows the signed-in user to update their profile fields in Firestore.
 */
public class EditProfileActivity extends AppCompatActivity {

    private static final int MIN_USERNAME_LENGTH = 3;

    private UserRepository userRepository;
    private EditText etFirstName;
    private EditText etLastName;
    private EditText etUsername;
    private EditText etMajor;
    private EditText etYear;
    private EditText etInterests;
    private EditText etSocialStyle;
    private Button btnSaveProfile;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userRepository = new UserRepository();
        if (!userRepository.isSignedIn()) {
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etUsername = findViewById(R.id.etUsername);
        etMajor = findViewById(R.id.etMajor);
        etYear = findViewById(R.id.etYear);
        etInterests = findViewById(R.id.etInterests);
        etSocialStyle = findViewById(R.id.etSocialStyle);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        progressBar = findViewById(R.id.progressBar);

        TextView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
        btnSaveProfile.setOnClickListener(v -> saveChanges());

        loadCurrentProfile();
    }

    private void loadCurrentProfile() {
        setLoading(true);
        userRepository.getCurrentUserProfile()
                .addOnSuccessListener(user -> {
                    setLoading(false);
                    if (user != null) {
                        populateFields(user);
                    }
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(this,
                            e.getLocalizedMessage() != null ? e.getLocalizedMessage() : "Could not load profile.",
                            Toast.LENGTH_LONG).show();
                });
    }

    private void populateFields(User user) {
        etFirstName.setText(user.getFirstName() != null ? user.getFirstName() : "");
        etLastName.setText(user.getLastName() != null ? user.getLastName() : "");
        etUsername.setText(user.getUsername() != null ? user.getUsername() : "");
        etMajor.setText(user.getMajor() != null ? user.getMajor() : "");
        etYear.setText(user.getYear() != null ? user.getYear() : "");
        etSocialStyle.setText(user.getSocialStyle() != null ? user.getSocialStyle() : "");

        List<String> interests = user.getInterests();
        etInterests.setText(interests != null && !interests.isEmpty() ? TextUtils.join(", ", interests) : "");
    }

    private void saveChanges() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String major = etMajor.getText().toString().trim();
        String year = etYear.getText().toString().trim();
        String interestsRaw = etInterests.getText().toString();
        String socialStyle = etSocialStyle.getText().toString().trim();

        if (firstName.isEmpty()) {
            etFirstName.setError("First name is required");
            etFirstName.requestFocus();
            return;
        }
        if (lastName.isEmpty()) {
            etLastName.setError("Last name is required");
            etLastName.requestFocus();
            return;
        }
        if (username.length() < MIN_USERNAME_LENGTH || username.contains(" ")) {
            etUsername.setError("Username must be at least 3 characters with no spaces");
            etUsername.requestFocus();
            return;
        }

        String uid = userRepository.getCurrentUid();
        if (uid == null) {
            finish();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("firstName", firstName);
        updates.put("lastName", lastName);
        updates.put("username", username);
        updates.put("major", major);
        updates.put("year", year);
        updates.put("interests", parseInterests(interestsRaw));
        updates.put("socialStyle", socialStyle);

        setLoading(true);
        userRepository.updateUserProfile(uid, updates)
                .addOnSuccessListener(unused -> {
                    setLoading(false);
                    Toast.makeText(EditProfileActivity.this, "Profile updated!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(EditProfileActivity.this,
                            e.getLocalizedMessage() != null ? e.getLocalizedMessage() : "Failed to save profile.",
                            Toast.LENGTH_LONG).show();
                });
    }

    private List<String> parseInterests(String raw) {
        List<String> result = new ArrayList<>();
        if (raw == null || raw.trim().isEmpty()) {
            return result;
        }
        for (String part : raw.split(",")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return result;
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnSaveProfile.setEnabled(!loading);
    }
}
