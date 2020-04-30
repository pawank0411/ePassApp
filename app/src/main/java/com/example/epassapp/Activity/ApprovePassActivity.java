package com.example.epassapp.Activity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.epassapp.MainActivity;
import com.example.epassapp.Model.Pass;
import com.example.epassapp.Model.User;
import com.example.epassapp.R;
import com.example.epassapp.adapter.ApprovePassAdapter;
import com.example.epassapp.utilities.AskSignature;
import com.example.epassapp.utilities.Constants;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
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
    public static boolean isSearchBarOpen = false;
    private String user_name, user_phone;
    private static final int EXTERNAL_STORAGE_PERMISSION_CODE = 1002;
    private boolean signature_exists;
    private AskSignature askSignature;
    private boolean fromHistory;
    private ArrayList<Pass> passOriginalArrayList = new ArrayList<>();
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_approvepass);

        askPermission();
        askSignature = new AskSignature(this);
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.navigation_view);
        account_verify = findViewById(R.id.account_verify);
        recyclerView = findViewById(R.id.recycler_view);
        progressBar = findViewById(R.id.progress);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            fromHistory = bundle.getBoolean("fromHistory");
        }
        passAdapter = new ApprovePassAdapter(this, passArrayList, fromHistory, this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setElevation(4);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);

        final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        final CollectionReference usersReference = firestore.collection(USER_ACCOUNTS);

        if (!fromHistory) {
            Objects.requireNonNull(getSupportActionBar()).setTitle("Site Supervisor");
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();
            navigationView.setNavigationItemSelectedListener(this);
        } else {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            Objects.requireNonNull(getSupportActionBar()).setTitle("History");
        }
        usersReference.document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                User user = Objects.requireNonNull(task.getResult()).toObject(User.class);
                if (user != null && user.getIsVerified().equals(PASS_ACCEPTED)) {
                    user_name = user.getUser_name();
                    user_phone = user.getUser_phone();
                    firestore.collection(E_PASSES).addSnapshotListener((queryDocumentSnapshots, e) -> {
                        passArrayList.clear();
                        passOriginalArrayList.clear();
                        if (queryDocumentSnapshots != null) {
                            if (!fromHistory) {
                                for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()) {
                                    if (Objects.requireNonNull(snapshot.toObject(Pass.class)).getDate().equals(Constants.date)
                                            && !Objects.requireNonNull(snapshot.toObject(Pass.class)).getPass_approved().equals(PASS_ACCEPTED)
                                            && !Objects.requireNonNull(snapshot.toObject(Pass.class)).getPass_approved().equals(PASS_REJECTED)) {
                                        passArrayList.add(snapshot.toObject(Pass.class));
                                        passOriginalArrayList.add(snapshot.toObject(Pass.class));
                                    }
                                }
                            } else {
                                for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()) {
                                    if (Objects.requireNonNull(snapshot.toObject(Pass.class)).getPass_approved().equals(PASS_ACCEPTED)) {
                                        passArrayList.add(snapshot.toObject(Pass.class));
                                        passOriginalArrayList.add(snapshot.toObject(Pass.class));
                                    }
                                }
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
                storageReference.getDownloadUrl().addOnSuccessListener(uri -> signature_exists = true).addOnFailureListener(e -> signature_exists = false);
            }
        }).addOnFailureListener(e -> Toast.makeText(ApprovePassActivity.this, "Something went wrong. Please try again.", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
            return;
        }
        if (!fromHistory) {
            moveTaskToBack(true);
        } else {
            super.onBackPressed();
            finish();
        }
    }

    @Override
    public void onRejectClicked(String pass_date, String pass_id) {
        if (!signature_exists && !askSignature.isSuccessfullySaved) {
            askSignature.GetSignature(user_name);
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
        if (!signature_exists && !askSignature.isSuccessfullySaved) {
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
                Log.d("permission", "1");
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
                Intent intent = new Intent(ApprovePassActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                break;
            }
            case R.id.active_pass: {
                Intent intent = new Intent(ApprovePassActivity.this, PassActivity.class);
                intent.putExtra("fromSiteInCharge", true);
                startActivity(intent);
                break;
            }
            case R.id.generate_pass: {
                Intent intent = new Intent(ApprovePassActivity.this, GeneratePassActivity.class);
                intent.putExtra("fromSiteInCharge", true);
                startActivity(intent);
                break;
            }
            case R.id.history: {
                Intent intent = new Intent(ApprovePassActivity.this, ApprovePassActivity.class);
                intent.putExtra("fromHistory", true);
                startActivity(intent);
                break;
            }
//            case R.id.approve : {
//                Intent intent = new Intent(ApprovePassActivity.this, ApproveAccount.class);
//                startActivity(intent);
//                break;
//            }
            case R.id.delete: {
                new AlertDialog.Builder(this)
                        .setTitle(Html.fromHtml("<font color=\"#CA0B0B\">Delete Account</font>"))
                        .setMessage("Are you sure you want to delete this account?")
                        .setCancelable(false)
                        .setPositiveButton("YES", (dialog, which) -> {
                            Intent intent = new Intent(ApprovePassActivity.this, VerifyOTP.class);
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
                passArrayList.clear();
                for (Pass pass : passOriginalArrayList) {
                    if (pass.getTruck_no().contains(newText.trim())
                            || pass.getContractor_name().toLowerCase().contains(newText.trim())) {
                        passArrayList.add(pass);
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
}