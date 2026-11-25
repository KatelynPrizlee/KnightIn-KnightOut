package com.example.knightinknightout.repositories;

import com.example.knightinknightout.models.User;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

/**
 * Centralizes Firebase Authentication and Firestore {@code users} collection operations.
 */
public class UserRepository {

    private static final String COLLECTION_USERS = "users";

    private final FirebaseAuth auth;
    private final CollectionReference users;

    public UserRepository() {
        this.auth = FirebaseAuth.getInstance();
        this.users = FirebaseFirestore.getInstance().collection(COLLECTION_USERS);
    }

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    public String getCurrentUid() {
        FirebaseUser currentUser = auth.getCurrentUser();
        return currentUser != null ? currentUser.getUid() : null;
    }

    public boolean isSignedIn() {
        return auth.getCurrentUser() != null;
    }

    /**
     * Signs in an existing user with email and password.
     */
    public Task<AuthResult> login(String email, String password) {
        String normalizedEmail = email != null ? email.trim() : "";
        return auth.signInWithEmailAndPassword(normalizedEmail, password != null ? password : "");
    }

    /**
     * Creates a Firebase Auth account and a matching Firestore profile under {@code users/{uid}}.
     * If creating the Firestore profile fails, the newly created Auth user is deleted to avoid orphaned accounts.
     */
    public Task<User> register(String firstName,
                               String lastName,
                               String username,
                               String email,
                               String password) {
        String cleanFirst = firstName != null ? firstName.trim() : "";
        String cleanLast = lastName != null ? lastName.trim() : "";
        String cleanUsername = username != null ? username.trim() : "";
        String cleanEmail = email != null ? email.trim() : "";

        return auth.createUserWithEmailAndPassword(cleanEmail, password != null ? password : "")
                .continueWithTask(authTask -> {
                    if (!authTask.isSuccessful()) {
                        Exception e = authTask.getException();
                        return Tasks.forException(e != null ? e : new IllegalStateException("Registration failed."));
                    }

                    FirebaseUser firebaseUser = authTask.getResult() != null
                            ? authTask.getResult().getUser()
                            : null;
                    if (firebaseUser == null) {
                        return Tasks.forException(new IllegalStateException("Authenticated user was null."));
                    }

                    String uid = firebaseUser.getUid();
                    User newUser = new User(
                            uid,
                            cleanFirst,
                            cleanLast,
                            cleanUsername,
                            cleanEmail,
                            Timestamp.now()
                    );

                    return users.document(uid)
                            .set(newUser)
                            .continueWithTask(saveTask -> {
                                if (saveTask.isSuccessful()) {
                                    return Tasks.forResult(newUser);
                                }
                                // Roll back Auth user if Firestore profile creation fails so the user can retry cleanly.
                                Exception saveException = saveTask.getException();
                                return firebaseUser.delete().continueWithTask(deleteTask ->
                                        Tasks.forException(saveException != null
                                                ? saveException
                                                : new IllegalStateException("Failed to create user profile.")));
                            });
                });
    }

    /**
     * Signs out the currently authenticated user.
     */
    public void logout() {
        auth.signOut();
    }

    /**
     * Sends a password reset email to the specified address.
     */
    public Task<Void> sendPasswordResetEmail(String email) {
        String normalizedEmail = email != null ? email.trim() : "";
        return auth.sendPasswordResetEmail(normalizedEmail);
    }

    /**
     * Sends a verification email to the currently signed-in user.
     */
    public Task<Void> sendEmailVerification() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            return Tasks.forException(new IllegalStateException("No authenticated user."));
        }
        return currentUser.sendEmailVerification();
    }

    /**
     * Fetches a user profile document by UID from Firestore.
     */
    public Task<User> getUserProfile(String uid) {
        if (uid == null || uid.trim().isEmpty()) {
            return Tasks.forException(new IllegalArgumentException("User ID is required."));
        }
        return users.document(uid).get().continueWith(task -> {
            if (!task.isSuccessful()) {
                Exception e = task.getException();
                throw e != null ? e : new IllegalStateException("Failed to fetch user profile.");
            }
            DocumentSnapshot snapshot = task.getResult();
            if (snapshot == null || !snapshot.exists()) {
                return null;
            }
            return snapshot.toObject(User.class);
        });
    }

    /**
     * Fetches the profile of the currently signed-in user.
     */
    public Task<User> getCurrentUserProfile() {
        String uid = getCurrentUid();
        if (uid == null) {
            return Tasks.forException(new IllegalStateException("No authenticated user."));
        }
        return getUserProfile(uid);
    }

    /**
     * Updates specific fields on the user's Firestore profile document.
     */
    public Task<Void> updateUserProfile(String uid, Map<String, Object> updates) {
        if (uid == null || uid.trim().isEmpty()) {
            return Tasks.forException(new IllegalArgumentException("User ID is required."));
        }
        return users.document(uid).update(updates);
    }
}
