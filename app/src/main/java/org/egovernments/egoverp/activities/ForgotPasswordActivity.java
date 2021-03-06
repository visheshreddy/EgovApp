package org.egovernments.egoverp.activities;


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
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.egovernments.egoverp.R;
import org.egovernments.egoverp.network.ApiController;
import org.egovernments.egoverp.network.SessionManager;
import com.google.gson.JsonObject;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * The password recovery screen activity
 **/

public class ForgotPasswordActivity extends AppCompatActivity {

    private String phone;
    private ProgressBar progressBar;

    private FloatingActionButton sendButton;
    private com.melnykov.fab.FloatingActionButton sendButtonCompat;

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgotpassword);

        progressBar = (ProgressBar) findViewById(R.id.forgotprogressBar);

        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00000000")));
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        sessionManager = new SessionManager(this);

        sendButton = (FloatingActionButton) findViewById(R.id.forgotpassword_send);
        sendButtonCompat = (com.melnykov.fab.FloatingActionButton) findViewById(R.id.forgotpassword_sendcompat);

        final EditText phone_edittext = (EditText) findViewById(R.id.forgotpassword_edittext);
        phone_edittext.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {

                    phone = phone_edittext.getText().toString().trim();
                    progressBar.setVisibility(View.VISIBLE);
                    sendButton.setVisibility(View.GONE);
                    sendButtonCompat.setVisibility(View.GONE);
                    submit(phone);
                    return true;
                }
                return false;
            }
        });


        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                phone = phone_edittext.getText().toString().trim();
                progressBar.setVisibility(View.VISIBLE);
                sendButton.setVisibility(View.GONE);
                sendButtonCompat.setVisibility(View.GONE);
                submit(phone);


            }
        };

        if (Build.VERSION.SDK_INT >= 21)

        {

            sendButton.setOnClickListener(onClickListener);

        } else

        {
            sendButtonCompat.setVisibility(View.VISIBLE);
            sendButton.setVisibility(View.GONE);
            sendButtonCompat.setOnClickListener(onClickListener);
        }

    }

    //Invokes call to API
    private void submit(String phone) {

        if (TextUtils.isEmpty(phone)) {
            Toast toast = Toast.makeText(ForgotPasswordActivity.this, R.string.forgot_password_prompt, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);
            toast.show();
            progressBar.setVisibility(View.GONE);
            if (Build.VERSION.SDK_INT >= 21) {
                sendButton.setVisibility(View.VISIBLE);
            } else {
                sendButtonCompat.setVisibility(View.VISIBLE);
            }

        } else if (phone.length() != 10) {
            Toast toast = Toast.makeText(ForgotPasswordActivity.this, R.string.mobilenumber_length_prompt, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);
            toast.show();
            progressBar.setVisibility(View.GONE);
            if (Build.VERSION.SDK_INT >= 21) {
                sendButton.setVisibility(View.VISIBLE);
            } else {
                sendButtonCompat.setVisibility(View.VISIBLE);
            }
        } else {
            ApiController.getAPI(ForgotPasswordActivity.this).recoverPassword(phone, sessionManager.getBaseURL(), new Callback<JsonObject>() {
                @Override
                public void success(JsonObject resp, Response response) {

                    Toast toast = Toast.makeText(ForgotPasswordActivity.this, R.string.recoverymessage_msg, Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);
                    toast.show();

                    progressBar.setVisibility(View.GONE);
                    if (Build.VERSION.SDK_INT >= 21) {
                        sendButton.setVisibility(View.VISIBLE);
                    } else {
                        sendButtonCompat.setVisibility(View.VISIBLE);
                    }
                    finish();

                }

                @Override
                public void failure(RetrofitError error) {
                    if (error.getLocalizedMessage() != null && !error.getLocalizedMessage().contains("400")) {
                        if (error.getLocalizedMessage() != null) {
                            Toast toast = Toast.makeText(ForgotPasswordActivity.this, error.getLocalizedMessage(), Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);
                            toast.show();
                        } else {
                            JsonObject jsonObject = (JsonObject) error.getBody();
                            if (jsonObject != null) {
                                Toast toast = Toast.makeText(ForgotPasswordActivity.this, R.string.noaccount_msg, Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);
                                toast.show();
                            }

                        }
                    } else {
                        Toast toast = Toast.makeText(ForgotPasswordActivity.this, "An unexpected error occurred while accessing the network", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);
                        toast.show();
                    }

                    progressBar.setVisibility(View.GONE);
                    if (Build.VERSION.SDK_INT >= 21) {
                        sendButton.setVisibility(View.VISIBLE);
                    } else {
                        sendButtonCompat.setVisibility(View.VISIBLE);
                    }

                }
            });

        }


    }
}
