package com.example.expensetracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CategoryBreakdownAdapter extends RecyclerView.Adapter<CategoryBreakdownAdapter.ViewHolder> {

    private final List<Map.Entry<String, Double>> categoryList;

    public CategoryBreakdownAdapter(Map<String, Double> categoryMap) {
        this.categoryList = new ArrayList<>(categoryMap.entrySet());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map.Entry<String, Double> entry = categoryList.get(position);
        holder.categoryText.setText(entry.getKey());
        holder.amountText.setText(
                String.format(Locale.getDefault(), "₱%,.2f", entry.getValue())
        );
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView categoryText, amountText;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryText = itemView.findViewById(android.R.id.text1);
            amountText = itemView.findViewById(android.R.id.text2);
        }
    }
}
