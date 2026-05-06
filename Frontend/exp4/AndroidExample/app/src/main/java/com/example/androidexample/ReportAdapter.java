package com.example.androidexample;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {

    private static final String BASE_URL = "http://coms-3090-015.class.las.iastate.edu:8080";

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

        String[] statuses = {"IN_REVIEW", "APPROVED", "DECLINED"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                holder.itemView.getContext(),
                android.R.layout.simple_spinner_item,
                statuses
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.spinnerReportStatus.setAdapter(adapter);

        int selectedIndex = 0;

        if ("APPROVED".equals(report.getStatus())) {
            selectedIndex = 1;
        } else if ("DECLINED".equals(report.getStatus())) {
            selectedIndex = 2;
        }

        /*
         * Important:
         * Set selection before attaching listener.
         * This prevents an unnecessary PUT request when RecyclerView binds the row.
         */
        holder.spinnerReportStatus.setSelection(selectedIndex, false);

        holder.spinnerReportStatus.setOnItemSelectedListener(
                new android.widget.AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(android.widget.AdapterView<?> parent, View view, int pos, long id) {
                        String oldStatus = report.getStatus();
                        String newStatus = statuses[pos];

                        if (newStatus.equals(report.getStatus())) {
                            return;
                        }

                        report.setStatus(newStatus);
                        updateReportStatus(holder, report, oldStatus,newStatus);
                    }

                    @Override
                    public void onNothingSelected(android.widget.AdapterView<?> parent) {
                    }
                }
        );
    }

    private void updateReportStatus(@NonNull ReportViewHolder holder, Report report, String oldStatus,String newStatus) {
        String url = BASE_URL + "/reports/" + report.getReportId();

        JSONObject body = new JSONObject();

        try {
            body.put("status", newStatus);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(
                    holder.itemView.getContext(),
                    "Failed to build status update",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        StringRequest request = new StringRequest(
                Request.Method.PUT,
                url,
                response -> {
                    Toast.makeText(
                            holder.itemView.getContext(),
                            "Report status updated to " + newStatus,
                            Toast.LENGTH_SHORT
                    ).show();
                },
                error -> {
                    report.setStatus(oldStatus);
                    Toast.makeText(
                            holder.itemView.getContext(),
                            "Failed to update report status",
                            Toast.LENGTH_SHORT
                    ).show();
                    int adapterPosition = holder.getAdapterPosition();
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        notifyItemChanged(adapterPosition);
                    }
                }
        ) {
            @Override
            public byte[] getBody() throws AuthFailureError {
                return body.toString().getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };

        VolleySingleton.getInstance(holder.itemView.getContext().getApplicationContext())
                .addToRequestQueue(request);
    }

    @Override
    public int getItemCount() {
        return reportList.size();
    }

    static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView tvReportType;
        TextView tvReportDescription;
        Spinner spinnerReportStatus;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            tvReportType = itemView.findViewById(R.id.tvReportType);
            tvReportDescription = itemView.findViewById(R.id.tvReportDescription);
            spinnerReportStatus = itemView.findViewById(R.id.spinnerReportStatus);
        }
    }
}