package com.fitnessproject.myapplication;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.view.View;


import com.fitnessproject.myapplication.databinding.ActivitySelectionBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;


public class SelectionActivity extends AppCompatActivity {

    private ActivityResultLauncher<Intent> launcher;
    private FirebaseAuth auth;
    private ActivitySelectionBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySelectionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();

        // create launcher menu
        launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // Get result from user and convert it from intent
                    Task<GoogleSignInAccount> task
                            = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    //  Check, if we can connect account and get data from it or not
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        if(account != null) {
                            firebaseAuthWithGoogle(account.getIdToken());
                        }
                        startActivity(new Intent(this, MainActivity.class));
                    } catch (ApiException e) {
                        Log.d("API EXCEPTION", e.toString());
                    }
                });

        // Set on click listener on button Google
        binding.selectionButtonGoogle.setOnClickListener(v -> {

            signInWithGoogle();
        });
        // Set on click listener on button Login
        binding.selectionButtonLogin.setOnClickListener(v -> {

                if(auth.getCurrentUser() == null)
                    startActivity(new Intent(this, LoginActivity.class));
                else
                    checkAuthState();
            }
        );
        // This method hides tool bar
        hideStatusBar();

        // Check if user was already logged in using google or not
        checkAuthState();
    }

    // send clients like an intent
    public void signInWithGoogle() {
        GoogleSignInClient signInClient = getClient();
        launcher.launch(signInClient.getSignInIntent());
    }

    // We create a google client that will show all accounts on the device
    private GoogleSignInClient getClient() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        return GoogleSignIn.getClient(this, gso);
    }

    // connect google account to firebase
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential).addOnCompleteListener(command -> {
            if(command.isSuccessful()) {
                Log.d("My log", "Google sign in done");
                // Check account
                checkAuthState();
            }else {
                Log.d("My log", "Google sign in error");
            }
        });
    }

    private void hideStatusBar() {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.hide();

    }

    // If user is already logged in we go to main activity
    private void checkAuthState() {
        if(auth.getCurrentUser() != null) {
            startActivity(new Intent(this, MainActivity.class));
        }
    }

}