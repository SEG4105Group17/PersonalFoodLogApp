package com.example.personalfoodlogapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import kotlin.Pair;

public class ItemListAdapter extends RecyclerView.Adapter<ItemListAdapter.ViewHolder> {

    private List<Pair<String,Integer>> mData;
    private LayoutInflater mInflater;

    private ClickListener listener;


    // data is passed into the constructor
    ItemListAdapter(Context context, List<Pair<String,Integer>> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_row, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Pair<String,Integer> food = mData.get(position);
        holder.itemNameView.setText(food.getFirst());
        holder.calorieView.setText(String.valueOf(food.getSecond()) + " Grams");
    }

    // total number of rows
    @Override
    public int getItemCount() {
        if (mData==null) {
            return 0;
        }
        return mData.size();
    }



    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView itemNameView;
        TextView calorieView;

        ViewHolder(View itemView) {
            super(itemView);
            itemNameView = itemView.findViewById(R.id.itemName);
            calorieView = itemView.findViewById(R.id.itemCalorie);

            // Add item manipulation buttons
            ImageButton itemRemoveButton = itemView.findViewById(R.id.removeItemButton);
            itemRemoveButton.setOnClickListener(v -> listener.onDeleteClick(v, getAbsoluteAdapterPosition()));

            ImageButton itemEditButton = itemView.findViewById(R.id.editItemButton);
            itemEditButton.setOnClickListener(v -> listener.onEditClick(v, getAbsoluteAdapterPosition()));

            ImageButton viewItemButton = itemView.findViewById(R.id.viewItemButton);
            viewItemButton.setOnClickListener(v -> listener.onViewClick(v, getAbsoluteAdapterPosition()));
        }

    }

    interface ClickListener {
        void onDeleteClick(View view, int position);
        void onEditClick(View view, int position);
        void onViewClick(View view, int position);
    }

    public void setClickListener(ClickListener listen) {
        this.listener = listen;
    }

    // convenience method for getting data at click position
    Pair<String,Integer> getItem(int id) {
        return mData.get(id);
    }

}