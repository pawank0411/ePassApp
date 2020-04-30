package com.example.epassapp.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.epassapp.Model.User;
import com.example.epassapp.R;
import com.example.epassapp.adapter.ApproveAccountAdapter;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Objects;

import static com.example.epassapp.utilities.Constants.PASS_ACCEPTED;
import static com.example.epassapp.utilities.Constants.PASS_APPROVED;
import static com.example.epassapp.utilities.Constants.PASS_REJECTED;
import static com.example.epassapp.utilities.Constants.USER_ACCOUNTS;

public class ApproveAccount extends AppCompatActivity implements ApproveAccountAdapter.OnItemClickListener {
    private ArrayList<User> accountArrayList = new ArrayList<>();
    private ProgressBar progressBar;
    private ApproveAccountAdapter approveAccountAdapter;
    private RecyclerView recyclerView;
    private MaterialTextView account_verify;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        progressBar = findViewById(R.id.progress);
        recyclerView = findViewById(R.id.recycler_view);
        account_verify = findViewById(R.id.account_verify);
        approveAccountAdapter = new ApproveAccountAdapter(accountArrayList, this);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setElevation(4);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Approve Account");
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);

        FirebaseFirestore.getInstance().collection(USER_ACCOUNTS)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    accountArrayList.clear();
                    progressBar.setVisibility(View.GONE);

                    if (queryDocumentSnapshots != null) {
                        for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()) {
                            if (!Objects.requireNonNull(snapshot.toObject(User.class)).getIsVerified().equals(PASS_ACCEPTED)
                                    && !Objects.requireNonNull(snapshot.toObject(User.class)).getIsVerified().equals(PASS_REJECTED))
                                accountArrayList.add(snapshot.toObject(User.class));
                        }
                    }

                    if (accountArrayList.size() == 0) {
                        account_verify.setText("NO APPROVE REQUEST!");
                        account_verify.setVisibility(View.VISIBLE);
                        approveAccountAdapter.notifyDataSetChanged();
                        return;
                    }

                    recyclerView.setVisibility(View.VISIBLE);
                    recyclerView.setLayoutManager(new LinearLayoutManager(ApproveAccount.this));
                    recyclerView.setHasFixedSize(true);
                    recyclerView.setAdapter(approveAccountAdapter);
                    approveAccountAdapter.notifyDataSetChanged();
                });
    }

    @Override
    public void OnApproveAccount(String id) {
        DocumentReference documentReference = FirebaseFirestore.getInstance().collection(USER_ACCOUNTS).document(id);
        documentReference.update("isVerified", PASS_ACCEPTED).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(ApproveAccount.this, "Account Approved", Toast.LENGTH_SHORT).show();
                approveAccountAdapter.notifyDataSetChanged();
            }
        }).addOnFailureListener(e -> Toast.makeText(ApproveAccount.this, "Something went wrong.", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void OnRejectAccount(String id) {
        DocumentReference documentReference = FirebaseFirestore.getInstance().collection(USER_ACCOUNTS).document(id);
        documentReference.update("isVerified", PASS_REJECTED).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(ApproveAccount.this, "Account Rejected", Toast.LENGTH_SHORT).show();
                approveAccountAdapter.notifyDataSetChanged();
            }
        }).addOnFailureListener(e -> Toast.makeText(ApproveAccount.this, "Something went wrong.", Toast.LENGTH_SHORT).show());
    }
}
