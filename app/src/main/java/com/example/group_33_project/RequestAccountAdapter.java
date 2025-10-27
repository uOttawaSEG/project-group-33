package com.example.group_33_project;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.jspecify.annotations.NonNull;

import java.util.List;

// ADAPTER FOR THE PENDING ACCONTS TO BE APPROVED AGAIN
// helper method for RecyclerView
//takes each element from the account list and transforms it into scrollable UI object

public class RequestAccountAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private OnAccountActionListener listener;
    private List<Account> accounts;

    // Define interface for callbacks (delete(or deny) and approve)
    public interface OnAccountActionListener {
        void onApprove(Account account);
        void onDelete(Account account);
    }

    //constructor
    public RequestAccountAdapter(List<Account> accounts, OnAccountActionListener listener) {
        this.accounts = accounts;
        this.listener = listener;
    }

    //create a sample view (unfilled yet)
    @Override
    public RecyclerView.@NonNull ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        //different view for student and tutor
        if (viewType == 1){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.account_student_add_layout, parent, false);
            return new StudentViewHolder(view);
        }

        else if (viewType == 2){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.account_tutor_add_layout, parent, false);
            return new TutorViewHolder(view);
        }

        else {
            //error if something else
            throw new IllegalArgumentException("Unknown view type " + viewType);
        }

    }

    //fills the each view with appropriate account information
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Account account = accounts.get(position);

        if (holder instanceof StudentViewHolder && account instanceof Student) {
            Student s= (Student) account;
            StudentViewHolder studentHolder = (StudentViewHolder) holder;
            studentHolder.tvName.setText(s.getFirstName() + " " + s.getLastName());
            studentHolder.tvEmail.setText(s.getEmail());
            studentHolder.tvPhone.setText(s.getPhone());
            studentHolder.tvProgram.setText(s.getProgram());

            studentHolder.bAccept.setOnClickListener(v -> listener.onApprove(account));
            studentHolder.bDeny.setOnClickListener(v -> listener.onDelete(account));

        } else if (holder instanceof TutorViewHolder && account instanceof Tutor) {
            Tutor t= (Tutor) account;
            TutorViewHolder tutorHolder = (TutorViewHolder) holder;
            tutorHolder.tvName.setText(t.getFirstName() + " " + t.getLastName());
            tutorHolder.tvEmail.setText(t.getEmail());
            tutorHolder.tvPhone.setText(t.getPhone());
            tutorHolder.tvDegree.setText(t.getEducation());
            tutorHolder.tvCourses.setText(t.getCourses().toString().substring(1, t.getCourses().toString().length() - 1));

            tutorHolder.bAccept.setOnClickListener(v -> listener.onApprove(account));
            tutorHolder.bDeny.setOnClickListener(v -> listener.onDelete(account));
        }
    }

    //number of items to be scrolled through
    @Override
    public int getItemCount() {
        return accounts.size();
    }

    //dummy of a student view (unfilled yet)
    public static class StudentViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail, tvPhone, tvProgram;
        Button bAccept, bDeny;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvProgram = itemView.findViewById(R.id.tvProgram);
            bAccept = itemView.findViewById(R.id.bAccept);
            bDeny = itemView.findViewById(R.id.bDeny);
        }
    }

    //dummy of a tutor view (unfilled yet)
    public static class TutorViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail, tvPhone, tvDegree, tvCourses;
        Button bAccept, bDeny;

        public TutorViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvDegree = itemView.findViewById(R.id.tvDegree);
            tvCourses = itemView.findViewById(R.id.tvCourses);
            bAccept = itemView.findViewById(R.id.bAccept);
            bDeny = itemView.findViewById(R.id.bDeny);
        }
    }

    //getter of a type for different views
    //student and tutor have different UI elements
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
