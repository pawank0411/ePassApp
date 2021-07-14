package com.example.epassapp.Activity;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.epassapp.MainActivity;
import com.example.epassapp.Model.Pass;
import com.example.epassapp.Model.User;
import com.example.epassapp.R;
import com.example.epassapp.adapter.PassAdapter;
import com.example.epassapp.utilities.Constants;
import com.example.epassapp.utilities.Cryptography;
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
    private final ArrayList<Pass> passInfo = new ArrayList<>();
    public static boolean isSearchBarOpen = false;
    private RecyclerView recyclerView;
    private PassAdapter passAdapter;
    private MaterialTextView account_verify;
    private ProgressBar progressBar;
    private final ArrayList<Pass> passOriginalArrayList = new ArrayList<>();
    private boolean fromWayBridge, fromHistory, fromSiteInCharge;
    private DrawerLayout drawerLayout;
    private String user_phone;

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
        if (bundle != null) {
            fromSiteInCharge = bundle.getBoolean("fromSiteInCharge");
        }
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setElevation(4);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);

        SharedPreferences fromWayPref = this.getSharedPreferences("fromWayPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = fromWayPref.edit();
        editor.putBoolean("fromWayPref", fromWayBridge);
        editor.apply();

        if (!fromHistory && !fromSiteInCharge) {
            drawerLayout = findViewById(R.id.drawer_layout);
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

        if (fromWayPref.getBoolean("fromWayPref", false) || fromSiteInCharge) {
            if (fromHistory) {
                Objects.requireNonNull(getSupportActionBar()).setTitle("History");
            } else {
                if (fromSiteInCharge) {
                    Objects.requireNonNull(getSupportActionBar()).setTitle("Active Passes");
                } else {
                    Objects.requireNonNull(getSupportActionBar()).setTitle("Way Bridge");
                }
            }
            userRef.document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                    .get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    User user = Objects.requireNonNull(task.getResult()).toObject(User.class);
                    if (user != null) {
                        if (user.getIsVerified().equals(PASS_ACCEPTED)) {
                            firebaseFirestore.collection(E_PASSES)
                                    .addSnapshotListener((queryDocumentSnapshots, e) -> {
                                        passInfo.clear();
                                        passOriginalArrayList.clear();
                                        progressBar.setVisibility(View.GONE);
                                        user_phone = Cryptography.decrypt(user.getUser_phone());
                                        if (queryDocumentSnapshots != null) {
                                            if (!fromHistory) {
                                                for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()) {
                                                    if (Objects.requireNonNull(snapshot.toObject(Pass.class)).getDate().equals(Constants.date)
                                                            && Objects.requireNonNull(snapshot.toObject(Pass.class)).getPass_approved().equals(PASS_ACCEPTED)
                                                            && !Objects.requireNonNull(snapshot.toObject(Pass.class)).getPass_approved().equals(PASS_REJECTED)) {
                                                        passInfo.add(snapshot.toObject(Pass.class));
                                                        passOriginalArrayList.add(snapshot.toObject(Pass.class));
                                                    }
                                                }
                                            } else {
                                                for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()) {
                                                    if (Objects.requireNonNull(snapshot.toObject(Pass.class)).getPass_approved().equals(PASS_ACCEPTED)) {
                                                        passInfo.add(snapshot.toObject(Pass.class));
                                                        passOriginalArrayList.add(snapshot.toObject(Pass.class));
                                                    }
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
            if (!fromHistory) {
                Objects.requireNonNull(getSupportActionBar()).setTitle("Truck Owner");
            } else {
                Objects.requireNonNull(getSupportActionBar()).setTitle("History");
            }
            userRef.document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                    .get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    User user = Objects.requireNonNull(task.getResult()).toObject(User.class);
                    if (user != null) {
                        if (user.getIsVerified().equals(PASS_ACCEPTED)) {
                            firebaseFirestore.collection(E_PASSES)
                                    .whereEqualTo(PASS_TRUCKNO, user.getTruck_number())
                                    .addSnapshotListener((queryDocumentSnapshots, e) -> {
                                passInfo.clear();
                                passOriginalArrayList.clear();
                                progressBar.setVisibility(View.GONE);
                                user_phone = Cryptography.decrypt(user.getUser_phone());

                                if (queryDocumentSnapshots != null) {
                                    if (!fromHistory) {
                                        for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()) {
                                            if (Objects.requireNonNull(snapshot.toObject(Pass.class)).getDate().equals(Constants.date) &&
                                                    Objects.requireNonNull(snapshot.toObject(Pass.class)).getPass_approved().equals(PASS_ACCEPTED)) {
                                                passInfo.add(snapshot.toObject(Pass.class));
                                                passOriginalArrayList.add(snapshot.toObject(Pass.class));
                                            }
                                        }
                                    } else {
                                        for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()) {
                                            if (Objects.requireNonNull(snapshot.toObject(Pass.class)).getPass_approved().equals(PASS_ACCEPTED)) {
                                                passInfo.add(snapshot.toObject(Pass.class));
                                                passOriginalArrayList.add(snapshot.toObject(Pass.class));
                                            }
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
                SharedPreferences fromWayPref = this.getSharedPreferences("fromWayPref", MODE_PRIVATE);
                FirebaseAuth.getInstance().signOut();
                fromWayPref.edit().clear().apply();
                Toast.makeText(this, "Signed Out Successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(PassActivity.this, MainActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.history: {
                Intent intent = new Intent(PassActivity.this, PassActivity.class);
                intent.putExtra("HfromWayBridge", true);
                intent.putExtra("HfromTruck", true);
                intent.putExtra("fromHistory", true);
                startActivity(intent);
                break;
            }
            case R.id.delete: {
                new AlertDialog.Builder(this)
                        .setTitle(Html.fromHtml("<font color=\"#CA0B0B\">Delete Account</font>"))
                        .setMessage("Are you sure you want to delete this account?")
                        .setCancelable(false)
                        .setPositiveButton("YES", (dialog, which) -> {
                            Intent intent = new Intent(PassActivity.this, VerifyOTP.class);
                            intent.putExtra("deleteUser", true);
                            intent.putExtra("user_phone", user_phone);
                            startActivity(intent);
                        }).setNegativeButton("NO", (dialog, which) -> {
                    dialog.dismiss();
                }).show();
            }
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_searchbar, menu);
        MenuItem searchItem = menu.findItem(R.id.search_item);

        SearchManager searchManager = (SearchManager) this.getSystemService(Context.SEARCH_SERVICE);

        SearchView searchView = new SearchView(Objects.requireNonNull((this).getSupportActionBar()).getThemedContext());
        searchItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW | MenuItem.SHOW_AS_ACTION_IF_ROOM);
        searchView.setQueryHint("Search by contractor or truck number...");
        searchItem.setActionView(searchView);
        if (searchManager != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(this.getComponentName()));
        }
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                passInfo.clear();
                for (Pass pass : passOriginalArrayList) {
                    if (pass.getTruck_no().contains(newText.trim())
                            || pass.getContractor_name().toLowerCase().contains(newText.trim())) {
                        passInfo.add(pass);
                    }
                }
                passAdapter.notifyDataSetChanged();
                return true;
            }
        });

        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                isSearchBarOpen = true;
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                isSearchBarOpen = false;
                return true;
            }
        });
        return true;
    }

    @Override
    public void onBackPressed() {
        if (!fromHistory && !fromSiteInCharge) {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                moveTaskToBack(true);
            }
        } else {
            finish();
        }
    }

}
