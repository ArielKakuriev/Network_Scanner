package com.schoolcomputers.networkscanner.viewmodels;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.schoolcomputers.networkscanner.models.User;

import java.util.HashMap;
import java.util.Map;

/**
 * ViewModel for all auth screens.
 * Uses Firebase Authentication + Firestore. No SQLite/Room involved here.
 */

/** // Firebase: Main firestore read/write for login, register, user profiles // **/
public class AuthViewModel extends AndroidViewModel {

    private static final String TAG = "AuthViewModel";

    private final FirebaseAuth      auth;
    private final FirebaseFirestore db;

    private final MutableLiveData<FirebaseUser> currentUser    = new MutableLiveData<>();
    private final MutableLiveData<Boolean>      loading        = new MutableLiveData<>(false);
    private final MutableLiveData<String>       successMessage = new MutableLiveData<>();
    private final MutableLiveData<String>       errorMessage   = new MutableLiveData<>();

    public AuthViewModel(@NonNull Application application) {
        super(application);
        auth = FirebaseAuth.getInstance();
        db   = FirebaseFirestore.getInstance();
        currentUser.setValue(auth.getCurrentUser());
        db.collection("users").limit(1).get()
                .addOnSuccessListener(s -> Log.d("AuthViewModel", "Firestore OK, docs: " + s.size()))
                .addOnFailureListener(e -> Log.e("AuthViewModel", "Firestore FAILED: " + e.getMessage()));

    }

    public LiveData<FirebaseUser> getCurrentUser()   { return currentUser; }
    public LiveData<Boolean>      isLoading()         { return loading; }
    public LiveData<String>       getSuccessMessage() { return successMessage; }
    public LiveData<String>       getErrorMessage()   { return errorMessage; }

    public boolean isSignedIn() {
        return auth.getCurrentUser() != null;
    }

    // ── LOGIN ───────────────────────────────────────────────────────────────

    public void login(String usernameOrEmail, String password) {
        if (usernameOrEmail == null || usernameOrEmail.trim().isEmpty()) {
            errorMessage.setValue("Please enter your username or email.");
            return;
        }
        if (password == null || password.isEmpty()) {
            errorMessage.setValue("Please enter your password.");
            return;
        }
        loading.setValue(true);
        errorMessage.setValue(null); // Clear previous error

        String inputNormalized = usernameOrEmail.trim().toLowerCase();

        // If input looks like email (contains @), try email login directly
        if (inputNormalized.contains("@")) {
            signInWithEmail(inputNormalized, password);
        } else {
            // Otherwise look up username in Firestore
            db.collection("users")
                    .whereEqualTo("username", usernameOrEmail.trim())
                    .limit(1)
                    .get()
                    .addOnSuccessListener(snap -> {
                        if (!snap.isEmpty()) {
                            String email = snap.getDocuments().get(0).getString("email");
                            if (email != null) {
                                signInWithEmail(email, password);
                            } else {
                                loading.postValue(false);
                                errorMessage.postValue("Account data is incomplete. Contact support.");
                            }
                        } else {
                            loading.postValue(false);
                            errorMessage.postValue("No account found with that username.");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "login Firestore lookup failed", e);
                        loading.postValue(false);
                        errorMessage.postValue("Login error: " + e.getMessage());
                    });
        }
    }

    private void signInWithEmail(String email, String password) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    loading.postValue(false);
                    currentUser.postValue(result.getUser());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "signInWithEmail failed", e);
                    loading.postValue(false);
                    errorMessage.postValue("Incorrect email or password.");
                });
    }

    // ── REGISTER ────────────────────────────────────────────────────────────

    /**
     * Registration flow:
     * 1. Check Firestore that the username is not already taken.
     * 2. Check Firestore that the email is not already taken.
     * 3. Create the Firebase Auth account.
     * 4. Write the user profile to Firestore.
     */
    public void register(String username, String email, String password) {
        if (username == null || username.trim().isEmpty()) {
            errorMessage.setValue("Username is required.");
            return;
        }
        if (email == null || email.trim().isEmpty()) {
            errorMessage.setValue("Email is required.");
            return;
        }
        if (password == null || password.length() < 6) {
            errorMessage.setValue("Password must be at least 6 characters.");
            return;
        }
        loading.setValue(true);

        String normalizedEmail = email.trim().toLowerCase();

        db.collection("users")
                .whereEqualTo("username", username.trim())
                .limit(1)
                .get()
                .addOnSuccessListener(snap -> {
                    if (!snap.isEmpty()) {
                        loading.postValue(false);
                        errorMessage.postValue("That username is already taken.");
                        return;
                    }
                    checkEmailUniqueness(normalizedEmail, username.trim(), password);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "register: username check failed", e);
                    loading.postValue(false);
                    errorMessage.postValue("Registration error (username check): " + e.getMessage());
                });
    }

    private void checkEmailUniqueness(String normalizedEmail, String username, String password) {
        db.collection("users")
                .whereEqualTo("email", normalizedEmail)
                .limit(1)
                .get()
                .addOnSuccessListener(snap -> {
                    if (!snap.isEmpty()) {
                        loading.postValue(false);
                        errorMessage.postValue("That email is already registered.");
                        return;
                    }
                    createAuthAccount(username, normalizedEmail, password);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "register: email check failed", e);
                    loading.postValue(false);
                    errorMessage.postValue("Registration error (email check): " + e.getMessage());
                });
    }

    private void createAuthAccount(String username, String email, String password) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    FirebaseUser firebaseUser = result.getUser();
                    if (firebaseUser == null) {
                        loading.postValue(false);
                        errorMessage.postValue("Registration failed unexpectedly.");
                        return;
                    }
                    writeProfileToFirestore(firebaseUser, username, email);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "register: createUserWithEmailAndPassword failed", e);
                    loading.postValue(false);
                    errorMessage.postValue(e.getMessage());
                });
    }

    private void writeProfileToFirestore(FirebaseUser firebaseUser,
                                         String username, String email) {
        Map<String, Object> profile = new HashMap<>();
        profile.put("uid",      firebaseUser.getUid());
        profile.put("username", username);
        profile.put("email",    email.toLowerCase());

        db.collection("users")
                .document(firebaseUser.getUid())
                .set(profile)
                .addOnSuccessListener(v -> {
                    Log.d(TAG, "Firestore profile written for uid=" + firebaseUser.getUid());
                    loading.postValue(false);
                    successMessage.postValue("Account created! Welcome, " + username + ".");
                    currentUser.postValue(firebaseUser);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "register: Firestore profile write FAILED", e);
                    loading.postValue(false);
                    errorMessage.postValue("Profile save failed — " + e.getMessage()
                            + "\n\nCheck your Firestore security rules.");
                    currentUser.postValue(firebaseUser);
                });
    }

    // ── FORGOT PASSWORD ─────────────────────────────────────────────────────

    /**
     * username must be non-empty. If called from Settings (where we only have
     * the email), pass the email for both parameters — we skip the username
     * check in that case.
     */
    public void forgotPassword(String username, String email) {
        if (email == null || email.trim().isEmpty()) {
            errorMessage.setValue("Please enter your email.");
            return;
        }
        loading.setValue(true);

        String normalizedEmail = email.trim().toLowerCase();

        if (username == null || username.trim().isEmpty()) {
            sendResetEmail(normalizedEmail);
            return;
        }

        db.collection("users")
                .whereEqualTo("username", username.trim())
                .whereEqualTo("email", normalizedEmail)
                .limit(1)
                .get()
                .addOnSuccessListener(snap -> {
                    if (!snap.isEmpty()) {
                        sendResetEmail(normalizedEmail);
                    } else {
                        loading.postValue(false);
                        errorMessage.postValue("No account found with that username and email.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "forgotPassword: Firestore lookup failed", e);
                    loading.postValue(false);
                    errorMessage.postValue("Error: " + e.getMessage());
                });
    }

    private void sendResetEmail(String email) {
        auth.sendPasswordResetEmail(email)
                .addOnSuccessListener(v -> {
                    loading.postValue(false);
                    successMessage.postValue("Password reset email sent to " + email + ". Check your inbox.");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "sendPasswordResetEmail failed", e);
                    loading.postValue(false);
                    errorMessage.postValue("Could not send reset email: " + e.getMessage());
                });
    }

    // ── USER PROFILE ────────────────────────────────────────────────────────

    public LiveData<User> getUserProfile() {
        MutableLiveData<User> result = new MutableLiveData<>();
        FirebaseUser fu = auth.getCurrentUser();
        if (fu == null) { result.setValue(null); return result; }

        db.collection("users").document(fu.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        result.postValue(new User(
                                doc.getString("uid"),
                                doc.getString("username"),
                                doc.getString("email")
                        ));
                    } else {
                        Log.w(TAG, "getUserProfile: no Firestore document for uid=" + fu.getUid());
                        result.postValue(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "getUserProfile failed", e);
                    result.postValue(null);
                });
        return result;
    }

    // ── SIGN OUT ─────────────────────────────────────────────────────────────

    public void signOut() {
        auth.signOut();
        currentUser.setValue(null);
        errorMessage.setValue(null);
        successMessage.setValue(null);
    }
}
