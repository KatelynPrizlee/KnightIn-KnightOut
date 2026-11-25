package com.example.knightinknightout.utils;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;

import com.example.knightinknightout.MainActivity;
import com.example.knightinknightout.R;
import com.example.knightinknightout.activities.FollowingActivity;
import com.example.knightinknightout.activities.ProfileActivity;
import com.example.knightinknightout.activities.SavedEventsActivity;

/**
 * Shared helper for binding bottom navigation tabs consistently across top-level screens.
 */
public final class BottomNavHelper {

    private static final String CREATE_EVENT_ACTIVITY_CLASS =
            "com.example.knightinknightout.activities.CreateEventActivity";

    private BottomNavHelper() {
    }

    public static void setup(Activity activity, int activeNavId) {
        ImageView navHome = activity.findViewById(R.id.navHome);
        ImageView navFollowing = activity.findViewById(R.id.navFollowing);
        ImageView navCreate = activity.findViewById(R.id.navCreate);
        ImageView navSaved = activity.findViewById(R.id.navSaved);
        ImageView navProfile = activity.findViewById(R.id.navProfile);

        if (navHome == null || navFollowing == null || navCreate == null
                || navSaved == null || navProfile == null) {
            return;
        }

        int activeColor = ContextCompat.getColor(activity, R.color.knight_yellow);
        int inactiveColor = ContextCompat.getColor(activity, R.color.app_white);

        ImageViewCompat.setImageTintList(navHome,
                ColorStateList.valueOf(activeNavId == R.id.navHome ? activeColor : inactiveColor));
        ImageViewCompat.setImageTintList(navFollowing,
                ColorStateList.valueOf(activeNavId == R.id.navFollowing ? activeColor : inactiveColor));
        ImageViewCompat.setImageTintList(navCreate, ColorStateList.valueOf(activeColor));
        ImageViewCompat.setImageTintList(navSaved,
                ColorStateList.valueOf(activeNavId == R.id.navSaved ? activeColor : inactiveColor));
        ImageViewCompat.setImageTintList(navProfile,
                ColorStateList.valueOf(activeNavId == R.id.navProfile ? activeColor : inactiveColor));

        navHome.setOnClickListener(v -> navigateTo(activity, activeNavId, R.id.navHome, MainActivity.class));
        navFollowing.setOnClickListener(v -> navigateTo(activity, activeNavId, R.id.navFollowing, FollowingActivity.class));
        navSaved.setOnClickListener(v -> navigateTo(activity, activeNavId, R.id.navSaved, SavedEventsActivity.class));
        navProfile.setOnClickListener(v -> navigateTo(activity, activeNavId, R.id.navProfile, ProfileActivity.class));

        navCreate.setOnClickListener(v -> {
            if (activeNavId == R.id.navCreate) {
                return;
            }
            try {
                Class<?> createClass = Class.forName(CREATE_EVENT_ACTIVITY_CLASS);
                activity.startActivity(new Intent(activity, createClass));
            } catch (ClassNotFoundException e) {
                Toast.makeText(activity,
                        "Create Event will be available once the Event System is merged.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static void navigateTo(Activity currentActivity,
                                   int activeNavId,
                                   int targetNavId,
                                   Class<? extends Activity> targetClass) {
        if (activeNavId == targetNavId) {
            return;
        }
        Intent intent = new Intent(currentActivity, targetClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        currentActivity.startActivity(intent);
        if (!(currentActivity instanceof MainActivity)) {
            currentActivity.finish();
        }
    }
}
