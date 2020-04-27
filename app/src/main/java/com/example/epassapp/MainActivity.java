package com.example.epassapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.epassapp.Activity.ApprovePassActivity;
import com.example.epassapp.Activity.GeneratePassActivity;
import com.example.epassapp.Activity.IndividualPassActivity;
import com.example.epassapp.Activity.LoginActivity;
import com.example.epassapp.Activity.PassActivity;
import com.example.epassapp.Model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

import static com.example.epassapp.utilities.Constants.POST_CONTRACTOR;
import static com.example.epassapp.utilities.Constants.POST_PITOWNER;
import static com.example.epassapp.utilities.Constants.POST_SITEINCHARGE;
import static com.example.epassapp.utilities.Constants.POST_TRUCKDRIVER;
import static com.example.epassapp.utilities.Constants.POST_WAYBRIDGE;
import static com.example.epassapp.utilities.Constants.USER_ACCOUNTS;

public class MainActivity extends AppCompatActivity {
    private String post;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            FirebaseFirestore.getInstance().collection(USER_ACCOUNTS).document(FirebaseAuth.getInstance().getCurrentUser().getUid()).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            User user = Objects.requireNonNull(task.getResult()).toObject(User.class);
                            if (user != null) {
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
                                Toast.makeText(this, "no user", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).addOnFailureListener(e -> {
                Log.d("error_main", e.getMessage());
                Toast.makeText(MainActivity.this, "Something went wrong. Try Again", Toast.LENGTH_SHORT).show();
            });
        } else {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        }
    }
}
