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
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.epassapp.MainActivity;
import com.example.epassapp.Model.Pass;
import com.example.epassapp.Model.User;
import com.example.epassapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

import static com.example.epassapp.utilities.Constants.E_PASSES;
import static com.example.epassapp.utilities.Constants.PASS_ACCEPTED;
import static com.example.epassapp.utilities.Constants.PASS_PENDING;
import static com.example.epassapp.utilities.Constants.USER_ACCOUNTS;

public class GeneratePassActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private TextInputEditText pit_owner, section_no, bench_no, pass_date, serial_no, truck_no;
    private Spinner spinner, spinner_con;
    private String date, serial_number, user_name;
    private MaterialTextView pass_count, account_verify;
    private ProgressDialog progressDialog;
    private ProgressBar progressBar;
    private MaterialButton generate_pass;
    private ConstraintLayout form_layout;
    private FirebaseFirestore firestore;
    private boolean fromSiteInCharge;

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

        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.navigation_view);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setElevation(4);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Contractor");
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
        date = sdf.format(new Date());

        Random random = new Random();
        serial_number = String.format("%06d", random.nextInt(1000000));

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            fromSiteInCharge = bundle.getBoolean("fromSiteInCharge");
        }
        if (fromSiteInCharge) {
            contractor_layout.setVisibility(View.VISIBLE);
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
                            final CollectionReference epassReference = firestore.collection(E_PASSES);
                            final String pass_ref_id = epassReference.document().getId();
                            progressDialog = new ProgressDialog(GeneratePassActivity.this);
                            progressDialog.setTitle("Please wait");
                            progressDialog.setMessage("Generating pass..");
                            progressDialog.setCancelable(false);
                            progressDialog.show();
                            epassReference.document(pass_ref_id + "#" + date).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    Pass epass = new Pass();
                                    epass.setDate(date);
                                    epass.setSerial_no(serial_number);
                                    epass.setMine_no(spinner.getSelectedItem().toString().trim());
                                    epass.setPit_owner(pit_owner.getText().toString().trim());
                                    epass.setSection_no(section_no.getText().toString().trim());
                                    epass.setBench_no(bench_no.getText().toString().trim());
                                    epass.setTruck_no(truck_no.getText().toString().trim());
                                    epass.setUser_id(pass_ref_id + "#" + date);
                                    if (!fromSiteInCharge) {
                                        epass.setContractor_name(user.getUser_name());
                                    } else {
                                        epass.setContractor_name(spinner_con.getSelectedItem().toString().trim());
                                    }
                                    epass.setPass_approved(PASS_PENDING);

                                    epassReference.document(pass_ref_id + "#" + date).set(epass);
                                    Toast.makeText(GeneratePassActivity.this, "Pass Generated Successfully!", Toast.LENGTH_SHORT).show();
                                    firestore.collection(E_PASSES).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task1) {
                                            if (task1.isSuccessful()) {
                                                int count = 0;
                                                for (DocumentSnapshot document : Objects.requireNonNull(task1.getResult())) {
                                                    if (Objects.requireNonNull(document.toObject(Pass.class)).getDate().equals(date))
                                                        count++;
                                                }
                                                pass_count.setText("No. of truck pass generated today : " + count);
                                            }
                                        }
                                    });
                                    progressDialog.dismiss();
                                    clearfeilds();
                                }
                            }).addOnFailureListener(e -> {
                                progressDialog.dismiss();
                                Toast.makeText(GeneratePassActivity.this, "Something went wrong. Please try again.", Toast.LENGTH_SHORT).show();
                            });
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
        serial_number = String.format("%06d", random.nextInt(1000000));
        serial_no.setText(serial_number);
        pass_date.setText(date);
        spinner.setSelection(0);
        truck_no.setText("");
        pit_owner.setText("");
        section_no.setText("");
        bench_no.setText("");
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
            case R.id.active_pass:
                Intent intent = new Intent(GeneratePassActivity.this, IndividualPassActivity.class);
                intent.putExtra("CurrentContractorName",user_name);
                startActivity(intent);
                break;
            case R.id.history:
                Toast.makeText(this, "history", Toast.LENGTH_SHORT).show();
                break;
        }
        return false;
    }
}
//        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//        if (user != null) {
//            DatabaseReference db1 = FirebaseDatabase.getInstance().getReference("user")
//                    .child(Objects.requireNonNull(post)).child(user.getUid()).child(USER_VERIFIED);
//
//            db1.addValueEventListener(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
//                    String isVerified = dataSnapshot.getValue(String.class);
//                    if (isVerified != null) {
//                        if (isVerified.equals("1")) {
//                            Toast.makeText(GeneratePass.this, "Account Verified", Toast.LENGTH_SHORT).show();
//                            DatabaseReference db2 = FirebaseDatabase.getInstance().getReference("epass");
//                            progressBar.setVisibility(View.GONE);
//                            form_layout.setVisibility(View.VISIBLE);
//                            mine_number.setText("1");
//                            serial_no.setText(serial_number);
//                            pass_date.setText(date);
//                            db2.addChildEventListener(new ChildEventListener() {
//                                @Override
//                                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//                                    if (Objects.requireNonNull(dataSnapshot.getKey()).matches(date)) {
//                                        String a = Long.toString(dataSnapshot.getChildrenCount());
//                                        pass_count.setText("No. of truck pass generated today : " + a);
//                                    } else {
//                                        pass_count.setText("No. of truck pass generated today : 0");
//                                    }
//                                }
//
//                                @Override
//                                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//
//                                }
//
//                                @Override
//                                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
//
//                                }
//
//                                @Override
//                                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//
//                                }
//
//                                @Override
//                                public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                                }
//                            });
//                            generate_pass.setOnClickListener(new View.OnClickListener() {
//                                @Override
//                                public void onClick(View view) {
//                                    if (!pit_owner.getText().toString().isEmpty() && !bench_no.getText().toString().trim().isEmpty() &&
//                                            !truck_no.getText().toString().trim().isEmpty()
//                                            && !section_no.getText().toString().trim().isEmpty()) {
//                                        progressDialog = new ProgressDialog(GeneratePass.this);
//                                        progressDialog.setTitle("Please wait");
//                                        progressDialog.setCancelable(false);
//                                        progressDialog.show();
//                                        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
//                                        String pass_id = firebaseDatabase.getReference("epass").push().getKey();
//                                        DatabaseReference db = firebaseDatabase.getReference("epass").
//                                                child(date).child(Objects.requireNonNull(pass_id));
//                                        db.child(USER_ID).setValue(pass_id);
//                                        db.child(PASS_SERIALNO).setValue(serial_number);
//                                        db.child(PASS_MINENO).setValue("1");
//                                        db.child(PASS_PITOWNER).setValue(pit_owner.getText().toString());
//                                        db.child(PASS_BENCHNO).setValue(bench_no.getText().toString());
//                                        db.child(PASS_TRUCKNO).setValue(truck_no.getText().toString());
//                                        db.child(PASS_SECTIONNO).setValue(section_no.getText().toString());
//                                        db.child(PASS_DATE).setValue(date);
//                                        db.child(PASS_APPROVED).setValue("pending");
//                                        DatabaseReference db1 = FirebaseDatabase.getInstance().getReference("date").child(date);
//                                        db1.child("date").setValue(date);
//                                        clearfeilds();
//                                    } else {
//                                        Toast.makeText(GeneratePass.this, "please fill all the details", Toast.LENGTH_SHORT).show();
//                                    }
//                                }
//                            });
//                        } else {
//                            progressBar.setVisibility(View.GONE);
//                            account_verify.setVisibility(View.VISIBLE);
//                            Toast.makeText(GeneratePass.this, "Account not verified!", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError databaseError) {
//                    Log.d("DatabaseError", databaseError.getMessage());
//                }
//            });
//        }
