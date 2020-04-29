package com.example.epassapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.epassapp.MainActivity;
import com.example.epassapp.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

import static com.example.epassapp.utilities.Constants.POST_CONTRACTOR;
import static com.example.epassapp.utilities.Constants.POST_TRUCKDRIVER;

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText editText1, t1, t2;
    private LinearLayout asknumber_layout, profile_layout, contractor_layout;
    private Spinner spinner, spinner_con;
    private String number;
    private String username, userpost, contractorname, trucknumber;
    private TextInputLayout truck_layout;

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
        profile_layout = findViewById(R.id.profile_layout);
        editText1 = findViewById(R.id.phn);
        t1 = findViewById(R.id.t1);
        MaterialButton btn1 = findViewById(R.id.btn1);
        spinner = findViewById(R.id.spinner_1);
        spinner_con = findViewById(R.id.spinner_3);
        MaterialButton button_signin = findViewById(R.id.btnsignin);
        truck_layout = findViewById(R.id.truck_layout);
        contractor_layout = findViewById(R.id.contractor_layout);
        ConstraintLayout already_account = findViewById(R.id.already_account);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (spinner.getSelectedItem().toString().equals(POST_TRUCKDRIVER)) {
                    truck_layout.setVisibility(View.VISIBLE);
                    t2 = findViewById(R.id.t2);
                } else {
                    truck_layout.setVisibility(View.GONE);
                }
                if (spinner.getSelectedItem().toString().equals(POST_CONTRACTOR)) {
                    contractor_layout.setVisibility(View.VISIBLE);
                } else {
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
                } else if (userpost.equals(POST_CONTRACTOR))
                    contractorname = spinner_con.getSelectedItem().toString();
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
                Intent intent = new Intent(LoginActivity.this, VerifyOTP.class);
                intent.putExtra("PhoneNumber", number);
                intent.putExtra("username", username);
                intent.putExtra("contractorname", contractorname);
                intent.putExtra("trucknumber", trucknumber);
                intent.putExtra("userpost", userpost);
                startActivity(intent);
            }
        });

    }

    public boolean isEmpty() {
        if (Objects.requireNonNull(t1.getText()).toString().isEmpty())
            return true;
        else if (spinner.getSelectedItem().toString().equals("Select an option"))
            return true;
        else if (spinner.getSelectedItem().toString().equals(POST_CONTRACTOR) && spinner_con.getSelectedItem().toString().equals("Select an option"))
            return true;
        else
            return spinner.getSelectedItem().toString().equals(POST_TRUCKDRIVER) && Objects.requireNonNull(t2.getText()).toString().trim().isEmpty();
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
