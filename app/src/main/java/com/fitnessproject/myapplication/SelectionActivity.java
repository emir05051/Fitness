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

        launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Task<GoogleSignInAccount> task
                            = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        if(account != null) {
                            firebaseAuthWithGoogle(account.getIdToken());
                        }
                    } catch (ApiException e) {
                        Log.d("API EXCEPTION", e.toString());
                    }
                });


        binding.selectionButtonGoogle.setOnClickListener(v -> {
            signInWithGoogle();
        });

        binding.selectionButtonLogin.setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class))
        );
        //        Hide status bar
        hideStatusBar();

    }


    public void signInWithGoogle() {
        GoogleSignInClient signInClient = getClient();
        launcher.launch(signInClient.getSignInIntent());
    }



    private GoogleSignInClient getClient() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        return GoogleSignIn.getClient(this, gso);
    }


    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential).addOnCompleteListener(command -> {
            if(command.isSuccessful()) {
                Log.d("My log", "Google sign in done");
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
        actionBar.hide();

    }



}