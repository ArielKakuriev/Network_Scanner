package com.networkscanner.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.networkscanner.models.User;

import java.util.HashMap;
import java.util.Map;

/**
 * ViewModel backing the auth screens (Login, Register, ForgotPassword).
 * All Firebase calls are made here; the Fragments observe LiveData.
 */
public class AuthViewModel extends AndroidViewModel {

    private final FirebaseAuth auth;
    private final FirebaseFirestore db;

    /** Emits the currently signed-in user or null. */
    private final MutableLiveData<FirebaseUser> currentUser = new MutableLiveData<>();

    /** Emits loading state (true = busy). */
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);

    /** Emits success messages. */
    private final MutableLiveData<String> successMessage = new MutableLiveData<>();

    /** Emits error messages. */
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public AuthViewModel(@NonNull Application application) {
        super(application);
        auth = FirebaseAuth.getInstance();
        db   = FirebaseFirestore.getInstance();
        // Push current auth state
        currentUser.setValue(auth.getCurrentUser());
    }

    // ---- LiveData accessors ----

    public LiveData<FirebaseUser> getCurrentUser()  { return currentUser; }
    public LiveData<Boolean>      isLoading()        { return loading; }
    public LiveData<String>       getSuccessMessage() { return successMessage; }
    public LiveData<String>       getErrorMessage()   { return errorMessage; }

    public boolean isSignedIn() {
        return auth.getCurrentUser() != null;
    }

    // ---- Auth actions ----

    /**
     * Signs in with email derived from username via Firestore lookup.
     * Falls back to treating {@code usernameOrEmail} as a direct email.
     */
    public void login(String usernameOrEmail, String password) {
        loading.setValue(true);

        // If looks like an email, sign in directly
        if (usernameOrEmail.contains("@")) {
            signInWithEmail(usernameOrEmail, password);
        } else {
            // Look up email by username in Firestore
            db.collection("users")
              .whereEqualTo("username", usernameOrEmail)
              .limit(1)
              .get()
              .addOnSuccessListener(snap -> {
                  if (!snap.isEmpty()) {
                      String email = snap.getDocuments().get(0).getString("email");
                      signInWithEmail(email, password);
                  } else {
                      loading.postValue(false);
                      errorMessage.postValue("Username not found");
                  }
              })
              .addOnFailureListener(e -> {
                  loading.postValue(false);
                  errorMessage.postValue(e.getMessage());
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
                loading.postValue(false);
                errorMessage.postValue("Invalid credentials. Please try again.");
            });
    }

    /**
     * Creates a Firebase Auth account and stores the user profile in Firestore.
     */
    public void register(String username, String email, String password) {
        loading.setValue(true);

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener(result -> {
                FirebaseUser firebaseUser = result.getUser();
                if (firebaseUser == null) {
                    loading.postValue(false);
                    errorMessage.postValue("Registration failed unexpectedly.");
                    return;
                }

                // Store profile in Firestore
                Map<String, Object> profile = new HashMap<>();
                profile.put("uid",      firebaseUser.getUid());
                profile.put("username", username);
                profile.put("email",    email);

                db.collection("users")
                  .document(firebaseUser.getUid())
                  .set(profile)
                  .addOnSuccessListener(v -> {
                      loading.postValue(false);
                      currentUser.postValue(firebaseUser);
                      successMessage.postValue("Account created successfully!");
                  })
                  .addOnFailureListener(e -> {
                      loading.postValue(false);
                      errorMessage.postValue("Profile save failed: " + e.getMessage());
                  });
            })
            .addOnFailureListener(e -> {
                loading.postValue(false);
                errorMessage.postValue(e.getMessage());
            });
    }

    /**
     * Sends a password reset email after verifying that the username + email match.
     */
    public void forgotPassword(String username, String email) {
        loading.setValue(true);

        db.collection("users")
          .whereEqualTo("username", username)
          .whereEqualTo("email", email)
          .limit(1)
          .get()
          .addOnSuccessListener(snap -> {
              if (!snap.isEmpty()) {
                  auth.sendPasswordResetEmail(email)
                      .addOnSuccessListener(v -> {
                          loading.postValue(false);
                          successMessage.postValue("Reset email sent to " + email);
                      })
                      .addOnFailureListener(e -> {
                          loading.postValue(false);
                          errorMessage.postValue(e.getMessage());
                      });
              } else {
                  loading.postValue(false);
                  errorMessage.postValue("No account found with that username and email.");
              }
          })
          .addOnFailureListener(e -> {
              loading.postValue(false);
              errorMessage.postValue(e.getMessage());
          });
    }

    /**
     * Fetches the Firestore profile for the currently signed-in user.
     */
    public LiveData<User> getUserProfile() {
        MutableLiveData<User> result = new MutableLiveData<>();
        FirebaseUser fu = auth.getCurrentUser();
        if (fu == null) { result.setValue(null); return result; }

        db.collection("users").document(fu.getUid())
          .get()
          .addOnSuccessListener(doc -> {
              if (doc.exists()) {
                  User u = new User(
                      doc.getString("uid"),
                      doc.getString("username"),
                      doc.getString("email")
                  );
                  result.postValue(u);
              }
          });
        return result;
    }

    public void signOut() {
        auth.signOut();
        currentUser.setValue(null);
    }
}
