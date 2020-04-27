package com.example.epassapp.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.epassapp.Model.Pass;
import com.example.epassapp.Model.User;
import com.example.epassapp.R;
import com.example.epassapp.adapter.PassAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import static com.example.epassapp.utilities.Constants.E_PASSES;
import static com.example.epassapp.utilities.Constants.PASS_ACCEPTED;
import static com.example.epassapp.utilities.Constants.PASS_REJECTED;
import static com.example.epassapp.utilities.Constants.PASS_TRUCKNO;
import static com.example.epassapp.utilities.Constants.USER_ACCOUNTS;

public class PassActivity extends AppCompatActivity {
    private String date;
    private ArrayList<Pass> passInfo = new ArrayList<>();
    private RecyclerView recyclerView;
    private PassAdapter passAdapter;
    private MaterialTextView account_verify;
    private ProgressBar progressBar;
    private boolean fromWayBridge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pass);

        recyclerView = findViewById(R.id.recycler_view);
        account_verify = findViewById(R.id.account_verify);
        progressBar = findViewById(R.id.progress);

        passAdapter = new PassAdapter(PassActivity.this, passInfo);

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
        date = sdf.format(new Date());

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            fromWayBridge = bundle.getBoolean("fromWayBridge");
        }

        final FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        CollectionReference userRef = firebaseFirestore.collection(USER_ACCOUNTS);

        if (fromWayBridge) {
            userRef.document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                    .get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            User user = Objects.requireNonNull(task.getResult()).toObject(User.class);
                            if (user != null) {
                                if (user.getIsVerified().equals(PASS_ACCEPTED)) {
                                    firebaseFirestore.collection(E_PASSES)
                                            .addSnapshotListener((queryDocumentSnapshots, e) -> {
                                                passInfo.clear();
                                                progressBar.setVisibility(View.GONE);

                                                for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()) {
                                                    if (snapshot.toObject(Pass.class).getDate().equals(date)
                                                            && Objects.requireNonNull(snapshot.toObject(Pass.class)).getPass_approved().equals(PASS_ACCEPTED)
                                                            && !Objects.requireNonNull(snapshot.toObject(Pass.class)).getPass_approved().equals(PASS_REJECTED))
                                                        passInfo.add(snapshot.toObject(Pass.class));
                                                }
                                                if (passInfo.size() == 0) {
                                                    account_verify.setText("NO PASS!");
                                                    account_verify.setVisibility(View.VISIBLE);
                                                    passAdapter.notifyDataSetChanged();
                                                    return;
                                                }
                                                account_verify.setVisibility(View.GONE);
                                                recyclerView.setVisibility(View.VISIBLE);
                                                recyclerView.setLayoutManager(new LinearLayoutManager(PassActivity.this));
                                                recyclerView.setHasFixedSize(true);
                                                recyclerView.setAdapter(passAdapter);
                                                passAdapter.notifyDataSetChanged();
                                            });
                                } else {
                                    account_verify.setVisibility(View.VISIBLE);
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(PassActivity.this, "Account Not verified!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }).addOnFailureListener(e -> Toast.makeText(PassActivity.this, "Something went wrong.", Toast.LENGTH_SHORT).show());
        } else {
            userRef.document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                    .get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            User user = Objects.requireNonNull(task.getResult()).toObject(User.class);
                            if (user != null) {
                                if (user.getIsVerified().equals(PASS_ACCEPTED)) {
                                    firebaseFirestore.collection(E_PASSES)
                                            .whereEqualTo(PASS_TRUCKNO, user.getTruck_number()).addSnapshotListener((queryDocumentSnapshots, e) -> {
                                        passInfo.clear();
                                        progressBar.setVisibility(View.GONE);

                                        if (queryDocumentSnapshots != null) {
                                            for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()) {
                                                if (Objects.requireNonNull(snapshot.toObject(Pass.class)).getDate().equals(date) &&
                                                        Objects.requireNonNull(snapshot.toObject(Pass.class)).getPass_approved().equals(PASS_ACCEPTED))
                                                    passInfo.add(snapshot.toObject(Pass.class));
                                            }
                                        }
                                        if (passInfo.size() == 0) {
                                            account_verify.setText("NO PASS!");
                                            account_verify.setVisibility(View.VISIBLE);
                                            passAdapter.notifyDataSetChanged();
                                            return;
                                        }
                                        account_verify.setVisibility(View.GONE);
                                        recyclerView.setVisibility(View.VISIBLE);
                                        recyclerView.setLayoutManager(new LinearLayoutManager(PassActivity.this));
                                        recyclerView.setHasFixedSize(true);
                                        recyclerView.setAdapter(passAdapter);
                                        passAdapter.notifyDataSetChanged();
                                    });
                                } else {
                                    account_verify.setVisibility(View.VISIBLE);
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(PassActivity.this, "Account Not verified!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }).addOnFailureListener(e -> Toast.makeText(PassActivity.this, "Something went wrong.", Toast.LENGTH_SHORT).show());
        }

//
//        SharedPreferences sharedPreferences = getSharedPreferences(USER_POST, MODE_MULTI_PROCESS);
//        String post = sharedPreferences.getString(USER_POST, null);
//        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//        DatabaseReference db1 = FirebaseDatabase.getInstance().getReference("user").child(Objects.requireNonNull(post))
//                .child(Objects.requireNonNull(user).getUid());
//        db1.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                User snapshot = dataSnapshot.getValue(User.class);
//                if (snapshot != null) {
//                    if (snapshot.getIsVerified().equals("1")) {
//                        passInfo.clear();
//                        DatabaseReference passDb = FirebaseDatabase.getInstance().getReference("epass").child(date);
//                        if (fromWayBridge) {
//                            passDb.addValueEventListener(new ValueEventListener() {
//                                @Override
//                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                                    for (DataSnapshot shot : dataSnapshot.getChildren()) {
//                                        Pass passinfo = shot.getValue(Pass.class);
//                                        if (passinfo != null && passinfo.getPass_approved().equals(PASS_ACCEPTED)) {
//                                            passinfo.setSerial_no(shot.child(PASS_SERIALNO).getValue(String.class));
//                                            passinfo.setMine_no(shot.child(PASS_MINENO).getValue(String.class));
//                                            passinfo.setTruck_no(shot.child(PASS_TRUCKNO).getValue(String.class));
//                                            passinfo.setPit_owner(shot.child(PASS_PITOWNER).getValue(String.class));
//                                            passinfo.setSection_no(shot.child(PASS_SECTIONNO).getValue(String.class));
//                                            passinfo.setBench_no(shot.child(PASS_BENCHNO).getValue(String.class));
//                                            passinfo.setDate(shot.child(PASS_DATE).getValue(String.class));
//                                            passInfo.add(passinfo);
//                                            passAdapter.notifyDataSetChanged();
//                                        }
//                                    }
//                                    progressBar.setVisibility(View.GONE);
//                                    if (passInfo != null && passInfo.size() > 0) {
//                                        account_verify.setVisibility(View.GONE);
//                                        recyclerView.setVisibility(View.VISIBLE);
//                                        recyclerView.setLayoutManager(new LinearLayoutManager(TruckPassActivity.this));
//                                        recyclerView.setHasFixedSize(true);
//                                        recyclerView.setAdapter(passAdapter);
//                                    } else {
//                                        Log.d("nopass", "true");
//                                        account_verify.setText("NO PASS!");
//                                        account_verify.setVisibility(View.VISIBLE);
//                                    }
//                                }
//
//                                @Override
//                                public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                                }
//                            });
//                        } else {
//                            passDb.orderByChild(PASS_TRUCKNO).equalTo(snapshot.getTruck_number()).addValueEventListener(new ValueEventListener() {
//                                @Override
//                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                                    for (DataSnapshot shot : dataSnapshot.getChildren()) {
//                                        Pass passinfo = shot.getValue(Pass.class);
//                                        if (passinfo != null && passinfo.getPass_approved().equals(PASS_ACCEPTED)) {
//                                            passinfo.setSerial_no(shot.child(PASS_SERIALNO).getValue(String.class));
//                                            passinfo.setMine_no(shot.child(PASS_MINENO).getValue(String.class));
//                                            passinfo.setTruck_no(shot.child(PASS_TRUCKNO).getValue(String.class));
//                                            passinfo.setPit_owner(shot.child(PASS_PITOWNER).getValue(String.class));
//                                            passinfo.setSection_no(shot.child(PASS_SECTIONNO).getValue(String.class));
//                                            passinfo.setBench_no(shot.child(PASS_BENCHNO).getValue(String.class));
//                                            passinfo.setDate(shot.child(PASS_DATE).getValue(String.class));
//                                            passInfo.add(passinfo);
//                                            passAdapter.notifyDataSetChanged();
//                                        }
//                                    }
//                                    progressBar.setVisibility(View.GONE);
//                                    if (passInfo != null && passInfo.size() > 0) {
//                                        account_verify.setVisibility(View.GONE);
//                                        recyclerView.setVisibility(View.VISIBLE);
//                                        recyclerView.setLayoutManager(new LinearLayoutManager(TruckPassActivity.this));
//                                        recyclerView.setHasFixedSize(true);
//                                        recyclerView.setAdapter(passAdapter);
//                                    } else {
//                                        Log.d("nopass", "true");
//                                        account_verify.setText("NO PASS!");
//                                        account_verify.setVisibility(View.VISIBLE);
//                                    }
//                                }
//
//                                @Override
//                                public void onCancelled(@NonNull DatabaseError databaseError) {
//                                    Log.d("DatabaseError", databaseError.getMessage());
//                                }
//                            });
//                        }
//                    } else {
//                        account_verify.setVisibility(View.VISIBLE);
//                        progressBar.setVisibility(View.GONE);
//                        Toast.makeText(TruckPassActivity.this, "Account Not verified!", Toast.LENGTH_SHORT).show();
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });

    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}
