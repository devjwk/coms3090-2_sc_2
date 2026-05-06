package com.example.androidexample;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {

    private List<Report> reportList;

    public ReportAdapter(List<Report> reportList) {
        this.reportList = reportList;
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_report, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        Report report = reportList.get(position);

        holder.tvReportType.setText("Reported User ID: " + report.getReportedId());
        holder.tvReportDescription.setText(
                "Reporter ID: " + report.getReporterId() +
                        "\nDescription: " + report.getDescription()
        );

        holder.tvReportStatus.setText("Status: " + report.getStatus());
    }


    @Override
    public int getItemCount() {
        return reportList.size();
    }

    static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView tvReportType, tvReportDescription, tvReportStatus;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            tvReportType = itemView.findViewById(R.id.tvReportType);
            tvReportDescription = itemView.findViewById(R.id.tvReportDescription);
            tvReportStatus = itemView.findViewById(R.id.tvReportStatus);
        }
    }
}