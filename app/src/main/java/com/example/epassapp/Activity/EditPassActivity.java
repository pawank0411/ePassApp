package com.example.epassapp.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.epassapp.Model.Pass;
import com.example.epassapp.Model.User;
import com.example.epassapp.R;
import com.example.epassapp.utilities.AskSignature;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;

import static com.example.epassapp.utilities.Constants.E_PASSES;
import static com.example.epassapp.utilities.Constants.PASS_ACCEPTED;
import static com.example.epassapp.utilities.Constants.PASS_BENCHNO;
import static com.example.epassapp.utilities.Constants.PASS_DATE;
import static com.example.epassapp.utilities.Constants.PASS_MINENO;
import static com.example.epassapp.utilities.Constants.PASS_PITOWNER;
import static com.example.epassapp.utilities.Constants.PASS_SECTIONNO;
import static com.example.epassapp.utilities.Constants.PASS_SERIALNO;
import static com.example.epassapp.utilities.Constants.PASS_TRUCKNO;
import static com.example.epassapp.utilities.Constants.USER_ACCOUNTS;
import static com.example.epassapp.utilities.Constants.USER_ID;

public class EditPassActivity extends AppCompatActivity {
    private TextInputEditText pit_owner, section_no, bench_no, pass_date, serial_no, truck_no;
    private Spinner spinner;
    private ProgressDialog progressDialog;
    private String pass_id;
    private boolean signature_exists;
    private String user_name;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generatepass);

        AskSignature askSignature = new AskSignature(this);
        ;
        pit_owner = findViewById(R.id.pit_owner);
        section_no = findViewById(R.id.section_no);
        bench_no = findViewById(R.id.bench_no);
        pass_date = findViewById(R.id.pass_date);
        MaterialButton generate_pass = findViewById(R.id.generate_pass);
        serial_no = findViewById(R.id.serial_no);
        truck_no = findViewById(R.id.truck_number);
        final ProgressBar progressBar = findViewById(R.id.progress);
        ConstraintLayout form_layout = findViewById(R.id.form_layout);
        MaterialTextView heading = findViewById(R.id.heading);
        spinner = findViewById(R.id.spinner_1);

        progressBar.setVisibility(View.GONE);
        form_layout.setVisibility(View.VISIBLE);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            serial_no.setText(bundle.getString(PASS_SERIALNO));
            spinner.setSelection(bundle.getInt(PASS_MINENO));
            truck_no.setText(bundle.getString(PASS_TRUCKNO));
            pit_owner.setText(bundle.getString(PASS_PITOWNER));
            section_no.setText(bundle.getString(PASS_SECTIONNO));
            bench_no.setText(bundle.getString(PASS_BENCHNO));
            pass_date.setText(bundle.getString(PASS_DATE));
            pass_id = bundle.getString(USER_ID);
        }

        FirebaseFirestore.getInstance().collection(USER_ACCOUNTS)
                .document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                User user = Objects.requireNonNull(task.getResult()).toObject(User.class);
                if (user != null) {
                    user_name = user.getUser_name();
                    StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("signatures/" + user_name + ".png");
                    storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        signature_exists = true;
                    }).addOnFailureListener(e -> {
                        signature_exists = false;
                    });
                }
            }
        }).addOnFailureListener(e -> Toast.makeText(EditPassActivity.this, "Something went wrong.Please Try again", Toast.LENGTH_SHORT).show());

        heading.setText("EDIT PASS");
        generate_pass.setText("APPROVE PASS");
        generate_pass.setOnClickListener(view -> {
            if (!pit_owner.getText().toString().isEmpty() && !bench_no.getText().toString().trim().isEmpty() &&
                    !truck_no.getText().toString().trim().isEmpty()
                    && !section_no.getText().toString().trim().isEmpty()) {

                final CollectionReference epassReference = FirebaseFirestore.getInstance().collection(E_PASSES);
                epassReference.document(pass_id).get().addOnCompleteListener(task -> {
                    if (!signature_exists) {
                        askSignature.GetSignature(user_name);
                        signature_exists = true;
                    } else {
                        progressDialog = new ProgressDialog(EditPassActivity.this);
                        progressDialog.setTitle("Please wait");
                        progressDialog.setMessage("Generating pass..");
                        progressDialog.setCancelable(false);
                        progressDialog.show();
                        Pass epass = new Pass();
                        epass.setDate(pass_date.getText().toString().trim());
                        epass.setSerial_no(serial_no.getText().toString().trim());
                        epass.setMine_no(spinner.getSelectedItem().toString().trim());
                        epass.setPit_owner(pit_owner.getText().toString().trim());
                        epass.setSection_no(section_no.getText().toString().trim());
                        epass.setBench_no(bench_no.getText().toString().trim());
                        epass.setTruck_no(truck_no.getText().toString().trim());
                        epass.setUser_id(pass_id);
                        epass.setPass_approved(PASS_ACCEPTED);

                        epassReference.document(pass_id).set(epass);
                        progressDialog.dismiss();
                        Intent intent = new Intent(EditPassActivity.this, ApprovePassActivity.class);
                        startActivity(intent);
                    }
                }).addOnFailureListener(e -> Toast.makeText(EditPassActivity.this, "Account not verified!", Toast.LENGTH_SHORT).show());
            }
        });
    }
}
