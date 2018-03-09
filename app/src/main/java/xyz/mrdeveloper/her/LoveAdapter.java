package xyz.mrdeveloper.her;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Lakshay Raj on 24-11-2017.
 */

public class LoveAdapter extends RecyclerView.Adapter<LoveAdapter.ViewHolder> {

    private ArrayList<LovedOnes> lovedList;

    LoveAdapter(ArrayList<LovedOnes> lovedList) {

        this.lovedList = MainActivity.lovedList;
        //Log.d("Check", "in adapter: & size: " + MainActivity.lovedList.get(0).getName() + MainActivity.lovedList.get(1).getName() + MainActivity.lovedList.size());
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout layout;
        TextView name;
        TextView number;

        ViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.loved_name);
            number = (TextView) itemView.findViewById(R.id.loved_number);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.loved_ones_details, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        LovedOnes lovedOnes = lovedList.get(position);
        holder.name.setText(lovedOnes.getName());
        holder.number.setText(lovedOnes.getNumber());
    }

    @Override
    public int getItemCount() {
        return lovedList.size();
    }
}
