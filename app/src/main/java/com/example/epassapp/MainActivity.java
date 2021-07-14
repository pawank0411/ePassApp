package com.example.epassapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.epassapp.Activity.ApprovePassActivity;
import com.example.epassapp.Activity.GeneratePassActivity;
import com.example.epassapp.Activity.IndividualPassActivity;
import com.example.epassapp.Activity.LoginActivity;
import com.example.epassapp.Activity.PassActivity;
import com.example.epassapp.Model.User;
import com.example.epassapp.utilities.AppStatus;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

import static com.example.epassapp.utilities.Constants.PASS_ACCEPTED;
import static com.example.epassapp.utilities.Constants.POST_CONTRACTOR;
import static com.example.epassapp.utilities.Constants.POST_PITOWNER;
import static com.example.epassapp.utilities.Constants.POST_SITEINCHARGE;
import static com.example.epassapp.utilities.Constants.POST_TRUCKDRIVER;
import static com.example.epassapp.utilities.Constants.POST_WAYBRIDGE;
import static com.example.epassapp.utilities.Constants.USER_ACCOUNTS;
import static com.example.epassapp.utilities.Constants.USER_NAME;

public class MainActivity extends AppCompatActivity {
    private String post;
    private ProgressBar progressBar;
    private MaterialTextView account_verify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = findViewById(R.id.progress);
        account_verify = findViewById(R.id.account_verify);
        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.simpleSwipeRefreshLayout);
        AppStatus appStatus = new AppStatus(this);
        if (!appStatus.isOnline()) {
            Toast.makeText(this, "Please make sure you have active internet connection!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            assignposts();
            swipeRefreshLayout.setOnRefreshListener(this::recreate);
        }
    }


    public void assignposts() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            Log.d("user_id", FirebaseAuth.getInstance().getCurrentUser().getUid());
            FirebaseFirestore.getInstance().collection(USER_ACCOUNTS).document(FirebaseAuth.getInstance().getCurrentUser().getUid()).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            User user = Objects.requireNonNull(task.getResult()).toObject(User.class);
                            if (user != null && user.getIsVerified().equals(PASS_ACCEPTED)) {
                                post = user.getUser_post();
                                if (post != null) {
                                    switch (post) {
                                        case POST_CONTRACTOR: {
                                            Intent intent = new Intent(MainActivity.this, GeneratePassActivity.class);
                                            startActivity(intent);
                                            break;
                                        }
                                        case POST_WAYBRIDGE: {
                                            Intent intent = new Intent(MainActivity.this, PassActivity.class);
                                            intent.putExtra("fromWayBridge", true);
                                            startActivity(intent);
                                            break;
                                        }
                                        case POST_SITEINCHARGE: {
                                            Intent intent = new Intent(MainActivity.this, ApprovePassActivity.class);
                                            intent.putExtra(USER_NAME, user.getUser_name());
                                            startActivity(intent);
                                            break;
                                        }
                                        case POST_TRUCKDRIVER: {
                                            Intent intent = new Intent(MainActivity.this, PassActivity.class);
                                            startActivity(intent);
                                            break;
                                        }
                                        case POST_PITOWNER: {
                                            Intent intent = new Intent(MainActivity.this, IndividualPassActivity.class);
                                            intent.putExtra("fromPitOwner", true);
                                            startActivity(intent);
                                            break;
                                        }
                                        default:
                                            Toast.makeText(this, "Post not exists", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(this, "no post", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                progressBar.setVisibility(View.GONE);
                                account_verify.setVisibility(View.VISIBLE);
                            }
                        }
                    }).addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Something went wrong. Try Again", Toast.LENGTH_SHORT).show());
        } else {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            Bundle bundle = getIntent().getExtras();
            if (bundle != null && bundle.getBoolean("fromLoginActivity"))
                intent.putExtra("fromMainActivity", true);
            intent.putExtra("fromMain",true);
            startActivity(intent);
        }
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        assignposts();
    }
}
