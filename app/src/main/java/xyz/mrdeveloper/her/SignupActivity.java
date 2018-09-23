package xyz.mrdeveloper.her;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hbb20.CountryCodePicker;

import java.util.concurrent.TimeUnit;

public class SignupActivity extends AppCompatActivity implements
        View.OnClickListener {

    CountryCodePicker countryCodePicker;
    EditText mPhoneNumberField, mVerificationField;
    Button mStartButton, mVerifyButton, mResendButton;
    String mPhoneNumber;

    private FirebaseAuth mAuth;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    String mVerificationId;

    LinearLayout registrationView;
    LinearLayout verificationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        countryCodePicker = findViewById(R.id.country_code_picker);
        mPhoneNumberField = findViewById(R.id.field_phone_number);
        mVerificationField = findViewById(R.id.field_verification_code);
        countryCodePicker.registerCarrierNumberEditText(mPhoneNumberField);

        mStartButton = findViewById(R.id.button_start_verification);
        mVerifyButton = findViewById(R.id.button_verify_phone);
        mResendButton = findViewById(R.id.button_resend);

        mStartButton.setOnClickListener(this);
        mVerifyButton.setOnClickListener(this);
        mResendButton.setOnClickListener(this);

        registrationView = findViewById(R.id.registration_view);
        verificationView = findViewById(R.id.verification_view);
        verificationView.setVisibility(View.GONE);

        mAuth = FirebaseAuth.getInstance();

        Log.d("Check", "$$$$SIGNUP$$$$");
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                @Override
                public void onVerificationCompleted(PhoneAuthCredential credential) {
                    Log.d("Check", "onVerificationCompleted:" + credential);
                    SignInWithPhoneAuthCredential(credential);
                }

                @Override
                public void onVerificationFailed(FirebaseException e) {
                    Log.w("Check", "onVerificationFailed", e);
                    if (e instanceof FirebaseAuthInvalidCredentialsException) {
                        mPhoneNumberField.setError("Invalid phone number");
                    } else if (e instanceof FirebaseTooManyRequestsException) {
                        Snackbar.make(findViewById(android.R.id.content), "Number of tries exceeded. Please try after some time.",
                                Snackbar.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                    Log.d("Check", "onCodeSent:" + verificationId);
                    mVerificationId = verificationId;
                    mResendToken = token;
                }
            };
        } else {
            StartMainActivity();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_start_verification:
                if (IsPhoneNumberValid()) {
                    ShowTermsOfServiceDialog();
                }
                break;

            case R.id.button_verify_phone:
                String code = mVerificationField.getText().toString();
                if (TextUtils.isEmpty(code)) {
                    mVerificationField.setError("Cannot be empty.");
                    return;
                }

                Toast.makeText(this, "Verifying code", Toast.LENGTH_SHORT).show();
                VerifyPhoneNumberWithCode(mVerificationId, code);
                break;

            case R.id.button_resend:
                Toast.makeText(this, "Resending code", Toast.LENGTH_SHORT).show();
                ResendVerificationCode(countryCodePicker.getFullNumberWithPlus(), mResendToken);
                break;
        }
    }

    private void SignInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        final Context _this = this;
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d("Check", "signInWithCredential:success");
                            FirebaseUser user = task.getResult().getUser();

                            mPhoneNumber = user.getPhoneNumber();

                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(_this).edit();
                            editor.putString("myPhoneNumber", mPhoneNumber);
                            editor.apply();

                            AddUserToFirebase();
                            Snackbar.make(findViewById(android.R.id.content), "Signing you in. Welcome to the community :)",
                                    Snackbar.LENGTH_SHORT).show();

                            StartMainActivity();

                        } else {
                            Log.w("Check", "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                mVerificationField.setError("Invalid code. Please try again.");
                            }
                        }
                    }
                });
    }


    private void VerifyPhoneNumberWithCode(String verificationId, String code) {
        if (verificationId != null) {
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
            SignInWithPhoneAuthCredential(credential);
        }
    }

    private void StartPhoneNumberVerification(String phoneNumber) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,              // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,        // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
    }

    private void ResendVerificationCode(String phoneNumber, PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks,         // OnVerificationStateChangedCallbacks
                token);             // ForceResendingToken from callbacks
    }

    private boolean IsPhoneNumberValid() {
        String phoneNumber = mPhoneNumberField.getText().toString();
        if (TextUtils.isEmpty(phoneNumber)) {
            mPhoneNumberField.setError("Please enter a phone number");
            return false;
        }
        if (!countryCodePicker.isValidFullNumber()) {
            mPhoneNumberField.setError("Invalid phone number");
            return false;
        }
        return true;
    }

    private void ShowTermsOfServiceDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.terms_of_use_title)
                .setView(getLayoutInflater().inflate(R.layout.dialog_terms_of_use, null))
                .setPositiveButton("AGREE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        verificationView.setVisibility(View.VISIBLE);
                        registrationView.setVisibility(View.GONE);
                        StartPhoneNumberVerification(countryCodePicker.getFullNumberWithPlus());
                    }
                })
                .setNegativeButton("DECLINE", null)
                .create()
                .show();
    }

    public void AddUserToFirebase() {
        final DatabaseReference mFirebaseDatabase;
        FirebaseDatabase mFirebaseInstance = FirebaseDatabase.getInstance();

        Log.i("Log", "Adding user....");

        mFirebaseDatabase = mFirebaseInstance.getReference("personData");
        mFirebaseDatabase.child(mPhoneNumber).child("isInEmergency").setValue(false);
        mFirebaseDatabase.child(mPhoneNumber).child("phoneNumber").setValue(mPhoneNumber);
        mFirebaseDatabase.child(mPhoneNumber).child("latitude").setValue(0);
        mFirebaseDatabase.child(mPhoneNumber).child("longitude").setValue(0);
    }

    public void StartMainActivity() {
        startActivity(new Intent(SignupActivity.this, MainActivity.class));
        finish();
    }
}
