package com.aueb.urbanarts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Adapter extends RecyclerView.Adapter<Adapter.myViewHolder> {
    private Context mContext;
    private List<item> mData;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener mListener) {
        this.mListener = mListener;
    }

    Adapter(Context mContext, List<item> mData) {
        this.mContext = mContext;
        this.mData = mData;
    }

    @NotNull
    @Override
    public myViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View v = inflater.inflate(R.layout.card_item, parent, false);
        return new myViewHolder(v, mListener);
    }

    @Override
    public void onBindViewHolder(myViewHolder holder, int position) {
        holder.profilePhoto.setImageResource(mData.get(position).getProfilePhoto());
        holder.artistName.setText(mData.get(position).getArtistName());
        holder.eventType.setText(mData.get(position).getTypeOfArt());
        holder.address.setText(mData.get(position).getLocation());
        holder.likeCount.setText(String.valueOf(mData.get(position).getLikeCount()));
        holder.commentCount.setText(String.valueOf(mData.get(position).getCommentCount()));
        if (mData.get(position).getLiveEvent()) {
            holder.liveImage.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    static class myViewHolder extends RecyclerView.ViewHolder {
        ImageView profilePhoto, likeImage, commentImage, liveImage;
        TextView artistName, eventType, address, likeCount, commentCount;

        myViewHolder(View itemView, final OnItemClickListener mListener) {
            super(itemView);
            profilePhoto = itemView.findViewById(R.id.profilePhoto);
            likeImage = itemView.findViewById(R.id.upvoteImage);
            commentImage = itemView.findViewById(R.id.commentImage);
            artistName = itemView.findViewById(R.id.artistName);
            eventType = itemView.findViewById(R.id.eventType);
            address = itemView.findViewById(R.id.address);
            likeCount = itemView.findViewById(R.id.likeCount);
            commentCount = itemView.findViewById(R.id.commentCount);
            liveImage = itemView.findViewById(R.id.liveImage);

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
