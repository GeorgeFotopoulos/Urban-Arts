package com.aueb.urbanarts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.myViewHolder> {
    private Context mContext;
    private List<String> Usernames;
    private List<String> Comments;

    CommentAdapter(Context mContext, List<String> Usernames,List<String>Comments) {
        this.mContext = mContext;
        this.Usernames = Usernames;
        this.Comments = Comments;
    }

    @NotNull
    @Override
    public myViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View v = inflater.inflate(R.layout.comment, parent, false);
        return new myViewHolder(v);
    }

    @Override
    public void onBindViewHolder(myViewHolder holder, int position) {
        holder.username.setText(Usernames.get(position));
        holder.comment.setText(Comments.get(position));
    }

    @Override
    public int getItemCount() {
        return Comments.size();
    }

    static class myViewHolder extends RecyclerView.ViewHolder {
        TextView username, comment;

        myViewHolder(View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.username);
            comment = itemView.findViewById(R.id.comment);
        }
    }
}
