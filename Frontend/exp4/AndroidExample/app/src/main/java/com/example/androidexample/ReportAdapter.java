package com.example.androidexample;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
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

        String[] statuses = {"IN_REVIEW", "CLOSED"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                holder.itemView.getContext(),
                android.R.layout.simple_spinner_item,
                statuses
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.spinnerReportStatus.setAdapter(adapter);

        int selectedIndex = 0;
        if (report.getStatus().equals("CLOSED")) {
            selectedIndex = 1;
        }

        holder.spinnerReportStatus.setSelection(selectedIndex);

        holder.spinnerReportStatus.setOnItemSelectedListener(
                new android.widget.AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(android.widget.AdapterView<?> parent, View view, int pos, long id) {
                        report.setStatus(statuses[pos]);
                    }

                    @Override
                    public void onNothingSelected(android.widget.AdapterView<?> parent) {
                    }
                }
        );
    }

    @Override
    public int getItemCount() {
        return reportList.size();
    }

    static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView tvReportType, tvReportDescription;
        Spinner spinnerReportStatus;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            tvReportType = itemView.findViewById(R.id.tvReportType);
            tvReportDescription = itemView.findViewById(R.id.tvReportDescription);
            spinnerReportStatus = itemView.findViewById(R.id.spinnerReportStatus);
        }
    }
}