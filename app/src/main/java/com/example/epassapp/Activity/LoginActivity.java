package com.example.epassapp.Activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.epassapp.MainActivity;
import com.example.epassapp.Model.User;
import com.example.epassapp.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
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

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText editText1, editText, t1, t2;
    private LinearLayout asknumber_layout, verifyotp_layout, profile_layout, contractor_layout;
    private TextView resend;
    private Spinner spinner, spinner_con;
    private String number, id, phn, selected_post;
    private FirebaseAuth mAuth;
    private FirebaseUser currentuser;
    private String username, userpost, contractorname, trucknumber;
    private TextInputLayout truck_layout, name_layout;
    private ProgressDialog progressDialog;
    private PhoneAuthCredential credential;

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        asknumber_layout = findViewById(R.id.asknumber_layout);
        verifyotp_layout = findViewById(R.id.verifyotp_layout);
        profile_layout = findViewById(R.id.profile_layout);
        editText1 = findViewById(R.id.phn);
        t1 = findViewById(R.id.t1);
        MaterialButton btn1 = findViewById(R.id.btn1);
        mAuth = FirebaseAuth.getInstance();
        editText = findViewById(R.id.code);
        resend = findViewById(R.id.resend);
        spinner = findViewById(R.id.spinner_1);
        spinner_con = findViewById(R.id.spinner_3);
        MaterialButton button_signin = findViewById(R.id.btnsignin);
        truck_layout = findViewById(R.id.truck_layout);
        contractor_layout = findViewById(R.id.contractor_layout);
        name_layout = findViewById(R.id.name_layout);
        ConstraintLayout already_account = findViewById(R.id.already_account);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Making app ready for you");
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Please wait..");

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selected_post = spinner.getSelectedItem().toString();
                if (spinner.getSelectedItem().toString().equals(POST_TRUCKDRIVER)) {
                    truck_layout.setVisibility(View.VISIBLE);
                    t2 = findViewById(R.id.t2);
                } else {
                    truck_layout.setVisibility(View.GONE);
                }
                if (spinner.getSelectedItem().toString().equals(POST_CONTRACTOR)) {
                    name_layout.setVisibility(View.GONE);
                    contractor_layout.setVisibility(View.VISIBLE);
                } else {
                    name_layout.setVisibility(View.VISIBLE);
                    contractor_layout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        already_account.setOnClickListener(view -> {
            profile_layout.setVisibility(View.GONE);
            asknumber_layout.setVisibility(View.VISIBLE);
        });

        btn1.setOnClickListener(view -> {
            if (isEmpty()) {
                Toast.makeText(LoginActivity.this, "Please fill all the details!", Toast.LENGTH_SHORT).show();
            } else {
                userpost = spinner.getSelectedItem().toString();
                if (userpost.equals(POST_TRUCKDRIVER)) {
                    trucknumber = Objects.requireNonNull(t2.getText()).toString().trim();
                    username = convertToTitleCaseIteratingChars(Objects.requireNonNull(t1.getText()).toString().trim());
                } else if (userpost.equals(POST_CONTRACTOR))
                    contractorname = spinner_con.getSelectedItem().toString();
                else
                    username = convertToTitleCaseIteratingChars(Objects.requireNonNull(t1.getText()).toString().trim());
                profile_layout.setVisibility(View.GONE);
                asknumber_layout.setVisibility(View.VISIBLE);
            }
        });

        button_signin.setOnClickListener(view1 -> {
            number = Objects.requireNonNull(editText1.getText()).toString().trim();
            if (number.isEmpty() || number.length() < 10) {
                editText1.setError("wrong no");
                editText1.requestFocus();
            } else {
                number = "+91" + number;
                otpver();
            }
        });

    }

    public boolean isEmpty() {
        if (name_layout.getVisibility() == View.VISIBLE && Objects.requireNonNull(t1.getText()).toString().isEmpty())
            return true;
        else if (spinner.getSelectedItem().toString().equals("Select an option"))
            return true;
        else if (spinner.getSelectedItem().toString().equals(POST_CONTRACTOR) && spinner_con.getSelectedItem().toString().equals("Select an option"))
            return true;
        else
            return spinner.getSelectedItem().toString().equals(POST_TRUCKDRIVER) && Objects.requireNonNull(t2.getText()).toString().trim().isEmpty();
    }

    public void otpver() {
        asknumber_layout.setVisibility(View.GONE);
        verifyotp_layout.setVisibility(View.VISIBLE);
        MaterialButton btn = findViewById(R.id.btnver);
        sendVerification();
        btn.setOnClickListener(v -> {
            String code = Objects.requireNonNull(editText.getText()).toString().trim();
            if (code.isEmpty() || code.length() < 6) {
                editText.setError("enter code");
                editText.requestFocus();
            } else {
                verifyotp_layout.setVisibility(View.GONE);
                progressDialog.show();
                credential = PhoneAuthProvider.getCredential(id, code);
                signInWithPhoneAuthCredential(credential);
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
                number,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onCodeSent(@NonNull String s, @NonNull @NotNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        LoginActivity.this.id = s;
                        Toast.makeText(LoginActivity.this, "Code has been successfully sent to your number!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onVerificationCompleted(@NonNull @NotNull PhoneAuthCredential phoneAuthCredential) {
                        verifyotp_layout.setVisibility(View.GONE);
                        progressDialog.show();
                        signInWithPhoneAuthCredential(phoneAuthCredential);
                    }

                    @Override
                    public void onVerificationFailed(@NonNull @NotNull FirebaseException e) {
                        Log.e("exp", String.valueOf(e));
                        Toast.makeText(LoginActivity.this, "Failed!!!", Toast.LENGTH_SHORT).show();
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
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        Toast.makeText(LoginActivity.this, "Verification Failed", Toast.LENGTH_SHORT).show();
                    }
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
                    if (!spinner.getSelectedItem().toString().equals(POST_CONTRACTOR)) {
                        user.setUser_name(convertToTitleCaseIteratingChars(username));
                    } else {
                        user.setUser_name(contractorname);
                    }
                    user.setUser_phone(phn);
                    user.setUser_post(selected_post);
                    if (spinner.getSelectedItem().toString().equals(POST_TRUCKDRIVER)) {
                        user.setTruck_number(trucknumber);
                    }
                    usersReference.document(currentuser.getUid()).set(user);
                    Toast.makeText(LoginActivity.this, "Signed Up successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                } else {
                    //Logged-In
                    Toast.makeText(LoginActivity.this, "Logged In successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            }
        }).addOnFailureListener(e -> {
            Log.d("error_Login", Objects.requireNonNull(e.getMessage()));
            Toast.makeText(LoginActivity.this, "Something went wrong. Please try again!", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onBackPressed() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.getBoolean("fromMainActivity")) {
            moveTaskToBack(true);
            recreate();
        } else {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra("fromLoginActivity", true);
            startActivity(intent);
        }
    }

}
