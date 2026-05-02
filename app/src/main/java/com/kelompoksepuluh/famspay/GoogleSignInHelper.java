package com.kelompoksepuluh.famspay;

import android.app.Activity;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

public class GoogleSignInHelper {
    private static final String TAG = "GoogleSignInHelper";
    private static final int SIGN_IN_REQUEST_CODE = 9001;

    private final Activity activity;
    private final FirebaseAuth firebaseAuth;
    private final GoogleSignInClient googleSignInClient;
    private GoogleSignInCallback callback;

    public interface GoogleSignInCallback {
        void onSignInSuccess(String userId, String email);
        void onSignInFailure(String errorMessage);
    }

    public GoogleSignInHelper(Activity activity) {
        this.activity = activity;
        this.firebaseAuth = FirebaseAuth.getInstance();

        // googlesign in config
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(activity.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        this.googleSignInClient = GoogleSignIn.getClient(activity, gso);
    }

    public void setCallback(GoogleSignInCallback callback) {
        this.callback = callback;
    }

    public void signIn() {
        activity.startActivityForResult(googleSignInClient.getSignInIntent(), SIGN_IN_REQUEST_CODE);
    }

    public void handleSignInResult(int requestCode, int resultCode, android.content.Intent data) {
        if (requestCode == SIGN_IN_REQUEST_CODE) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(Exception.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account.getIdToken());
                }
            } catch (Exception e) {
                Log.w(TAG, "Google sign in failed", e);
                if (callback != null) {
                    callback.onSignInFailure("Google sign in gagal: " + e.getMessage());
                }
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(activity, task -> {
                    if (task.isSuccessful()) {
                        String userId = firebaseAuth.getCurrentUser().getUid();
                        String email = firebaseAuth.getCurrentUser().getEmail();
                        Log.d(TAG, "Firebase auth dengan Google berhasil: " + email);
                        if (callback != null) {
                            callback.onSignInSuccess(userId, email);
                        }
                    } else {
                        Log.w(TAG, "Firebase signInWithCredential gagal", task.getException());
                        if (callback != null) {
                            callback.onSignInFailure("Firebase auth gagal");
                        }
                    }
                });
    }

    public void signOut() {
        firebaseAuth.signOut();
        googleSignInClient.signOut();
    }
}