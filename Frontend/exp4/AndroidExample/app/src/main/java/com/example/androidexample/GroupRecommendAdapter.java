package com.example.androidexample;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.List;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
public class GroupRecommendAdapter extends RecyclerView.Adapter<GroupRecommendAdapter.GroupRecommendViewHolder>{
    private List<RecommendGroup> recommendGroupsList;

    public GroupRecommendAdapter(List<RecommendGroup> recommendGroupsList) {
        this.recommendGroupsList = recommendGroupsList;
    }

    @NonNull
    @Override
    public GroupRecommendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group_recommend, parent, false);
        return new GroupRecommendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupRecommendViewHolder holder, int position) {
        RecommendGroup group = recommendGroupsList.get(position);
        holder.tvGroupName.setText(group.getGroupName());
        holder.tvCategory.setText(group.getCategory());
        holder.tvMemberCount.setText(group.getMemberCount() + " members");
        holder.tvMatchScore.setText(group.getMatchScore() + "%");
        holder.tvGroupDescription.setText(group.getDescription());

        List<String> matchedKeywords = group.getMatchedKeywords();
        if (matchedKeywords.size() > 0)  {
            holder.tvKeyword1.setVisibility(View.VISIBLE);
            holder.tvKeyword1.setText(matchedKeywords.get(0));
        }
        else{
            holder.tvKeyword1.setVisibility(View.GONE);
        }

        if (matchedKeywords.size() > 1) {
            holder.tvKeyword2.setVisibility(View.VISIBLE);
            holder.tvKeyword2.setText(matchedKeywords.get(1));
        }
        else{
            holder.tvKeyword2.setVisibility(View.GONE);
        }

        if(matchedKeywords.size() > 2){
            holder.tvKeyword3.setVisibility(View.VISIBLE);
            holder.tvKeyword3.setText(matchedKeywords.get(2));
        }
        else{
            holder.tvKeyword3.setVisibility(View.GONE);
        }

        holder.tvGroupEmoji.setText(getCategoryEmoji(group.getCategory()));
    }
    @Override
    public int getItemCount() {
        return recommendGroupsList != null ? recommendGroupsList.size() : 0;
    }

    public void updateData(List<RecommendGroup> recommendGroupsList) {
        this.recommendGroupsList = recommendGroupsList;
        notifyDataSetChanged();
    }

    private String getCategoryEmoji(String category) {
        if (category == null) return "\uD83D\uDC65";

        switch (category.toLowerCase()) {
            case "tech":
            case "coding":
                return "💻";
            case "sports":
                return "🏀";
            case "music":
                return "🎵";
            case "fitness":
                return "💪";
            case "outdoor":
                return "🥾";
            case "gaming":
                return "🎮";
            case "study":
                return "📚";
            default:
                return "👥";
        }
    }

    static class GroupRecommendViewHolder extends RecyclerView.ViewHolder {

        TextView tvGroupEmoji;
        TextView tvGroupName;
        TextView tvCategory;
        TextView tvMemberCount;
        TextView tvMatchScore;
        TextView tvGroupDescription;
        TextView tvKeyword1;
        TextView tvKeyword2;
        TextView tvKeyword3;
        TextView tvViewDetails;

        public GroupRecommendViewHolder(@NonNull View itemView) {
            super(itemView);

            tvGroupEmoji = itemView.findViewById(R.id.tvGroupEmoji);
            tvGroupName = itemView.findViewById(R.id.tvGroupName);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvMemberCount = itemView.findViewById(R.id.tvMemberCount);
            tvMatchScore = itemView.findViewById(R.id.tvMatchScore);
            tvGroupDescription = itemView.findViewById(R.id.tvGroupDescription);
            tvKeyword1 = itemView.findViewById(R.id.tvKeyword1);
            tvKeyword2 = itemView.findViewById(R.id.tvKeyword2);
            tvKeyword3 = itemView.findViewById(R.id.tvKeyword3);
            tvViewDetails = itemView.findViewById(R.id.tvViewDetails);
        }
    }


}
