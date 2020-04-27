package com.example.epassapp.Activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.epassapp.MainActivity;
import com.example.epassapp.Model.User;
import com.example.epassapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.annotations.NotNull;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.example.epassapp.utilities.Constants.PASS_PENDING;
import static com.example.epassapp.utilities.Constants.USER_ACCOUNTS;

public class LoginActivity extends AppCompatActivity {
    private EditText editText1;
    private LinearLayout l1, l2, l3;
    private EditText editText, t1, t2;
    private DatabaseReference db;
    private TextView resend;
    private Button btn1;
    private Spinner spinner;
    private String number, id, phn, selected_post;
    private FirebaseAuth mAuth;
    private boolean isexists;
    private String post;
    private FirebaseUser currentuser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        l1 = findViewById(R.id.l1);
        l2 = findViewById(R.id.l2);
        l3 = findViewById(R.id.l3);
        editText1 = findViewById(R.id.phn);
        t1 = findViewById(R.id.t1);
        btn1 = findViewById(R.id.btn1);
        mAuth = FirebaseAuth.getInstance();
        editText = findViewById(R.id.code);
        resend = findViewById(R.id.resend);
        spinner = findViewById(R.id.spinner_1);

        findViewById(R.id.btnsignin).setOnClickListener(v -> {
            String number = editText1.getText().toString().trim();
            if (number.isEmpty() || number.length() < 10) {
                editText1.setError("wrong no");
                editText1.requestFocus();
            } else {
                phn = "+91" + number;
                otpver(phn);
            }
        });
    }

    public void otpver(String phn) {
        l1.setVisibility(View.GONE);
        l2.setVisibility(View.VISIBLE);
        Button btn = findViewById(R.id.btnver);
        number = phn;
        sendVerification();
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = editText.getText().toString().trim();
                if (code.isEmpty() || code.length() < 6) {
                    editText.setError("enter code");
                    editText.requestFocus();
                } else {
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(id, code);
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });
        resend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendVerification();
            }
        });
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
                    }

                    @Override
                    public void onVerificationCompleted(@NonNull @NotNull PhoneAuthCredential phoneAuthCredential) {
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
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            profile();
                        } else {
                            Toast.makeText(LoginActivity.this, "Verification Filed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void profile() {
        l2.setVisibility(View.GONE);
        currentuser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        final CollectionReference usersReference = firestore.collection(USER_ACCOUNTS);
        TextInputLayout truck_layout = findViewById(R.id.truck_layout);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selected_post = spinner.getSelectedItem().toString();
                if (spinner.getSelectedItem().toString().equals("Truck driver")) {
                    truck_layout.setVisibility(View.VISIBLE);
                    t2 = findViewById(R.id.t2);
                } else {
                    TextInputLayout truck_layout = findViewById(R.id.truck_layout);
                    truck_layout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        usersReference.document(Objects.requireNonNull(currentuser).getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult() == null || !task.getResult().exists()) {
                        //Signed up
                        l3.setVisibility(View.VISIBLE);
                        btn1.setOnClickListener(view -> {
                            if (t1.getText().toString().matches("") || spinner.getSelectedItem().toString().equals("Select an option")) {
                                Toast.makeText(LoginActivity.this, "Enter Full details", Toast.LENGTH_SHORT).show();
                            } else {
                                User user = new User();
                                user.setIsVerified(PASS_PENDING);
                                user.setUser_id(currentuser.getUid());
                                user.setUser_name(t1.getText().toString().trim());
                                user.setUser_phone(phn);
                                user.setUser_post(selected_post);
                                if (selected_post.equals("Truck driver")) {
                                    user.setTruck_number(t2.getText().toString().trim());
                                }
                                usersReference.document(currentuser.getUid()).set(user);
                                Toast.makeText(LoginActivity.this, "Signed Up successfully", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                            }
                        });
                    } else {
                        //Logged-In
                        Toast.makeText(LoginActivity.this, "Logged In successfully", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("error_Login", Objects.requireNonNull(e.getMessage()));
                Toast.makeText(LoginActivity.this, "Something went wrong. Please try again!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
