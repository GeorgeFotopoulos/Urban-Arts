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
    Context mContext;
    List<item> mData;

    public Adapter(Context mContext, List<item> mData) {
        this.mContext = mContext;
        this.mData = mData;
    }

    @NotNull
    @Override
    public myViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View v = inflater.inflate(R.layout.card_item, parent, false);
        return new myViewHolder(v);
    }

    @Override
    public void onBindViewHolder(myViewHolder holder, int position) {
        holder.profilePhoto.setImageResource(mData.get(position).getProfilePhoto());
        holder.artistName.setText(mData.get(position).getArtistName());
        holder.eventType.setText(mData.get(position).getEventType());
        holder.address.setText(mData.get(position).getAddress());
        holder.likeCount.setText(mData.get(position).getLikeCount()+"");
        holder.commentCount.setText(mData.get(position).getCommentCount()+"");
        holder.shareCount.setText(mData.get(position).getShareCount()+"");
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class myViewHolder extends RecyclerView.ViewHolder {
        ImageView profilePhoto, likeImage, commentImage, shareImage;
        TextView artistName, eventType, address, likeCount, commentCount, shareCount;

        public myViewHolder(View itemView) {
            super(itemView);
            profilePhoto = itemView.findViewById(R.id.profilePhoto);
            likeImage = itemView.findViewById(R.id.likeImage);
            commentImage = itemView.findViewById(R.id.commentImage);
            shareImage = itemView.findViewById(R.id.shareImage);
            artistName = itemView.findViewById(R.id.artistName);
            eventType = itemView.findViewById(R.id.eventType);
            address = itemView.findViewById(R.id.address);
            likeCount = itemView.findViewById(R.id.likeCount);
            commentCount = itemView.findViewById(R.id.commentCount);
            shareCount = itemView.findViewById(R.id.shareCount);
        }
    }
}
