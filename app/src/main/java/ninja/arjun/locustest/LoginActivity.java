package ninja.arjun.locustest;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;

import android.view.View;

import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;

import com.facebook.FacebookSdk;

import org.json.JSONObject;


public class LoginActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {

    private GoogleApiClient mGoogleApiClient;  // Google sign in button code
    private static final int RC_SIGN_IN = 9001;  // Google sign in button code
    private static final String TAG = "SignInActivity";  // Google sign in button code

    CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(this.getApplicationContext());

        callbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {

                        GraphRequest request = GraphRequest.newMeRequest(
                                loginResult.getAccessToken(),
                                new GraphRequest.GraphJSONObjectCallback() {
                                    @Override
                                    public void onCompleted(
                                            JSONObject object,
                                            GraphResponse response) {
                                        // Application code
                                        Log.v("LoginActivity", response.toString());
                                    }
                                });
                        Bundle parameters = new Bundle();
                        parameters.putString("fields", "id, name, email, gender, birthday");
                        request.setParameters(parameters);
                        request.executeAsync();

                        Toast toast = Toast.makeText(getApplicationContext(),
                                "Facebook login successful",
                                Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.BOTTOM, 0, 10);
                        toast.show();

                        Intent i = new Intent(getApplicationContext(), NavDrawerActivity.class);
                        startActivity(i);

                    }

                    @Override
                    public void onCancel() {
                        // App code
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        // App code
                    }
                });

        setContentView(R.layout.activity_login);

        findViewById(R.id.sign_in_button).setOnClickListener(this); // if we set like this, we can work on code in onClick()

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        SignInButton signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setScopes(gso.getScopeArray());
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
        }
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void handleSignInResult(GoogleSignInResult result) {
//        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {

            GoogleSignInAccount acct = result.getSignInAccount();
            String personName = acct.getDisplayName();

            String displayName = getString(R.string.loginSuccessMessage);
            displayName = displayName.concat(personName);
            Toast toast = Toast.makeText(getApplicationContext(),
                    displayName,
                    Toast.LENGTH_LONG);
            toast.setGravity(Gravity.BOTTOM, 0, 10);
            toast.show();

            String personEmail = acct.getEmail();
            String personID = acct.getId();
            Uri personPhotoURI = acct.getPhotoUrl();

            SharedPreferences sharedPreferences = getSharedPreferences("MyPref", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("email", personEmail);
            editor.putString("name", personName);
            editor.putString("photoURI", personPhotoURI.toString());

            editor.apply();


            //proceed to Map activity
//            Intent i = new Intent(getApplicationContext(),MapActivity.class);
//            startActivity(i);
            Intent i = new Intent(getApplicationContext(), NavDrawerActivity.class);
            startActivity(i);
        } else {
            Toast toast = Toast.makeText(getApplicationContext(),
                    R.string.loginFailMessage,
                    Toast.LENGTH_LONG);
            toast.setGravity(Gravity.BOTTOM, 0, 10);
            toast.show();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast toast = Toast.makeText(getApplicationContext(),
                connectionResult.toString(),
                Toast.LENGTH_LONG);
        toast.setGravity(Gravity.BOTTOM, 0, 10);
        toast.show();
    }

}