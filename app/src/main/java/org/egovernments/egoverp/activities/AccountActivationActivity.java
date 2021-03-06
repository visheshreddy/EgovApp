package org.egovernments.egoverp.activities;


import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.egovernments.egoverp.R;
import org.egovernments.egoverp.network.ApiController;
import org.egovernments.egoverp.network.ApiUrl;
import org.egovernments.egoverp.network.SessionManager;
import org.egovernments.egoverp.network.UpdateService;
import com.google.gson.JsonObject;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * The OTP verification screen activity
 **/


public class AccountActivationActivity extends AppCompatActivity {

    private String username;
    private String password;
    private String activationCode;

    private ProgressBar progressBar;

    private FloatingActionButton activateButton;
    private com.melnykov.fab.FloatingActionButton activateButtonCompat;
    private Button resendButton;

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accountactivation);

        sessionManager = new SessionManager(this);

        progressBar = (ProgressBar) findViewById(R.id.activateprogressBar);
        resendButton = (Button) findViewById(R.id.activate_resend);

        //Intent extras sent from register activity or from login activity
        username = getIntent().getStringExtra("username");
        password = getIntent().getStringExtra("password");

        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00000000")));
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        final EditText code_edittext = (EditText) findViewById(R.id.activate_otpfield);

        code_edittext.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {

                    activationCode = code_edittext.toString().trim();
                    progressBar.setVisibility(View.VISIBLE);
                    activateButton.setVisibility(View.GONE);
                    activateButtonCompat.setVisibility(View.GONE);
                    resendButton.setVisibility(View.INVISIBLE);
                    submit(activationCode);
                    return true;
                }
                return false;
            }
        });


        resendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ApiController.getAPI(AccountActivationActivity.this).sendOTP(username, new Callback<JsonObject>() {
                    @Override
                    public void success(JsonObject jsonObject, Response response) {
                        Toast toast = Toast.makeText(AccountActivationActivity.this, R.string.otp_resent_msg, Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);
                        toast.show();
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        if (error.getLocalizedMessage() != null) {
                            Toast toast = Toast.makeText(AccountActivationActivity.this, error.getLocalizedMessage(), Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);
                            toast.show();
                        } else {
                            Toast toast = Toast.makeText(AccountActivationActivity.this, "An unexpected error occurred while accessing the network", Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);
                            toast.show();
                        }
                    }
                });

            }
        });

        activateButton = (FloatingActionButton) findViewById(R.id.activate_verify);
        activateButtonCompat = (com.melnykov.fab.FloatingActionButton) findViewById(R.id.activate_verifycompat);

        final View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                activationCode = code_edittext.getText().toString().trim();
                progressBar.setVisibility(View.VISIBLE);
                activateButton.setVisibility(View.GONE);
                activateButtonCompat.setVisibility(View.GONE);
                submit(activationCode);
            }
        };


        if (Build.VERSION.SDK_INT >= 21) {

            activateButton.setOnClickListener(onClickListener);
        } else {

            activateButtonCompat.setVisibility(View.VISIBLE);
            activateButton.setVisibility(View.GONE);
            activateButtonCompat.setOnClickListener(onClickListener);

        }
    }

    //Method invokes call to API
    private void submit(String activationCode) {

        if (!TextUtils.isEmpty(activationCode)) {
            ApiController.getAPI(AccountActivationActivity.this).activate(username, activationCode.toUpperCase(), new Callback<JsonObject>() {
                @Override
                public void success(JsonObject jsonObject, Response response) {
                    Toast toast = Toast.makeText(AccountActivationActivity.this, R.string.account_activated_msg, Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);
                    toast.show();
                    ApiController.getAPI(AccountActivationActivity.this).login(ApiUrl.AUTHORIZATION,username, "read write", password, "password", new Callback<JsonObject>() {
                        @Override
                        public void success(JsonObject jsonObject, Response response) {

                            sessionManager.loginUser(password, username, jsonObject.get("access_token").toString());
                            startService(new Intent(AccountActivationActivity.this, UpdateService.class)
                                    .putExtra(UpdateService.KEY_METHOD, UpdateService.UPDATE_ALL));
                            startActivity(new Intent(AccountActivationActivity.this, HomeActivity.class));
                            finish();
                        }

                        @Override
                        public void failure(RetrofitError error) {

                            Toast toast = Toast.makeText(AccountActivationActivity.this, "An error occurred while logging in", Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);
                            toast.show();
                            startActivity(new Intent(AccountActivationActivity.this, LoginActivity.class));
                        }
                    });
                }

                @Override
                public void failure(RetrofitError error) {
                    if (error.getLocalizedMessage() != null && !error.getLocalizedMessage().contains("400")) {
                        if (error.getLocalizedMessage() != null) {
                            Toast toast = Toast.makeText(AccountActivationActivity.this, error.getLocalizedMessage(), Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);
                            toast.show();
                        } else {
                            JsonObject jsonObject = (JsonObject) error.getBody();
                            if (jsonObject != null) {
                                Toast toast = Toast.makeText(AccountActivationActivity.this, "The OTP is incorrect or has expired", Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);
                                toast.show();
                            } else {
                                Toast toast = Toast.makeText(AccountActivationActivity.this, "An unexpected error occurred while accessing the network", Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);
                                toast.show();
                            }
                        }
                    }

                    progressBar.setVisibility(View.GONE);
                    if (Build.VERSION.SDK_INT >= 21) {
                        activateButton.setVisibility(View.VISIBLE);
                    } else {
                        activateButtonCompat.setVisibility(View.VISIBLE);
                    }
                }
            });
        } else {
            Toast toast = Toast.makeText(AccountActivationActivity.this, "Please enter OTP", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);
            toast.show();

            progressBar.setVisibility(View.GONE);
            resendButton.setVisibility(View.VISIBLE);
            if (Build.VERSION.SDK_INT >= 21) {
                activateButton.setVisibility(View.VISIBLE);
            } else {
                activateButtonCompat.setVisibility(View.VISIBLE);
            }
        }


    }
}
