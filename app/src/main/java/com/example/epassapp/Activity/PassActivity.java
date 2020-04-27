package com.example.epassapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.epassapp.MainActivity;
import com.example.epassapp.Model.Pass;
import com.example.epassapp.Model.User;
import com.example.epassapp.R;
import com.example.epassapp.adapter.PassAdapter;
import com.example.epassapp.utilities.Constants;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Objects;

import static com.example.epassapp.utilities.Constants.E_PASSES;
import static com.example.epassapp.utilities.Constants.PASS_ACCEPTED;
import static com.example.epassapp.utilities.Constants.PASS_REJECTED;
import static com.example.epassapp.utilities.Constants.PASS_TRUCKNO;
import static com.example.epassapp.utilities.Constants.USER_ACCOUNTS;

public class PassActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private ArrayList<Pass> passInfo = new ArrayList<>();
    private RecyclerView recyclerView;
    private PassAdapter passAdapter;
    private MaterialTextView account_verify;
    private ProgressBar progressBar;
    private boolean fromWayBridge, fromHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_approvepass);

        recyclerView = findViewById(R.id.recycler_view);
        account_verify = findViewById(R.id.account_verify);
        progressBar = findViewById(R.id.progress);

        passAdapter = new PassAdapter(PassActivity.this, passInfo);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            fromWayBridge = bundle.getBoolean("fromWayBridge");
        }
        if (bundle != null) {
            fromHistory = bundle.getBoolean("fromHistory");
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setElevation(4);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);

        if (!fromHistory) {
            DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
            NavigationView navigationView = findViewById(R.id.navigation_view);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();
            navigationView.setNavigationItemSelectedListener(this);
            navigationView.getMenu().clear();
            navigationView.inflateMenu(R.menu.navigation_menu_normal);
        }


        final FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        CollectionReference userRef = firebaseFirestore.collection(USER_ACCOUNTS);

        if (fromWayBridge) {
            Objects.requireNonNull(getSupportActionBar()).setTitle("Way Bridge");
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

                                        if (queryDocumentSnapshots != null) {
                                            if (!fromHistory) {
                                                for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()) {
                                                    if (Objects.requireNonNull(snapshot.toObject(Pass.class)).getDate().equals(Constants.date)
                                                            && Objects.requireNonNull(snapshot.toObject(Pass.class)).getPass_approved().equals(PASS_ACCEPTED)
                                                            && !Objects.requireNonNull(snapshot.toObject(Pass.class)).getPass_approved().equals(PASS_REJECTED))
                                                        passInfo.add(snapshot.toObject(Pass.class));
                                                }
                                            } else {
                                                for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()) {
                                                    if (Objects.requireNonNull(snapshot.toObject(Pass.class)).getPass_approved().equals(PASS_ACCEPTED))
                                                        passInfo.add(snapshot.toObject(Pass.class));
                                                }
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
        } else {
            Objects.requireNonNull(getSupportActionBar()).setTitle("Truck Owner");
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
                                    if (!fromHistory) {
                                        for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()) {
                                            if (Objects.requireNonNull(snapshot.toObject(Pass.class)).getDate().equals(Constants.date) &&
                                                    Objects.requireNonNull(snapshot.toObject(Pass.class)).getPass_approved().equals(PASS_ACCEPTED))
                                                passInfo.add(snapshot.toObject(Pass.class));
                                        }
                                    } else {
                                        for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()) {
                                            if (Objects.requireNonNull(snapshot.toObject(Pass.class)).getPass_approved().equals(PASS_ACCEPTED))
                                                passInfo.add(snapshot.toObject(Pass.class));
                                        }
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
                Intent intent = new Intent(PassActivity.this, MainActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.history: {
                Intent intent = new Intent(PassActivity.this, PassActivity.class);
                intent.putExtra("fromHistory", true);
                startActivity(intent);
                break;
            }
        }
        return false;
    }
}
