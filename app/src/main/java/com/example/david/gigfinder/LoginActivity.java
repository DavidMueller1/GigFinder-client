package com.example.david.gigfinder;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInApi;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private GoogleSignInClient mGoogleSignInClient;
    private SignInButton signInButton;
    private GoogleSignInAccount GoogleAccount;
    private static final int RC_SIGN_IN = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_WIDE);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
    }

    /**
     * Starts the GoogleSignIn Intent
     */
    private void signIn() {

        Log.d(TAG, "GoogleSignIn: Started!");

        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach a listener.
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    /**
     * Handles the Sign In Result after GoogleSignIn Task is completed
     * @param result
     */
    private void handleSignInResult(GoogleSignInResult result) {

        if(result.isSuccess()){
            // Signed in successfully, show authenticated UI.
            Log.d(TAG, "GoogleSignIn: Successful");
            GoogleSignInAccount googleSignInAccount = result.getSignInAccount();
            String idToken = googleSignInAccount.getIdToken();

            //TODO: Send idToken to Server and wait for answer

            //Update GUI to SelectUserActivity
            Intent intent = new Intent(this, SelectUserActivity.class);
            startActivity(intent);
            finish();

        }  else {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "GoogleSignIn: Failed");
            Toast.makeText(getApplicationContext(),"GoogleSignIn failed",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    /**
     * Checks if there is a Google Account that is already signed in and updates GUI
     */
    protected void onStart() {
        // Check for existing Google Sign In account, if the user is already signed in the GoogleSignInAccount will be non-null.
        super.onStart();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if(account != null) {
            Log.d(TAG, "GoogleSignIn: Account already signed in");
            //TODO: Update GUI
            Toast.makeText(getApplicationContext(),"Already Signed In",Toast.LENGTH_SHORT).show();
        } else {
            Log.d(TAG, "GoogleSignIn: No Account signed in");
        }
    }
}
