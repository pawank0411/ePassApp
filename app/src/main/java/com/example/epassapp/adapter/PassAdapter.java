package com.example.epassapp.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.epassapp.Model.Pass;
import com.example.epassapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class PassAdapter extends RecyclerView.Adapter<PassAdapter.ViewHolder> {

    private ArrayList<Pass> passArrayList;
    private Context mcontext;

    public PassAdapter(Context mcontext, ArrayList<Pass> passArrayList) {
        this.mcontext = mcontext;
        this.passArrayList = passArrayList;
    }

    @NonNull
    @Override
    public PassAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pass_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PassAdapter.ViewHolder holder, int position) {
        holder.serial_no.setText(passArrayList.get(position).getSerial_no());
        holder.date.setText(passArrayList.get(position).getDate());
        holder.mine_no.setText(passArrayList.get(position).getMine_no());
        holder.pit_owner.setText(passArrayList.get(position).getPit_owner());
        holder.section_no.setText(passArrayList.get(position).getSection_no());
        holder.bench_no.setText(passArrayList.get(position).getBench_no());
        holder.truck_no.setText(passArrayList.get(position).getTruck_no());
        holder.pass_contractorname.setText(passArrayList.get(position).getContractor_name());

        holder.signature_layout.setVisibility(View.VISIBLE);
        holder.approver_name.setText("Approved By : " + passArrayList.get(position).getApprover_name());
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("signatures/"
                + passArrayList.get(position).getApprover_name() + ".png");

        storageReference.getDownloadUrl().addOnSuccessListener(uri -> Glide.with(mcontext).load(uri.toString()).into(holder.signature)).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(mcontext, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        Log.d("url", String.valueOf(storageReference.getDownloadUrl()));


    }

    @Override
    public int getItemCount() {
        Log.d("listSize", String.valueOf(passArrayList.size()));
        return passArrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialTextView serial_no, date, mine_no, pit_owner, section_no, bench_no, truck_no, pass_contractorname;
        ConstraintLayout signature_layout;
        ImageView signature;
        MaterialTextView approver_name;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            signature = itemView.findViewById(R.id.signature);
            approver_name = itemView.findViewById(R.id.approver_name);
            signature_layout = itemView.findViewById(R.id.signature_layout);
            serial_no = itemView.findViewById(R.id.pass_serialno);
            date = itemView.findViewById(R.id.pass_date);
            mine_no = itemView.findViewById(R.id.pass_mineno);
            pit_owner = itemView.findViewById(R.id.pass_pitowner);
            section_no = itemView.findViewById(R.id.pass_sectionno);
            bench_no = itemView.findViewById(R.id.pass_benchno);
            truck_no = itemView.findViewById(R.id.pass_truckno);
            pass_contractorname = itemView.findViewById(R.id.pass_contractorname);
        }
    }
}
