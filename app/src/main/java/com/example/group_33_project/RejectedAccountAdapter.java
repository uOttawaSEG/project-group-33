package com.example.group_33_project;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.jspecify.annotations.NonNull;

import java.util.List;

public class RejectedAccountAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private OnAccountActionListener listener;
    private List<Account> accounts;

    // Interface with only onApprove
    public interface OnAccountActionListener {
        void onApprove(Account account);
    }

    public RejectedAccountAdapter(List<Account> accounts, OnAccountActionListener listener) {
        this.accounts = accounts;
        this.listener = listener;
    }

    @Override
    public RecyclerView.@NonNull ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == 1) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.account_student_rejected_layout, parent, false);
            return new StudentViewHolder(view);
        } else if (viewType == 2) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.account_tutor_rejected_layout, parent, false);
            return new TutorViewHolder(view);
        } else {
            throw new IllegalArgumentException("Unknown view type " + viewType);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Account account = accounts.get(position);

        if (holder instanceof StudentViewHolder && account instanceof Student) {
            Student s = (Student) account;
            StudentViewHolder studentHolder = (StudentViewHolder) holder;
            studentHolder.tvName.setText(s.getFirstName() + " " + s.getLastName());
            studentHolder.tvEmail.setText(s.getEmail());
            studentHolder.tvPhone.setText(s.getPhone());
            studentHolder.tvProgram.setText(s.getProgram());

            studentHolder.bApprove.setOnClickListener(v -> listener.onApprove(account));

        } else if (holder instanceof TutorViewHolder && account instanceof Tutor) {
            Tutor t = (Tutor) account;
            TutorViewHolder tutorHolder = (TutorViewHolder) holder;
            tutorHolder.tvName.setText(t.getFirstName() + " " + t.getLastName());
            tutorHolder.tvEmail.setText(t.getEmail());
            tutorHolder.tvPhone.setText(t.getPhone());
            tutorHolder.tvDegree.setText(t.getEducation());
            tutorHolder.tvCourses.setText(t.getCourses().toString().substring(1, t.getCourses().toString().length() - 1));

            tutorHolder.bApprove.setOnClickListener(v -> listener.onApprove(account));
        }
    }

    @Override
    public int getItemCount() {
        return accounts.size();
    }

    public static class StudentViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail, tvPhone, tvProgram;
        Button bApprove;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvProgram = itemView.findViewById(R.id.tvProgram);
            bApprove = itemView.findViewById(R.id.bAccept);
        }
    }

    public static class TutorViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail, tvPhone, tvDegree, tvCourses;
        Button bApprove;

        public TutorViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvDegree = itemView.findViewById(R.id.tvDegree);
            tvCourses = itemView.findViewById(R.id.tvCourses);
            bApprove = itemView.findViewById(R.id.bAccept);
        }
    }

    @Override
    public int getItemViewType(int position) {
        Account account = accounts.get(position);
        if (account instanceof Student) {
            return 1;
        } else if (account instanceof Tutor) {
            return 2;
        } else {
            return 0;
        }
    }
}