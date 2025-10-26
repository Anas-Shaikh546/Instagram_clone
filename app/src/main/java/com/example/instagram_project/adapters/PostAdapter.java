package com.example.instagram_project.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.text.format.DateUtils;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.instagram_project.CommentsActivity;
import com.example.instagram_project.R;
import com.example.instagram_project.models.Post;
import com.example.instagram_project.utils.FirebaseAuthHelper;
import com.example.instagram_project.utils.FirebaseFirestoreHelper;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<Post> posts;
    private Context context;
    private FirebaseAuthHelper authHelper;
    private FirebaseFirestoreHelper firestoreHelper;

    public PostAdapter(List<Post> posts, Context context, FirebaseAuthHelper authHelper, FirebaseFirestoreHelper firestoreHelper) {
        this.posts = posts;
        this.context = context;
        this.authHelper = authHelper;
        this.firestoreHelper = firestoreHelper;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.bind(post);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    class PostViewHolder extends RecyclerView.ViewHolder {
        // Updated Views
        private TextView tvUsername, tvCaption, tvLikesCount, tvTimeAgo, tvViewAllComments;
        private ImageView ivPostImage, ivLike, ivComment, ivUserProfile;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvCaption = itemView.findViewById(R.id.tvCaption);
            tvLikesCount = itemView.findViewById(R.id.tvLikesCount);
            tvViewAllComments = itemView.findViewById(R.id.tvViewAllComments);
            tvTimeAgo = itemView.findViewById(R.id.tvTimeAgo);
            ivPostImage = itemView.findViewById(R.id.ivPostImage);
            ivLike = itemView.findViewById(R.id.ivLike);
            ivComment = itemView.findViewById(R.id.ivComment);
            ivUserProfile = itemView.findViewById(R.id.ivUserProfile);
        }

        public void bind(Post post) {
            // Set Username
            tvUsername.setText(post.getUsername());

            // Set Caption (with username bolded, like Instagram)
            String username = post.getUsername();
            String caption = post.getCaption();
            SpannableString content = new SpannableString(username + " " + caption);
            content.setSpan(new StyleSpan(Typeface.BOLD), 0, username.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            tvCaption.setText(content);

            // Set Likes Count
            tvLikesCount.setText(post.getLikesCount() + " likes");

            // Set Comments Count Text
            tvViewAllComments.setText("View all " + post.getCommentsCount() + " comments");

            // Set Time Ago
            if (post.getCreatedAt() != null) {
                CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(
                        post.getCreatedAt().getTime(),
                        System.currentTimeMillis(),
                        DateUtils.MINUTE_IN_MILLIS
                );
                tvTimeAgo.setText(timeAgo);
            }

            // Load Post Image
            if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
                Glide.with(context)
                        .load(post.getImageUrl())
                        .placeholder(R.drawable.ic_placeholder)
                        .into(ivPostImage);
            }

            // TODO: Load User Profile Image (if available)
            // Glide.with(context).load(post.getUserProfileImageUrl()).into(ivUserProfile);


            // --- LIKE LOGIC ---
            String currentUserId = authHelper.getCurrentUser().getUid();

            // Check initial like status
            firestoreHelper.checkIfUserLikedPost(post.getPostId(), currentUserId, new FirebaseFirestoreHelper.FirestoreDataCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean isLiked) {
                    if (isLiked) {
                        ivLike.setImageResource(R.drawable.ic_like_filled);
                    } else {
                        ivLike.setImageResource(R.drawable.ic_like_empty);
                    }
                }
                @Override
                public void onFailure(String error) {
                    ivLike.setImageResource(R.drawable.ic_like_empty);
                }
            });

            // Like/Unlike click listener
            ivLike.setOnClickListener(v -> {
                toggleLike(post, currentUserId);
            });

            // Double-tap to like
            setupDoubleTapToLike(post, currentUserId);

            // --- COMMENT LOGIC ---
            // This now launches the new CommentsActivity
            View.OnClickListener commentClickListener = v -> {
                Intent intent = new Intent(context, CommentsActivity.class);
                intent.putExtra("POST_ID", post.getPostId());
                context.startActivity(intent);
            };

            ivComment.setOnClickListener(commentClickListener);
            tvViewAllComments.setOnClickListener(commentClickListener);
        }

        private void toggleLike(Post post, String currentUserId) {
            String username = authHelper.getCurrentUser().getEmail(); // Or ideally, username

            firestoreHelper.checkIfUserLikedPost(post.getPostId(), currentUserId, new FirebaseFirestoreHelper.FirestoreDataCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean isLiked) {
                    if (isLiked) {
                        // UNLIKE the post
                        firestoreHelper.unlikePost(post.getPostId(), currentUserId, new FirebaseFirestoreHelper.FirestoreCallback() {
                            @Override
                            public void onSuccess() {
                                ivLike.setImageResource(R.drawable.ic_like_empty);
                                // Update likes count in UI
                                post.setLikesCount(post.getLikesCount() - 1);
                                tvLikesCount.setText(post.getLikesCount() + " likes");
                            }
                            @Override
                            public void onFailure(String error) {
                                Toast.makeText(context, "Failed to unlike: " + error, Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        // LIKE the post
                        firestoreHelper.likePost(post.getPostId(), currentUserId, username, new FirebaseFirestoreHelper.FirestoreCallback() {
                            @Override
                            public void onSuccess() {
                                ivLike.setImageResource(R.drawable.ic_like_filled);
                                // Update likes count in UI
                                post.setLikesCount(post.getLikesCount() + 1);
                                tvLikesCount.setText(post.getLikesCount() + " likes");
                            }
                            @Override
                            public void onFailure(String error) {
                                Toast.makeText(context, "Failed to like: " + error, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
                @Override
                public void onFailure(String error) {
                    Toast.makeText(context, "Error: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        }

        private void setupDoubleTapToLike(Post post, String currentUserId) {
            GestureDetector gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    // Only "like" on double-tap, don't "unlike"
                    String username = authHelper.getCurrentUser().getEmail();
                    firestoreHelper.likePost(post.getPostId(), currentUserId, username, new FirebaseFirestoreHelper.FirestoreCallback() {
                        @Override
                        public void onSuccess() {
                            ivLike.setImageResource(R.drawable.ic_like_filled);
                            post.setLikesCount(post.getLikesCount() + 1);
                            tvLikesCount.setText(post.getLikesCount() + " likes");
                            // TODO: Add a heart animation on the image
                        }
                        @Override
                        public void onFailure(String error) {
                            // Fail silently, maybe it was already liked
                        }
                    });
                    return super.onDoubleTap(e);
                }
            });

            ivPostImage.setOnTouchListener((v, event) -> {
                gestureDetector.onTouchEvent(event);
                return true; // Consume the event
            });
        }

        // The showCommentsDialog() method has been completely removed.
    }
}