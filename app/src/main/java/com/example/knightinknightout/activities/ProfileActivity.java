package com.example.knightinknightout.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
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
import com.example.knightinknightout.utils.BottomNavHelper;

import java.util.List;

/**
 * Displays the authenticated user's profile information and provides access to editing, settings, and logout.
 */
public class ProfileActivity extends AppCompatActivity {

    private UserRepository userRepository;
    private ProgressBar progressBar;
    private TextView tvDisplayName;
    private TextView tvUsername;
    private TextView tvVerificationStatus;
    private TextView tvMajor;
    private TextView tvYear;
    private TextView tvInterests;
    private TextView tvSocialStyle;
    private TextView tvHostedCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userRepository = new UserRepository();
        if (!userRepository.isSignedIn()) {
            redirectToLogin();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        BottomNavHelper.setup(this, R.id.navProfile);

        progressBar = findViewById(R.id.progressBar);
        tvDisplayName = findViewById(R.id.tvDisplayName);
        tvUsername = findViewById(R.id.tvUsername);
        tvVerificationStatus = findViewById(R.id.tvVerificationStatus);
        tvMajor = findViewById(R.id.tvMajor);
        tvYear = findViewById(R.id.tvYear);
        tvInterests = findViewById(R.id.tvInterests);
        tvSocialStyle = findViewById(R.id.tvSocialStyle);
        tvHostedCount = findViewById(R.id.tvHostedCount);

        TextView btnBack = findViewById(R.id.btnBack);
        TextView btnSettings = findViewById(R.id.btnSettings);
        Button btnEditProfile = findViewById(R.id.btnEditProfile);
        Button btnLogout = findViewById(R.id.btnLogout);

        btnBack.setOnClickListener(v -> finish());
        btnSettings.setOnClickListener(v ->
                startActivity(new Intent(ProfileActivity.this, SettingsActivity.class)));
        btnEditProfile.setOnClickListener(v ->
                startActivity(new Intent(ProfileActivity.this, EditProfileActivity.class)));
        btnLogout.setOnClickListener(v -> {
            userRepository.logout();
            redirectToLogin();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!userRepository.isSignedIn()) {
            redirectToLogin();
            return;
        }
        loadUserProfile();
    }

    private void loadUserProfile() {
        progressBar.setVisibility(View.VISIBLE);
        userRepository.getCurrentUserProfile()
                .addOnSuccessListener(user -> {
                    progressBar.setVisibility(View.GONE);
                    if (user != null) {
                        bindUser(user);
                    } else {
                        tvDisplayName.setText("Profile Not Found");
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    String message = e.getLocalizedMessage() != null
                            ? e.getLocalizedMessage()
                            : "Failed to load profile.";
                    Toast.makeText(ProfileActivity.this, message, Toast.LENGTH_LONG).show();
                });
    }

    private void bindUser(User user) {
        String fullName = user.getFullName();
        tvDisplayName.setText(!fullName.isEmpty() ? fullName : "Knight");

        String username = user.getUsername();
        tvUsername.setText(username != null && !username.trim().isEmpty() ? "@" + username.trim() : "");

        tvVerificationStatus.setText(user.isUcfVerified() ? "UCF Verified ✔" : "Not UCF Verified");

        String major = user.getMajor();
        tvMajor.setText("Major: " + (major != null && !major.trim().isEmpty() ? major.trim() : "Not set"));

        String year = user.getYear();
        tvYear.setText("Year: " + (year != null && !year.trim().isEmpty() ? year.trim() : "Not set"));

        List<String> interests = user.getInterests();
        tvInterests.setText(interests != null && !interests.isEmpty()
                ? TextUtils.join(", ", interests)
                : "None added yet");

        String socialStyle = user.getSocialStyle();
        tvSocialStyle.setText(socialStyle != null && !socialStyle.trim().isEmpty()
                ? socialStyle.trim()
                : "Not set");

        int hosted = user.getHostedCount();
        tvHostedCount.setText("Hosted: " + hosted + (hosted == 1 ? " Event" : " Events"));
    }

    private void redirectToLogin() {
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
