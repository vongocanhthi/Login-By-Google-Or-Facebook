package com.vnat.loginbygoogleorfacebook;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.LoginStatusCallback;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.login.widget.ProfilePictureView;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    ImageView imgAvatar;
    TextView txtName;
    SignInButton signInButton;
    Button btnSignOut;

    GoogleSignInClient mGoogleSignInClient;
    //------------------------------

    ProfilePictureView imgFB;
    CallbackManager callbackManager;
    LoginButton loginButton;

    private static final int RC_SIGN_IN = 831;
    boolean isGoogle = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        FacebookSdk.sdkInitialize(getApplicationContext());

        setContentView(R.layout.activity_main);

        initGoogle();
        eventGoogle();

        initFB();
        eventFB();


    }

    private void initFB() {
        imgFB = findViewById(R.id.imgFB);

        loginButton = findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList("public_profile", "email", "user_birthday", "user_friends"));

        //LoginManager.getInstance().logInWithReadPermissions(MainActivity.this, Arrays.asList("public_profile"));

        LoginManager.getInstance().retrieveLoginStatus(this, new LoginStatusCallback() {
            @Override
            public void onCompleted(AccessToken accessToken) {

            }

            @Override
            public void onFailure() {

            }

            @Override
            public void onError(Exception exception) {

            }
        });
    }

    private void eventFB() {
        // Callback registration
        callbackManager = CallbackManager.Factory.create();

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                Log.v("zzz", response.getJSONObject().toString());

                                btnSignOut.setVisibility(View.VISIBLE);
                                loginButton.setVisibility(View.GONE);
                                isGoogle = false;
                                // Application code
                                try {
                                    String name = object.getString("name");
                                    txtName.setText(name);
                                    imgFB.setProfileId(Profile.getCurrentProfile().getId());
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                //String birthday = object.getString("birthday"); // 01/31/1980 format
                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email,gender,birthday");
                request.setParameters(parameters);
                request.executeAsync();


            }

            @Override
            public void onCancel() {
                // App code
                Log.v("zzz", "cancel");
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
                Log.v("zzz", exception.getCause().toString());
            }
        });

    }


    private void initGoogle() {
        imgAvatar = findViewById(R.id.imgAvatar);
        txtName = findViewById(R.id.txtName);
        btnSignOut = findViewById(R.id.btnSignOut);

        signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);

        btnSignOut.setVisibility(View.INVISIBLE);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

    }

    private void eventGoogle() {

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });

        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        String name = acct.getDisplayName();
        Uri urlAvatar = acct.getPhotoUrl();
        if (acct != null) {
            txtName.setText(name);

            Glide.with(this)
                    .load(urlAvatar)
                    .error(android.R.drawable.star_on)
                    .into(imgAvatar);
        }

        btnSignOut.setVisibility(View.VISIBLE);
        isGoogle = true;
    }

    private void signOut() {
        if (isGoogle){
            mGoogleSignInClient.signOut().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    txtName.setText("Đã đăng xuất");
                    imgAvatar.setImageResource(R.drawable.googleg_disabled_color_18);
                }
            });

            signInButton.setVisibility(View.VISIBLE);

        }else{
            LoginManager.getInstance().logOut();
            txtName.setText("Đã đăng xuất");
            imgFB.setProfileId(null);

            btnSignOut.setVisibility(View.GONE);
        }

    }

}