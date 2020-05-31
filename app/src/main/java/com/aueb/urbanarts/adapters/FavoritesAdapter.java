package com.aueb.urbanarts.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.aueb.urbanarts.R;
import com.aueb.urbanarts.items.item;
import com.bumptech.glide.Glide;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.List;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.myViewHolder> {
    private FavoritesAdapter.OnItemClickListener mListener;
    private Context mContext;
    private List<item> mData;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(FavoritesAdapter.OnItemClickListener mListener) {
        this.mListener = mListener;
    }

    public FavoritesAdapter(Context mContext, List<item> mData) {
        this.mContext = mContext;
        this.mData = mData;
    }

    @NotNull
    @Override
    public FavoritesAdapter.myViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View v = inflater.inflate(R.layout.favorites_item, parent, false);
        return new FavoritesAdapter.myViewHolder(v, mListener);
    }

    @Override
    public void onBindViewHolder(FavoritesAdapter.myViewHolder holder, int position) {
        try {
            if (!(mData.get(position).getProfilePhoto()).equals("none")) {
                Glide.with(mContext.getApplicationContext()).load(mData.get(position).getProfilePhoto()).into(holder.profilePhoto);
            } else {
                holder.profilePhoto.setImageResource(R.drawable.profile);
            }
        } catch (Exception ignore) {
        }
        holder.artistName.setText(mData.get(position).getArtistName());
        if (mData.get(position).getArtistName().equalsIgnoreCase("none")) {
            holder.artistName.setText("Unknown");
        }
        try {
            holder.artistDescription.setText(mData.get(position).getArtistDescription());
        } catch (Exception e) {

        }
        String type = String.valueOf(mData.get(position).getArtistType());
        int age = Calendar.getInstance().get(Calendar.YEAR) - Integer.parseInt(mData.get(position).getYear());
        if (type.equals("individual")) {
            String textAge = age + " years old.";
            holder.artistGenre.setText(mData.get(position).getTypeOfArt());
            holder.artistAge.setText(textAge);
        } else {
            String textAge = "est. " + mData.get(position).getYear() + ".";
            String textGenre = mData.get(position).getTypeOfArt() + " Group";
            holder.artistGenre.setText(textGenre);
            holder.artistAge.setText(textAge);
        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    static class myViewHolder extends RecyclerView.ViewHolder {
        ImageView profilePhoto;
        TextView artistName, artistDescription, artistGenre, artistAge;

        myViewHolder(View itemView, final FavoritesAdapter.OnItemClickListener mListener) {
            super(itemView);
            profilePhoto = itemView.findViewById(R.id.profilePhoto);
            artistName = itemView.findViewById(R.id.artistName);
            artistDescription = itemView.findViewById(R.id.artistDescription);
            artistGenre = itemView.findViewById(R.id.artistGenre);
            artistAge = itemView.findViewById(R.id.artistAge);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            mListener.onItemClick(position);
                        }
                    }
                }
            });
        }
    }
}
