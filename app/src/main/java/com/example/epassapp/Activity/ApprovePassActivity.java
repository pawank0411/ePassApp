package com.example.epassapp.Activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.epassapp.MainActivity;
import com.example.epassapp.Model.Pass;
import com.example.epassapp.Model.User;
import com.example.epassapp.R;
import com.example.epassapp.adapter.ApprovePassAdapter;
import com.example.epassapp.utilities.AskSignature;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import static com.example.epassapp.utilities.Constants.APPROVER_NAME;
import static com.example.epassapp.utilities.Constants.E_PASSES;
import static com.example.epassapp.utilities.Constants.PASS_ACCEPTED;
import static com.example.epassapp.utilities.Constants.PASS_APPROVED;
import static com.example.epassapp.utilities.Constants.PASS_REJECTED;
import static com.example.epassapp.utilities.Constants.USER_ACCOUNTS;

public class ApprovePassActivity extends AppCompatActivity implements ApprovePassAdapter.OnItemClickListener, NavigationView.OnNavigationItemSelectedListener {
    private RecyclerView recyclerView;
    private ApprovePassAdapter passAdapter;
    private MaterialTextView account_verify;
    private ProgressBar progressBar;
    private ArrayList<Pass> passArrayList = new ArrayList<>();
    private String date, user_name;
    private static final int EXTERNAL_STORAGE_PERMISSION_CODE = 1002;
    private boolean signature_exists;
    private AskSignature askSignature;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_approvepass);

        askPermission();
        askSignature = new AskSignature(this);
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
        date = sdf.format(new Date());

        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.navigation_view);
        account_verify = findViewById(R.id.account_verify);
        recyclerView = findViewById(R.id.recycler_view);
        progressBar = findViewById(R.id.progress);
        passAdapter = new ApprovePassAdapter(this, passArrayList, this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setElevation(4);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Site Supervisor");
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        final CollectionReference usersReference = firestore.collection(USER_ACCOUNTS);
        usersReference.document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    User user = Objects.requireNonNull(task.getResult()).toObject(User.class);
                    if (user != null && user.getIsVerified().equals(PASS_ACCEPTED)) {
                        user_name = user.getUser_name();
                        firestore.collection(E_PASSES).addSnapshotListener((queryDocumentSnapshots, e) -> {
                            passArrayList.clear();
                            if (queryDocumentSnapshots != null) {
                                for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()) {
                                    if (Objects.requireNonNull(snapshot.toObject(Pass.class)).getDate().equals(date)
                                            && !Objects.requireNonNull(snapshot.toObject(Pass.class)).getPass_approved().equals(PASS_ACCEPTED)
                                            && !Objects.requireNonNull(snapshot.toObject(Pass.class)).getPass_approved().equals(PASS_REJECTED))
                                        passArrayList.add(snapshot.toObject(Pass.class));
                                }
                                progressBar.setVisibility(View.GONE);
                            }
                            if (passArrayList.size() == 0) {
                                account_verify.setText("NO PASS!");
                                account_verify.setVisibility(View.VISIBLE);
                                passAdapter.notifyDataSetChanged();
                                return;
                            }
                            account_verify.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                            recyclerView.setLayoutManager(new LinearLayoutManager(ApprovePassActivity.this));
                            recyclerView.setHasFixedSize(true);
                            recyclerView.setAdapter(passAdapter);
                            passAdapter.notifyDataSetChanged();
                        });
                    } else {
                        account_verify.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(ApprovePassActivity.this, "Account Not verified!", Toast.LENGTH_SHORT).show();
                    }

                    StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("signatures/" + user_name + ".png");
                    storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        signature_exists = true;
                    }).addOnFailureListener(e -> {
                        signature_exists = false;
                    });
                }
            }
        }).addOnFailureListener(e -> Toast.makeText(ApprovePassActivity.this, "Something went wrong. Please try again.", Toast.LENGTH_SHORT).show());

    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    public void onRejectClicked(String pass_date, String pass_id) {
        if (!signature_exists) {
            askSignature.GetSignature(user_name);
            signature_exists = true;
        } else {
            DocumentReference documentReference = FirebaseFirestore.getInstance().collection(E_PASSES).document(pass_id);
            documentReference.update(PASS_APPROVED, PASS_REJECTED).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(ApprovePassActivity.this, "Pass Rejected.", Toast.LENGTH_SHORT).show();
                    passAdapter.notifyDataSetChanged();
                }
            }).addOnFailureListener(e -> Toast.makeText(ApprovePassActivity.this, "Something went wrong.", Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    public void onAcceptClicked(String pass_date, final String pass_id) {
        if (!signature_exists) {
            askSignature.GetSignature(user_name);
        } else {
            DocumentReference documentReference = FirebaseFirestore.getInstance().collection(E_PASSES).document(pass_id);
            documentReference.update(PASS_APPROVED, PASS_ACCEPTED).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(ApprovePassActivity.this, "Pass Approved.", Toast.LENGTH_SHORT).show();
                    passAdapter.notifyDataSetChanged();
                    documentReference.update(APPROVER_NAME, user_name);
                }
            }).addOnFailureListener(e -> Toast.makeText(ApprovePassActivity.this, "Something went wrong.", Toast.LENGTH_SHORT).show());
        }
    }

    private void askPermission() {
        if (!hasPermission()) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                new AlertDialog.Builder(this)
                        .setTitle("Permission needed")
                        .setMessage("Please allow to access storage")
                        .setCancelable(false)
                        .setPositiveButton("OK", (dialog, which) -> {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                        Manifest.permission.READ_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_PERMISSION_CODE);
                            }
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> {
                            dialog.dismiss();
                            finish();
                        }).create().show();
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_PERMISSION_CODE);
                }
            }

        }
    }


    private boolean hasPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == EXTERNAL_STORAGE_PERMISSION_CODE) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            } else {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) || !ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    new AlertDialog.Builder(this)
                            .setTitle("Permission needed")
                            .setMessage("Please allow to access storage. Press OK to enable in settings.")
                            .setCancelable(false)
                            .setPositiveButton("OK", (dialog, which) -> {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", getPackageName(), null);
                                intent.setData(uri);
                                startActivityForResult(intent, EXTERNAL_STORAGE_PERMISSION_CODE);
                            })
                            .setNegativeButton("Cancel", (dialog, which) -> {
                                dialog.dismiss();
                                finish();
                            }).create().show();


                } else {

                    new AlertDialog.Builder(this)
                            .setTitle("Permission needed")
                            .setMessage("Please allow to access storage")
                            .setCancelable(false)
                            .setPositiveButton("OK", (dialog, which) -> {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                            Manifest.permission.READ_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_PERMISSION_CODE);
                                }
                            })
                            .setNegativeButton("Cancel", (dialog, which) -> {
                                dialog.dismiss();
                                finish();
                            }).create().show();
                }
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.logout: {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(this, "Signed Out Successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ApprovePassActivity.this, MainActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.active_pass: {
                Intent intent = new Intent(ApprovePassActivity.this, PassActivity.class);
                intent.putExtra("fromWayBridge", true);
                startActivity(intent);
                break;
            }
            case R.id.generate_pass:
                Intent intent = new Intent(ApprovePassActivity.this, GeneratePassActivity.class);
                intent.putExtra("fromSiteInCharge", true);
                startActivity(intent);
                break;
            case R.id.history:
                Toast.makeText(this, "history", Toast.LENGTH_SHORT).show();
                break;
        }
        return false;
    }
}
//    SharedPreferences sharedPreferences = getSharedPreferences(USER_POST, MODE_MULTI_PROCESS);
//        String post = sharedPreferences.getString(USER_POST, null);
//        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//        DatabaseReference userDb = FirebaseDatabase.getInstance().getReference("user").child(Objects.requireNonNull(post))
//                .child(Objects.requireNonNull(user).getUid()).child(USER_VERIFIED);
//        userDb.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                String verified = dataSnapshot.getValue(String.class);
//                if (verified != null && verified.equals("1")) {
//                    DatabaseReference passDb = FirebaseDatabase.getInstance().getReference("epass").child(date);
//                    passDb.addValueEventListener(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                            passArrayList.clear();
//                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                                Pass pass = snapshot.getValue(Pass.class);
//                                if (pass != null && pass.getPass_approved().equals("pending")) {
//                                    pass.setSerial_no(snapshot.child(PASS_SERIALNO).getValue(String.class));
//                                    pass.setMine_no(snapshot.child(PASS_MINENO).getValue(String.class));
//                                    pass.setTruck_no(snapshot.child(PASS_TRUCKNO).getValue(String.class));
//                                    pass.setPit_owner(snapshot.child(PASS_PITOWNER).getValue(String.class));
//                                    pass.setSection_no(snapshot.child(PASS_SECTIONNO).getValue(String.class));
//                                    pass.setBench_no(snapshot.child(PASS_BENCHNO).getValue(String.class));
//                                    pass.setDate(snapshot.child(PASS_DATE).getValue(String.class));
//                                    passArrayList.add(pass);
//                                    passAdapter.notifyDataSetChanged();
//                                }
//                            }
//                            progressBar.setVisibility(View.GONE);
//                            if (passArrayList != null && passArrayList.size() > 0) {
//                                account_verify.setVisibility(View.GONE);
//                                recyclerView.setVisibility(View.VISIBLE);
//                                recyclerView.setLayoutManager(new LinearLayoutManager(ApprovePass.this));
//                                recyclerView.setHasFixedSize(true);
//                                recyclerView.setAdapter(passAdapter);
//                            } else {
//                                Log.d("nopass", "true");
//                                account_verify.setText("NO PASS!");
//                                account_verify.setVisibility(View.VISIBLE);
//                            }
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError databaseError) {
//                            Log.d("DatabaseError", databaseError.getMessage());
//                        }
//                    });
//                } else {
//                    progressBar.setVisibility(View.GONE);
//                    account_verify.setVisibility(View.VISIBLE);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
