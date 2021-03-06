package org.egovernments.egoverp.activities;


import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import org.egovernments.egoverp.R;
import org.egovernments.egoverp.events.ProfileUpdatedEvent;
import org.egovernments.egoverp.models.Profile;
import org.egovernments.egoverp.models.ProfileAPIResponse;
import org.egovernments.egoverp.models.errors.ErrorResponse;
import org.egovernments.egoverp.network.ApiController;
import org.egovernments.egoverp.network.SessionManager;
import org.egovernments.egoverp.network.UpdateService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.greenrobot.event.EventBus;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * The profile edit screen activity
 **/

public class ProfileEditActivity extends AppCompatActivity {

    private EditText profileName;
    private EditText profilePhone;
    private EditText profileAltPhone;
    private EditText profileEmail;
    private EditText profileDOB;
    private EditText profileAadhaar;
    private EditText profilePAN;

    private RadioGroup profileGender;

    private SessionManager sessionManager;

    private ProgressDialog progressDialog;

    private String date_of_birth;

    public static final String PROFILE_EDIT_CONTENT = "profile";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        Profile profile = (Profile) getIntent().getSerializableExtra(PROFILE_EDIT_CONTENT);

        sessionManager = new SessionManager(ProfileEditActivity.this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressDialog = new ProgressDialog(this, ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Processing request");
        progressDialog.setCancelable(false);

        profileName = (EditText) findViewById(R.id.editprofile_name);
        profilePhone = (EditText) findViewById(R.id.editprofile_phoneno);
        profileAltPhone = (EditText) findViewById(R.id.editprofile_altphoneno);
        profileEmail = (EditText) findViewById(R.id.editprofile_email);
        profileDOB = (EditText) findViewById(R.id.editprofile_dateofbirth);
        profileAadhaar = (EditText) findViewById(R.id.editprofile_aadhaarcardno);
        profilePAN = (EditText) findViewById(R.id.editprofile_PANcardno);


        if (profile != null) {
            profileName.setText(profile.getName());
            profilePhone.setText(profile.getMobileNumber());
            profileAltPhone.setText(profile.getAltContactNumber());
            profileEmail.setText(profile.getEmailId());
            date_of_birth = profile.getDob();
            if (!(profile.getDob() == null)) {
                try {
                    profileDOB.setText(new SimpleDateFormat("d MMMM, yyyy", Locale.ENGLISH).format(new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(profile.getDob())));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            profileAadhaar.setText(profile.getAadhaarCard());
            profilePAN.setText(profile.getPanCard());

            profileGender = (RadioGroup) findViewById(R.id.editprofile_gender);
            profileGender.check(R.id.radioButton_others);

            if (profile.getGender() != null) {
                switch (profile.getGender()) {
                    case "MALE":
                        profileGender.check(R.id.radioButton_male);
                        break;
                    case "FEMALE":
                        profileGender.check(R.id.radioButton_female);
                        break;
                }
            }


            DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    profileDOB.setText(String.valueOf(dayOfMonth) + "-" + String.valueOf(monthOfYear + 1) + "-" + String.valueOf(year));
                    date_of_birth = String.valueOf(year) + "-" + String.valueOf(monthOfYear + 1) + "-" + String.valueOf(dayOfMonth);
                }
            };

            Calendar dob = Calendar.getInstance();
            if (profile.getDob() != null) {
                try {
                    dob.setTime(new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(profile.getDob()));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            final DatePickerDialog datePickerDialog = new DatePickerDialog(ProfileEditActivity.this, onDateSetListener, dob.get(Calendar.YEAR), dob.get(Calendar.MONTH), dob.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.setCancelable(false);
            datePickerDialog.setTitle(R.string.dob_label_editprofile);

            profileDOB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    datePickerDialog.show();
                }
            });
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.profiledit_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.confirm:
                progressDialog.show();
                submit();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //Invokes call to the API
    private void submit() {

        profileName = (EditText) findViewById(R.id.editprofile_name);
        profilePhone = (EditText) findViewById(R.id.editprofile_phoneno);
        profileAltPhone = (EditText) findViewById(R.id.editprofile_altphoneno);
        profileEmail = (EditText) findViewById(R.id.editprofile_email);
        profileDOB = (EditText) findViewById(R.id.editprofile_dateofbirth);
        profileAadhaar = (EditText) findViewById(R.id.editprofile_aadhaarcardno);
        profilePAN = (EditText) findViewById(R.id.editprofile_PANcardno);

        profileGender = (RadioGroup) findViewById(R.id.editprofile_gender);
        String gender = null;
        if (profileGender.getCheckedRadioButtonId() == R.id.radioButton_male) {
            gender = "MALE";
        } else if (profileGender.getCheckedRadioButtonId() == R.id.radioButton_female) {
            gender = "FEMALE";
        } else if (profileGender.getCheckedRadioButtonId() == R.id.radioButton_others) {
            gender = "OTHERS";
        }

        String name = profileName.getText().toString().trim();
        String emailId = profileEmail.getText().toString().trim();
        String mobileNumber = profilePhone.getText().toString().trim();
        String altContactNumber = profileAltPhone.getText().toString().trim();
        String dob = date_of_birth;
        String panCard = profilePAN.getText().toString().trim();
        String aadhaarCard = profileAadhaar.getText().toString().trim();

        if (mobileNumber.length() != 10) {
            Toast toast = Toast.makeText(ProfileEditActivity.this, "Phone no. must be 10 digits", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);
            toast.show();
            progressDialog.dismiss();
        } else if (TextUtils.isEmpty(mobileNumber)) {
            Toast toast = Toast.makeText(ProfileEditActivity.this, "Please enter mobile number", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);
            toast.show();
            progressDialog.dismiss();
        } else if (TextUtils.isEmpty(emailId)) {
            Toast toast = Toast.makeText(ProfileEditActivity.this, "Please enter email ID", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);
            toast.show();
            progressDialog.dismiss();
        } else if (!TextUtils.isEmpty(altContactNumber) && altContactNumber.length() != 10) {
            Toast toast = Toast.makeText(ProfileEditActivity.this, "Alternate Phone no. must be 10 digits", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);
            toast.show();
            progressDialog.dismiss();
        } else if (!isValidEmail(emailId)) {
            Toast toast = Toast.makeText(ProfileEditActivity.this, "Please enter a valid email ID", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);
            toast.show();
            progressDialog.dismiss();
        } else if (dob.isEmpty()) {
            Toast toast = Toast.makeText(ProfileEditActivity.this, "Please enter date of birth", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);
            toast.show();
            progressDialog.dismiss();
        } else {

            final Profile update_profile = new Profile(name, emailId, mobileNumber, mobileNumber, altContactNumber, gender, panCard, dob, aadhaarCard);

            ApiController.getAPI(ProfileEditActivity.this).updateProfile(update_profile, sessionManager.getAccessToken(), new Callback<ProfileAPIResponse>() {
                @Override
                public void success(ProfileAPIResponse profileAPIResponse, Response response) {

                    Toast toast = Toast.makeText(ProfileEditActivity.this, "Profile updated", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);
                    toast.show();

                    ProfileActivity.profile = profileAPIResponse.getProfile();

                    EventBus.getDefault().post(new ProfileUpdatedEvent());

                    progressDialog.dismiss();
                    startService(new Intent(ProfileEditActivity.this, UpdateService.class)
                            .putExtra(UpdateService.KEY_METHOD, UpdateService.UPDATE_PROFILE));
                    finish();
                }

                @Override
                public void failure(RetrofitError error) {

                    if (error.getLocalizedMessage() != null)
                        if (error.getLocalizedMessage().equals("Invalid access token")) {
                            Toast toast = Toast.makeText(ProfileEditActivity.this, "Session expired", Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);
                            toast.show();
                            sessionManager.logoutUser();
                            startActivity(new Intent(ProfileEditActivity.this, LoginActivity.class));
                        } else if (error.getLocalizedMessage().contains("400")) {
                            try {
                                ErrorResponse errorResponse = (ErrorResponse) error.getBodyAs(ErrorResponse.class);
                                Toast toast = Toast.makeText(ProfileEditActivity.this, errorResponse.getErrorStatus().getMessage(), Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);
                                toast.show();
                            } catch (Exception e) {
                                Toast toast = Toast.makeText(ProfileEditActivity.this, "An unexpected error occurred while accessing the network", Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);
                                toast.show();
                            }
                        } else {
                            Toast toast = Toast.makeText(ProfileEditActivity.this, error.getLocalizedMessage(), Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);
                            toast.show();
                        }
                    else {
                        Toast toast = Toast.makeText(ProfileEditActivity.this, "An unexpected error occurred while accessing the network", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);
                        toast.show();
                    }

                    progressDialog.dismiss();

                }
            });

        }
    }

    //Matches the email id against the pattern to check if it is of a typical format
    private boolean isValidEmail(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

}
