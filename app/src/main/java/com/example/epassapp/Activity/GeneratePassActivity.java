package com.example.epassapp.Activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.epassapp.MainActivity;
import com.example.epassapp.Model.Pass;
import com.example.epassapp.Model.User;
import com.example.epassapp.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Locale;
import java.util.Objects;
import java.util.Random;

import static com.example.epassapp.utilities.Constants.E_PASSES;
import static com.example.epassapp.utilities.Constants.PASS_ACCEPTED;
import static com.example.epassapp.utilities.Constants.PASS_PENDING;
import static com.example.epassapp.utilities.Constants.USER_ACCOUNTS;
import static com.example.epassapp.utilities.Constants.date;
import static com.example.epassapp.utilities.Constants.time;

public class GeneratePassActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private TextInputEditText pit_owner, section_no, bench_no, pass_date, serial_no, truck_no;
    private Spinner spinner, spinner_con;
    private String serial_number, user_name;
    private MaterialTextView pass_count, account_verify;
    private ProgressDialog progressDialog;
    private ProgressBar progressBar;
    private MaterialButton generate_pass;
    private ConstraintLayout form_layout;
    private FirebaseFirestore firestore;
    private boolean fromSiteInCharge;
    private DrawerLayout drawerLayout;

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

    @SuppressLint("DefaultLocale")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generatepass);

        pit_owner = findViewById(R.id.pit_owner);
        section_no = findViewById(R.id.section_no);
        bench_no = findViewById(R.id.bench_no);
        pass_date = findViewById(R.id.pass_date);
        pass_count = findViewById(R.id.pass_status);
        generate_pass = findViewById(R.id.generate_pass);
        serial_no = findViewById(R.id.serial_no);
        truck_no = findViewById(R.id.truck_number);
        progressBar = findViewById(R.id.progress);
        account_verify = findViewById(R.id.account_verify);
        form_layout = findViewById(R.id.form_layout);
        spinner = findViewById(R.id.spinner_1);
        LinearLayout contractor_layout = findViewById(R.id.contractor_layout);
        spinner_con = findViewById(R.id.spinner_2);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.navigation_view);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setElevation(4);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);

        Random random = new Random();
        serial_number = String.format("%06d", random.nextInt(1000000));

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            fromSiteInCharge = bundle.getBoolean("fromSiteInCharge");
        }
        if (fromSiteInCharge) {
            Objects.requireNonNull(getSupportActionBar()).setTitle("Generate Pass");
            contractor_layout.setVisibility(View.VISIBLE);
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        } else {
            Objects.requireNonNull(getSupportActionBar()).setTitle("Contractor");
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();
            navigationView.setNavigationItemSelectedListener(this);
        }
        firestore = FirebaseFirestore.getInstance();
        CollectionReference userRef = firestore.collection(USER_ACCOUNTS);
        userRef.document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                User user = Objects.requireNonNull(task.getResult()).toObject(User.class);
                if (user != null) {
                    if (user.getIsVerified().equals(PASS_ACCEPTED)) {
                        user_name = user.getUser_name();
                        progressBar.setVisibility(View.GONE);
                        form_layout.setVisibility(View.VISIBLE);
                        serial_no.setText(serial_number);
                        pass_date.setText(date);
                        generate_pass.setOnClickListener(view -> {
                            if (!isEmpty()) {
                                final CollectionReference epassReference = firestore.collection(E_PASSES);
                                final String pass_ref_id = epassReference.document().getId();
                                progressDialog = new ProgressDialog(GeneratePassActivity.this);
                                progressDialog.setTitle("Please wait");
                                progressDialog.setMessage("Generating pass..");
                                progressDialog.setCancelable(false);
                                progressDialog.show();
                                epassReference.document(pass_ref_id + "#" + date).get().addOnSuccessListener(documentSnapshot -> {
                                    Pass epass = new Pass();
                                    epass.setDate(date);
                                    epass.setPass_time(time);
                                    epass.setSerial_no(serial_number);
                                    epass.setMine_no(spinner.getSelectedItem().toString().trim());
                                    epass.setPit_owner(convertToTitleCaseIteratingChars(Objects.requireNonNull(pit_owner.getText()).toString().trim()));
                                    epass.setSection_no(Objects.requireNonNull(section_no.getText()).toString().trim());
                                    epass.setBench_no(Objects.requireNonNull(bench_no.getText()).toString().trim());
                                    epass.setTruck_no(Objects.requireNonNull(truck_no.getText()).toString().trim());
                                    epass.setUser_id(pass_ref_id + "#" + date);
                                    if (!fromSiteInCharge) {
                                        epass.setContractor_name(user.getUser_name());
                                    } else {
                                        if (!spinner_con.getSelectedItem().toString().equals("Select an option")) {
                                            epass.setContractor_name(spinner_con.getSelectedItem().toString().trim());
                                        } else {
                                            progressDialog.dismiss();
                                            Toast.makeText(this, "Please fill all the details!", Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                    }
                                    epass.setPass_approved(PASS_PENDING);

                                    epassReference.document(pass_ref_id + "#" + date).set(epass);
                                    Toast.makeText(GeneratePassActivity.this, "Pass Generated Successfully!", Toast.LENGTH_SHORT).show();
                                    firestore.collection(E_PASSES).get().addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            int count = 0;
                                            for (DocumentSnapshot document : Objects.requireNonNull(task1.getResult())) {
                                                if (Objects.requireNonNull(document.toObject(Pass.class)).getDate().equals(date))
                                                    count++;
                                            }
                                            pass_count.setText("No. of truck pass generated today : " + count);
                                        }
                                    });
                                    progressDialog.dismiss();
                                    clearfeilds();
                                }).addOnFailureListener(e -> {
                                    progressDialog.dismiss();
                                    Toast.makeText(GeneratePassActivity.this, "Something went wrong. Please try again.", Toast.LENGTH_SHORT).show();
                                });
                            } else {
                                Toast.makeText(this, "Please fill all the details!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        progressBar.setVisibility(View.GONE);
                        account_verify.setVisibility(View.VISIBLE);
                        Toast.makeText(GeneratePassActivity.this, "Account not verified!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

    }

    public void clearfeilds() {
        Random random = new Random();
        serial_number = String.format(Locale.US, "%06d", random.nextInt(1000000));
        serial_no.setText(serial_number);
        pass_date.setText(date);
        spinner.setSelection(0);
        truck_no.setText("");
        pit_owner.setText("");
        section_no.setText("");
        bench_no.setText("");
    }

    public boolean isEmpty() {
        if (spinner.getSelectedItem().toString().equals("Select an option"))
            return true;
        else if (Objects.requireNonNull(pit_owner.getText()).toString().isEmpty())
            return true;
        else if (Objects.requireNonNull(section_no.getText()).toString().isEmpty())
            return true;
        else return Objects.requireNonNull(bench_no.getText()).toString().isEmpty();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }
        return false;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.logout: {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(this, "Signed Out Successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(GeneratePassActivity.this, MainActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.active_pass: {
                Intent intent = new Intent(GeneratePassActivity.this, IndividualPassActivity.class);
                intent.putExtra("CurrentContractorName", user_name);
                startActivity(intent);
                break;
            }
            case R.id.history: {
                Intent intent = new Intent(GeneratePassActivity.this, IndividualPassActivity.class);
                intent.putExtra("CurrentContractorName", user_name);
                intent.putExtra("fromHistory", true);
                startActivity(intent);
                break;
            }
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            if (fromSiteInCharge) {
                finish();
            } else {
                moveTaskToBack(true);
            }
        }
    }
}