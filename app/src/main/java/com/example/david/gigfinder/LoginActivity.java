package com.example.david.gigfinder;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
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
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import static com.example.david.gigfinder.GigFinderFirebaseMessagingService.DEVICE_TOKEN;
import static com.example.david.gigfinder.GigFinderFirebaseMessagingService.sendDeviceToken;

public class LoginActivity extends AppCompatActivity {
    public static final String ID_TOKEN = "IdToken";
    private static final String TAG = "LoginActivity";

    private GoogleSignInClient mGoogleSignInClient;
    private SignInButton signInButton;
    private ProgressBar progress;
    private String idToken;
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
        progress = findViewById(R.id.login_progress);

        if(getIntent().hasExtra("SignOut")){
            signOut();
        }

        if (!isServiceRunning(GigFinderFirebaseMessagingService.class))
            startService(new Intent(this, GigFinderFirebaseMessagingService.class));

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        String token = task.getResult().getToken();
                        SharedPreferences.Editor editor = getSharedPreferences(getString(R.string.shared_prefs), MODE_PRIVATE).edit();
                        editor.putString(DEVICE_TOKEN, token);
                        editor.apply();
                    }
                });
    }

    public boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onStart() {
        // Check for existing Google Sign In account, if the user is already signed in the GoogleSignInAccount will be non-null.
        super.onStart();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if(account != null) {
            Log.d(TAG, "GoogleSignIn: Account already signed in");
            signIn();
        } else {
            Log.d(TAG, "GoogleSignIn: No Account signed in");
        }
    }

    /**
     * Starts the GoogleSignIn Intent
     */
    private void signIn() {
        if(isNetworkAvailable()) {
            Log.d(TAG, "GoogleSignIn: Started!");
            signInButton.setVisibility(View.GONE);
            progress.setVisibility(View.VISIBLE);
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        }else{
            Log.d(TAG, "GoogleSignIn: No Connection!");
            Toast.makeText(getApplicationContext(),getString(R.string.no_connection),Toast.LENGTH_SHORT).show();
            offlineMode();
        }
    }

    /**
     * Signs out the client
     */
    private void signOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // ...
                    }
                });
    }

    /**
     * Checks if Network is Available
     * @return True if there is an Internet Connection
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * Checks if Cache is available to start offline mode
     */
    private void offlineMode(){
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.shared_prefs), Context.MODE_PRIVATE);

        if(!sharedPreferences.getString("user", "null").equals("null")){
            if(!sharedPreferences.getString("userProfile", "null").equals("null")){
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("idToken", "offline");
                startActivity(intent);
                finish();
            }
        }
    }

    @Override
    /**
     * Called after user tries to Sign In with Google
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach a listener.
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
        else {
            signInButton.setVisibility(View.VISIBLE);
            progress.setVisibility(View.GONE);
            Toast.makeText(getApplicationContext(),"Could not sign in",Toast.LENGTH_SHORT).show();
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
            idToken = googleSignInAccount.getIdToken();

            // Send Login request and wait for answer
            SendLogin sendLogin = new SendLogin();
            sendLogin.execute(idToken);

        }  else {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "GoogleSignIn: Failed");
            Toast.makeText(getApplicationContext(),"GoogleSignIn failed",Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Updates the GUI if the Login was successful
     */
    private void updateGUI(){
        Intent intent = new Intent(this, SelectUserActivity.class);
        intent.putExtra("idToken", idToken);
        startActivity(intent);
        finish();
    }

    /**
     * Sends the Authorization Token to the Rest Server and UpdatesGUI if Login is successful
     */
    class SendLogin extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                Log.d(TAG, "SendLogin: Sending GET Request");
                URL url = new URL("https://gigfinder.azurewebsites.net/api/login");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.setRequestProperty("Authorization", params[0]);
                urlConnection.setRequestMethod("GET");

                Log.d(TAG, params[0]);

                BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                return response.toString();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d(TAG, "SendLogin: " + result);
            if(result != null) {
                if (result.equalsIgnoreCase("true")) {
                    SharedPreferences.Editor editor = getSharedPreferences(getString(R.string.shared_prefs), MODE_PRIVATE).edit();
                    editor.putString(ID_TOKEN, idToken);
                    editor.apply();

                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.putExtra("idToken", idToken);
                    startActivity(intent);
                    finish();
                } else if (result.equalsIgnoreCase("false")) {
                    updateGUI();
                }
            }
            else {
                signInButton.setVisibility(View.VISIBLE);
                progress.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(),"Could not sign in",Toast.LENGTH_SHORT).show();
            }
        }
    }
}
