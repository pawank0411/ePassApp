package com.example.epassapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.epassapp.Activity.EditPassActivity;
import com.example.epassapp.Model.Pass;
import com.example.epassapp.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;

import static com.example.epassapp.utilities.Constants.APPROVER_NAME;
import static com.example.epassapp.utilities.Constants.PASS_BENCHNO;
import static com.example.epassapp.utilities.Constants.PASS_DATE;
import static com.example.epassapp.utilities.Constants.PASS_MINENO;
import static com.example.epassapp.utilities.Constants.PASS_PITOWNER;
import static com.example.epassapp.utilities.Constants.PASS_SECTIONNO;
import static com.example.epassapp.utilities.Constants.PASS_SERIALNO;
import static com.example.epassapp.utilities.Constants.PASS_TRUCKNO;
import static com.example.epassapp.utilities.Constants.USER_ID;

public class ApprovePassAdapter extends RecyclerView.Adapter<ApprovePassAdapter.ViewHolder> {
    private ArrayList<Pass> passArrayList;
    private Context mcontext;
    private OnItemClickListener onItemClickListener;

    public ApprovePassAdapter(Context mcontext, ArrayList<Pass> passArrayList, OnItemClickListener onItemClickListener) {
        this.mcontext = mcontext;
        this.passArrayList = passArrayList;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ApprovePassAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pass_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ApprovePassAdapter.ViewHolder holder, final int position) {
        holder.action_layout.setVisibility(View.VISIBLE);
        holder.serial_no.setText(passArrayList.get(position).getSerial_no());
        holder.date.setText(passArrayList.get(position).getDate());
        holder.mine_no.setText(passArrayList.get(position).getMine_no());
        holder.pit_owner.setText(passArrayList.get(position).getPit_owner());
        holder.section_no.setText(passArrayList.get(position).getSection_no());
        holder.bench_no.setText(passArrayList.get(position).getBench_no());
        holder.truck_no.setText(passArrayList.get(position).getTruck_no());
        holder.pass_contractorname.setText(passArrayList.get(position).getContractor_name());

        //handle buttons
        holder.edit.setOnClickListener(view -> {
            Pass passList = passArrayList.get(position);
            Intent intent = new Intent(mcontext, EditPassActivity.class);
            intent.putExtra(PASS_SERIALNO, passList.getSerial_no());
            intent.putExtra(PASS_DATE, passList.getDate());
            intent.putExtra(PASS_PITOWNER, passList.getPit_owner());
            intent.putExtra(PASS_SECTIONNO, passList.getSection_no());
            intent.putExtra(PASS_BENCHNO, passList.getBench_no());
            intent.putExtra(PASS_TRUCKNO, passList.getTruck_no());
            intent.putExtra(USER_ID, passList.getUser_id());
            intent.putExtra(APPROVER_NAME, passList.getApprover_name());
            if (passList.getMine_no().equals("Mine Number 1")) {
                intent.putExtra(PASS_MINENO, 1);
            } else if (passList.getMine_no().equals("Mine Number 2")) {
                intent.putExtra(PASS_MINENO, 2);
            }
            mcontext.startActivity(intent);
        });

        holder.accept.setOnClickListener(view -> {
            if (onItemClickListener != null) {
                onItemClickListener.onAcceptClicked(passArrayList.get(position).getDate(), passArrayList.get(position).getUser_id());
            }
        });

        holder.reject.setOnClickListener(view -> {
            if (onItemClickListener != null) {
                onItemClickListener.onRejectClicked(passArrayList.get(position).getUser_id(), passArrayList.get(position).getUser_id());
            }
        });
    }

    @Override
    public int getItemCount() {
        return passArrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialTextView serial_no, date, mine_no, pit_owner, section_no, bench_no, truck_no,pass_contractorname;
        LinearLayout action_layout;
        MaterialButton accept, reject, edit;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            action_layout = itemView.findViewById(R.id.action_layout);
            serial_no = itemView.findViewById(R.id.pass_serialno);
            date = itemView.findViewById(R.id.pass_date);
            mine_no = itemView.findViewById(R.id.pass_mineno);
            pit_owner = itemView.findViewById(R.id.pass_pitowner);
            section_no = itemView.findViewById(R.id.pass_sectionno);
            bench_no = itemView.findViewById(R.id.pass_benchno);
            truck_no = itemView.findViewById(R.id.pass_truckno);
            accept = itemView.findViewById(R.id.pass_accept);
            reject = itemView.findViewById(R.id.pass_reject);
            edit = itemView.findViewById(R.id.edi_pass);
            pass_contractorname = itemView.findViewById(R.id.pass_contractorname);
        }
    }

    public interface OnItemClickListener {
        void onRejectClicked(String pass_date, String pass_id);

        void onAcceptClicked(String pass_date, String pass_id);
    }
}
