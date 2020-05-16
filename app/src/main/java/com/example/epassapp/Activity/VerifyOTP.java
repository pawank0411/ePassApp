package com.example.epassapp.Activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.epassapp.MainActivity;
import com.example.epassapp.Model.User;
import com.example.epassapp.R;
import com.example.epassapp.utilities.Cryptography;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.annotations.NotNull;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.example.epassapp.utilities.Constants.PASS_PENDING;
import static com.example.epassapp.utilities.Constants.POST_CONTRACTOR;
import static com.example.epassapp.utilities.Constants.POST_TRUCKDRIVER;
import static com.example.epassapp.utilities.Constants.USER_ACCOUNTS;

public class VerifyOTP extends AppCompatActivity {
    private LinearLayout verifyotp_layout;
    private String phoneNumber, id;
    private FirebaseAuth mAuth;
    private FirebaseUser currentuser;
    private ProgressDialog progressDialog;
    private PhoneAuthCredential credential;
    private TextView resend;
    private TextInputEditText code;
    private String username, userpost, contractorname, trucknumber;
    private boolean deleteUser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        verifyotp_layout = findViewById(R.id.verifyotp_layout);
        LinearLayout profile_layout;
        profile_layout = findViewById(R.id.profile_layout);

        mAuth = FirebaseAuth.getInstance();
        code = findViewById(R.id.code);
        resend = findViewById(R.id.resend);

        profile_layout.setVisibility(View.GONE);
        verifyotp_layout.setVisibility(View.VISIBLE);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Getting ready the app for you");
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Please don't close or refresh the app");

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            if (bundle.getBoolean("fromMainActivity")) {
                phoneNumber = bundle.getString("PhoneNumber");
                username = bundle.getString("username");
                userpost = bundle.getString("userpost");
                contractorname = bundle.getString("contractorname");
                trucknumber = bundle.getString("trucknumber");
            } else {
                deleteUser = bundle.getBoolean("deleteUser");
                phoneNumber = bundle.getString("user_phone");
            }
        }
        sendVerification();
        MaterialButton btn = findViewById(R.id.btnver);
        btn.setOnClickListener(view -> {
            String otp = Objects.requireNonNull(code.getText()).toString().trim();
            if (otp.isEmpty() || otp.length() < 6) {
                code.setError("enter code");
                code.requestFocus();
            } else {
                verifyotp_layout.setVisibility(View.GONE);
                progressDialog.show();
                credential = PhoneAuthProvider.getCredential(id, otp);
                if (!deleteUser) {
                    signInWithPhoneAuthCredential(credential);
                } else {
                    deleteWithPhoneAuthCredential(credential);
                }
            }
        });

        resend.setOnClickListener(v -> sendVerification());
    }

    private void sendVerification() {
        new CountDownTimer(60000, 1000) {

            @SuppressLint("SetTextI18n")
            @Override
            public void onTick(long l) {
                resend.setText("" + l / 1000);
                resend.setEnabled(false);
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onFinish() {
                resend.setText("Resend");
                resend.setEnabled(true);
            }
        }.start();
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onCodeSent(@NonNull String s, @NonNull @NotNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        VerifyOTP.this.id = s;
                        Toast.makeText(VerifyOTP.this, "Code has been successfully sent to your number!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onVerificationCompleted(@NonNull @NotNull PhoneAuthCredential phoneAuthCredential) {
                        verifyotp_layout.setVisibility(View.GONE);
                        if (!deleteUser) {
                            progressDialog.show();
                            signInWithPhoneAuthCredential(phoneAuthCredential);
                        } else {
                            progressDialog.setTitle("Deleting your account");
                            progressDialog.setCancelable(false);
                            progressDialog.setMessage("Please don't close or refresh the app");
                            progressDialog.show();
                            deleteWithPhoneAuthCredential(phoneAuthCredential);
                        }
                    }

                    @Override
                    public void onVerificationFailed(@NonNull @NotNull FirebaseException e) {
                        Log.e("exp", String.valueOf(e));
                        Toast.makeText(VerifyOTP.this, "Failed!!!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        profile();
                        Toast.makeText(this, "Verification Successful!", Toast.LENGTH_SHORT).show();
                    } else {
                        progressDialog.dismiss();
                        Intent intent = new Intent(VerifyOTP.this, MainActivity.class);
                        startActivity(intent);
                        Toast.makeText(VerifyOTP.this, "Verification Failed", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(e -> {
            Toast.makeText(VerifyOTP.this, "Something went wrong please try again!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(VerifyOTP.this, MainActivity.class);
            startActivity(intent);
        });
    }

    public void deleteWithPhoneAuthCredential(PhoneAuthCredential credential) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Objects.requireNonNull(user).reauthenticate(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                user.delete()
                        .addOnCompleteListener(task12 -> {
                            if (task12.isSuccessful()) {
                                FirebaseFirestore.getInstance().collection(USER_ACCOUNTS)
                                        .document(user.getUid())
                                        .delete()
                                        .addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful()) {
                                                progressDialog.dismiss();
                                                Toast.makeText(VerifyOTP.this, "Account Deleted Successfully!", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(VerifyOTP.this, MainActivity.class);
                                                startActivity(intent);
                                            }
                                        }).addOnFailureListener(e -> {
                                    Toast.makeText(this, "Something went wrong.Please try again!", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(VerifyOTP.this, MainActivity.class);
                                    startActivity(intent);
                                });
                            }
                        }).addOnFailureListener(e -> {
                    Toast.makeText(this, "Something went wrong.Please try again!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(VerifyOTP.this, MainActivity.class);
                    startActivity(intent);
                });
            }
        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(this, "Something went wrong.Please try again!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(VerifyOTP.this, MainActivity.class);
            startActivity(intent);
        });
    }

    private void profile() {
        currentuser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        final CollectionReference usersReference = firestore.collection(USER_ACCOUNTS);

        usersReference.document(Objects.requireNonNull(currentuser).getUid()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                progressDialog.dismiss();
                if (task.getResult() == null || !task.getResult().exists()) {
                    //Signed up
                    User user = new User();
                    user.setIsVerified(PASS_PENDING);
                    user.setUser_id(currentuser.getUid());
                    if (!userpost.equals(POST_CONTRACTOR)) {
                        user.setUser_name(convertToTitleCaseIteratingChars(username));
                    } else {
                        user.setUser_name(contractorname);
                        user.setEx_user_name(convertToTitleCaseIteratingChars(username));
                    }
                    user.setUser_phone(Cryptography.encrypt(phoneNumber));
                    user.setUser_post(userpost);
                    if (userpost.equals(POST_TRUCKDRIVER)) {
                        user.setTruck_number(trucknumber);
                    }
                    usersReference.document(currentuser.getUid()).set(user);
                    Toast.makeText(VerifyOTP.this, "Signed Up successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(VerifyOTP.this, MainActivity.class);
                    startActivity(intent);
                } else {
                    //Logged-In
                    Toast.makeText(VerifyOTP.this, "Logged In successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(VerifyOTP.this, MainActivity.class);
                    startActivity(intent);
                }
            }
        }).addOnFailureListener(e -> {
            Log.d("error_Login", Objects.requireNonNull(e.getMessage()));
            Toast.makeText(VerifyOTP.this, "Something went wrong. Please try again!", Toast.LENGTH_SHORT).show();
        });
    }

    public static String convertToTitleCaseIteratingChars(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        StringBuilder converted = new StringBuilder();

        boolean convertNext = true;
        for (char ch : text.toCharArray()) {
            if (Character.isSpaceChar(ch)) {
                convertNext = true;
            } else if (convertNext) {
                ch = Character.toTitleCase(ch);
                convertNext = false;
            } else {
                ch = Character.toLowerCase(ch);
            }
            converted.append(ch);
        }

        return converted.toString();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}
