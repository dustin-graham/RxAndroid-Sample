package com.example.rxandroid.api;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.rxandroid.R;
import com.example.rxandroid.SectionedRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dustin on 2/10/15.
 */
public class RepresentativeAdapter extends RecyclerView.Adapter<RepresentativeAdapter.ViewHolder>{

    private List<Representative> representatives = new ArrayList<>();

    public RepresentativeAdapter() {
    }

    public void swap(List<Representative> reps) {
        representatives.clear();
        if (reps != null) {
            representatives.addAll(reps);
            notifyDataSetChanged();
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_representative_list_item, parent, false);
        ImageView partyIcon = (ImageView) v.findViewById(R.id.partyIcon);
        TextView repName = (TextView) v.findViewById(R.id.representativeName);
        TextView officeLabel = (TextView) v.findViewById(R.id.officeLabel);
        ViewHolder vh = new ViewHolder(v, partyIcon, repName, officeLabel);
        return  vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Representative rep = representatives.get(position);
        holder.representativeName.setText(rep.name);
        holder.officeLabel.setText(rep.office);
        if (rep.isFunny) {
            holder.partyIcon.setImageDrawable(holder.itemView.getResources().getDrawable(R.drawable.funny));
        } else {
            holder.partyIcon.setImageDrawable(holder.itemView.getResources().getDrawable(R.drawable.not_funny));
        }
    }

    @Override
    public int getItemCount() {
        return this.representatives.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView representativeName;
        public ImageView partyIcon;
        public TextView officeLabel;
        public ViewHolder(View v, ImageView partyIcon, TextView representativeName, TextView officeLabel) {
            super(v);
            this.partyIcon = partyIcon;
            this.representativeName = representativeName;
            this.officeLabel = officeLabel;
        }
    }

    public static class SectionViewHolder extends RecyclerView.ViewHolder {

        private TextView textView;

        public SectionViewHolder(View itemView) {
            super(itemView);
            this.textView = (TextView) itemView.findViewById(R.id.partyLabel);
        }
    }
}
