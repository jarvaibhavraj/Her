package xyz.mrdeveloper.her;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Lakshay Raj on 24-11-2017.
 */

public class FamilyMemberAdapter extends RecyclerView.Adapter<FamilyMemberAdapter.ViewHolder> {

    private ArrayList<FamilyMemberData> familyMembersList;
    private RecyclerViewOnClickListener recyclerViewOnClickListener;

    FamilyMemberAdapter(RecyclerViewOnClickListener itemClick, ArrayList<FamilyMemberData> list) {
        familyMembersList = list;
        recyclerViewOnClickListener = itemClick;
        //Log.d("Check", "in adapter: & size: " + MainActivity.familyMembersList.get(0).getName() + MainActivity.familyMembersList.get(1).getName() + MainActivity.familyMembersList.size());
    }

    public interface RecyclerViewOnClickListener {
        void RecyclerViewOnClick(int position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        View itemView;
        TextView name;
        TextView countryCode;
        TextView number;

        ViewHolder(View view) {
            super(view);
            itemView = view;
            name = view.findViewById(R.id.family_member_name);
            countryCode = view.findViewById(R.id.family_member_country_code);
            number = view.findViewById(R.id.family_member_number);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.family_member_details, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        FamilyMemberData familyMemberData = this.familyMembersList.get(position);
        holder.name.setText(familyMemberData.getName());
        holder.countryCode.setText(String.format("+%s", Integer.toString(familyMemberData.getCountryCode())));
        holder.number.setText(familyMemberData.getNumber());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recyclerViewOnClickListener.RecyclerViewOnClick(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return familyMembersList.size();
    }
}
