package com.example.androidexample;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ModeratorMemberAdapter extends RecyclerView.Adapter<ModeratorMemberAdapter.MemberViewHolder> {

    private List<ModeratorMember> memberList;

    public ModeratorMemberAdapter(List<ModeratorMember> memberList) {
        this.memberList = memberList;
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_moderator_member, parent, false);
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        ModeratorMember member = memberList.get(position);
        holder.tvMemberName.setText(member.getDisplayName());
        holder.tvMemberId.setText("User ID: " + member.getUserId());
    }

    @Override
    public int getItemCount() {
        return memberList.size();
    }

    static class MemberViewHolder extends RecyclerView.ViewHolder {
        TextView tvMemberName, tvMemberId;

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMemberName = itemView.findViewById(R.id.tvMemberName);
            tvMemberId = itemView.findViewById(R.id.tvMemberId);
        }
    }
}