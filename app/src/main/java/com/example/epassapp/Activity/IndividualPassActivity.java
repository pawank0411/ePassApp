package com.example.epassapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
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
import static com.example.epassapp.utilities.Constants.PASS_CONTRACTOR;
import static com.example.epassapp.utilities.Constants.PASS_PITOWNER;
import static com.example.epassapp.utilities.Constants.PASS_REJECTED;
import static com.example.epassapp.utilities.Constants.USER_ACCOUNTS;

public class IndividualPassActivity extends ApprovePassActivity implements NavigationView.OnNavigationItemSelectedListener {
    private String user_name;
    private ArrayList<Pass> passInfo = new ArrayList<>();
    private RecyclerView recyclerView;
    private PassAdapter passAdapter;
    private MaterialTextView account_verify;
    private ProgressBar progressBar;
    private boolean fromPitOwner, fromHistory;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.getBoolean("fromPitOwner")) {
            setContentView(R.layout.activity_approvepass);
            fromPitOwner = true;
        } else
            setContentView(R.layout.activity_pass);

        recyclerView = findViewById(R.id.recycler_view);
        progressBar = findViewById(R.id.progress);
        account_verify = findViewById(R.id.account_verify);

        passAdapter = new PassAdapter(IndividualPassActivity.this, passInfo);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setElevation(4);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);

        if (bundle != null) {
            user_name = bundle.getString("CurrentContractorName");
        }
        if (bundle != null) {
            fromHistory = bundle.getBoolean("fromHistory");
        }

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        CollectionReference contractorpassRef = firebaseFirestore.collection(E_PASSES);

        if (!fromPitOwner) {
            if (!fromHistory) {
                Objects.requireNonNull(getSupportActionBar()).setTitle("Active Passes");
            } else {
                Objects.requireNonNull(getSupportActionBar()).setTitle("History");
            }
            contractorpassRef.whereEqualTo(PASS_CONTRACTOR, user_name).addSnapshotListener((queryDocumentSnapshots, e) -> {
                passInfo.clear();
                progressBar.setVisibility(View.GONE);

                if (queryDocumentSnapshots != null) {
                    if (!fromHistory) {
                        for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()) {
                            if (Objects.requireNonNull(snapshot.toObject(Pass.class)).getDate().equals(Constants.date) &&
                                    Objects.requireNonNull(snapshot.toObject(Pass.class)).getPass_approved().equals(PASS_ACCEPTED)
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
                recyclerView.setLayoutManager(new LinearLayoutManager(IndividualPassActivity.this));
                recyclerView.setHasFixedSize(true);
                recyclerView.setAdapter(passAdapter);
                passAdapter.notifyDataSetChanged();
            });
        } else {
            if (!fromHistory) {
                Objects.requireNonNull(getSupportActionBar()).setTitle("Pit Owner");
                DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
                NavigationView navigationView = findViewById(R.id.navigation_view);
                ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
                drawerLayout.addDrawerListener(toggle);
                toggle.syncState();
                navigationView.setNavigationItemSelectedListener(this);
                navigationView.getMenu().clear();
                navigationView.inflateMenu(R.menu.navigation_menu_normal);
            } else {
                Objects.requireNonNull(getSupportActionBar()).setTitle("History");
            }

            firebaseFirestore.collection(USER_ACCOUNTS)
                    .document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            contractorpassRef.whereEqualTo(PASS_PITOWNER,
                                    Objects.requireNonNull(Objects.requireNonNull(task.getResult()).toObject(User.class))
                                            .getUser_name())
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
                                        recyclerView.setLayoutManager(new LinearLayoutManager(IndividualPassActivity.this));
                                        recyclerView.setHasFixedSize(true);
                                        recyclerView.setAdapter(passAdapter);
                                        passAdapter.notifyDataSetChanged();
                                    });
                        }
                    }).addOnFailureListener(e -> {

            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home && !fromPitOwner || fromHistory) {
            finish();
        } else {
            switch (id) {
                case R.id.logout: {
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(IndividualPassActivity.this, MainActivity.class);
                    startActivity(intent);
                    break;
                }
                case R.id.history: {
                    Intent intent = new Intent(IndividualPassActivity.this, IndividualPassActivity.class);
                    intent.putExtra("fromHistory", true);
                    startActivity(intent);
                    break;
                }
            }
        }
        return true;
    }
}
