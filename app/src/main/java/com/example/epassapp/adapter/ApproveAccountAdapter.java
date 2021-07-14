package com.example.epassapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.epassapp.Model.User;
import com.example.epassapp.R;
import com.example.epassapp.utilities.Cryptography;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;

public class ApproveAccountAdapter extends RecyclerView.Adapter<ApproveAccountAdapter.ViewHolder> {
    private final OnItemClickListener onItemClickListener;
    private final ArrayList<User> accountArrayList;

    public ApproveAccountAdapter(ArrayList<User> accountArrayList, OnItemClickListener onItemClickListener) {
        this.accountArrayList = accountArrayList;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ApproveAccountAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.account_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ApproveAccountAdapter.ViewHolder holder, int position) {
        holder.reject.setOnClickListener(view -> {
            if (onItemClickListener != null)
                onItemClickListener.OnRejectAccount(accountArrayList.get(position).getUser_id());
        });

        holder.accept.setOnClickListener(view -> {
            if (onItemClickListener != null)
                onItemClickListener.OnApproveAccount(accountArrayList.get(position).getUser_id());
        });

        holder.username.setText(accountArrayList.get(position).getUser_name());
        holder.post.setText(accountArrayList.get(position).getUser_post());
        holder.truck_number.setText(accountArrayList.get(position).getTruck_number());
        holder.phone.setText(Cryptography.decrypt(accountArrayList.get(position).getUser_phone()));
        holder.contractor_name.setText(accountArrayList.get(position).getEx_user_name());
    }

    @Override
    public int getItemCount() {
        return accountArrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialTextView username, phone, post, truck_number, contractor_name;
        MaterialButton accept, reject;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.username);
            phone = itemView.findViewById(R.id.number);
            post = itemView.findViewById(R.id.post);
            truck_number = itemView.findViewById(R.id.truck_number);
            contractor_name = itemView.findViewById(R.id.name);
            accept = itemView.findViewById(R.id.account_accept);
            reject = itemView.findViewById(R.id.account_reject);
        }
    }

    public interface OnItemClickListener {
        void OnApproveAccount(String user_id);

        void OnRejectAccount(String user_id);
    }
}
