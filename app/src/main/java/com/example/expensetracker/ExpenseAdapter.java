package com.example.expensetracker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Locale;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ViewHolder> {

    private ArrayList<Expense> expenseList;
    private Context context;

    public ExpenseAdapter(ArrayList<Expense> expenseList, Context context) {
        this.expenseList = expenseList;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_expense, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Expense exp = expenseList.get(position);
        holder.txtTitle.setText(exp.getTitle());
        holder.txtCategory.setText(exp.getCategory());
        holder.txtDate.setText(exp.getDate());
        holder.txtAmount.setText(String.format(Locale.getDefault(), "₱%.2f", exp.getAmount()));

        // ✅ Dynamic Icon Logic Based on Category
        String category = exp.getCategory().toLowerCase();

        if (category.contains("food") || category.contains("meal") || category.contains("restaurant")) {
            holder.expIcon.setImageResource(R.drawable.ic_food);
        } else if (category.contains("transport") || category.contains("fare") || category.contains("gas")) {
            holder.expIcon.setImageResource(R.drawable.ic_transport);
        } else if (category.contains("shopping") || category.contains("clothes") || category.contains("mall")) {
            holder.expIcon.setImageResource(R.drawable.ic_shopping);
        } else if (category.contains("bills") || category.contains("utilities")) {
            holder.expIcon.setImageResource(R.drawable.ic_bills);
        } else if (category.contains("entertainment") || category.contains("movie") || category.contains("game")) {
            holder.expIcon.setImageResource(R.drawable.ic_entertainment);
        } else {
            // Default icon kung walang match
            holder.expIcon.setImageResource(R.drawable.ic_other);
        }
    }

    @Override
    public int getItemCount() {
        return expenseList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle, txtCategory, txtDate, txtAmount;
        ImageView expIcon;

        public ViewHolder(View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.expTitle);
            txtCategory = itemView.findViewById(R.id.expCategory);
            txtDate = itemView.findViewById(R.id.expDate);
            txtAmount = itemView.findViewById(R.id.expAmount);
            expIcon = itemView.findViewById(R.id.expIcon);
        }
    }
}