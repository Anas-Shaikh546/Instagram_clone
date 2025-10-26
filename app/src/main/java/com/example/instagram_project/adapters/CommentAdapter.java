package com.example.instagram_project.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.instagram_project.R;
import com.example.instagram_project.models.Comment;
// import com.bumptech.glide.Glide; // We can add this later

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private Context context;
    private List<Comment> commentList;

    public CommentAdapter(Context context, List<Comment> commentList) {
        this.context = context;
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = commentList.get(position);
        holder.bind(comment);
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    class CommentViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivCommentProfile;
        private TextView tvCommentText, tvCommentTime;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCommentProfile = itemView.findViewById(R.id.ivCommentProfile);
            tvCommentText = itemView.findViewById(R.id.tvCommentText);
            tvCommentTime = itemView.findViewById(R.id.tvCommentTime);
        }

        public void bind(Comment comment) {
            // Set comment text (with username bolded)
            String username = comment.getUsername();
            String text = comment.getText();
            SpannableString content = new SpannableString(username + " " + text);
            content.setSpan(new StyleSpan(Typeface.BOLD), 0, username.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            tvCommentText.setText(content);

            // Set time
            if (comment.getCreatedAt() != null) {
                CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(
                        comment.getCreatedAt().getTime(),
                        System.currentTimeMillis(),
                        DateUtils.MINUTE_IN_MILLIS
                );
                tvCommentTime.setText(timeAgo);
            }

            // TODO: Load profile image with Glide
            // Glide.with(context).load(comment.getUserProfileImageUrl()).into(ivCommentProfile);
        }
    }
}