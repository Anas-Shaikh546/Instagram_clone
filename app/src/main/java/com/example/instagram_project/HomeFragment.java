package com.example.instagram_project;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.instagram_project.adapters.PostAdapter;
import com.example.instagram_project.models.Post;
import com.example.instagram_project.models.User;
import com.example.instagram_project.utils.FirebaseAuthHelper;
import com.example.instagram_project.utils.FirebaseFirestoreHelper;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerViewPosts;
    private PostAdapter postAdapter;
    private List<Post> posts;
    private FirebaseAuthHelper authHelper;
    private FirebaseFirestoreHelper firestoreHelper;

    // Map to cache userId → username
    private Map<String, String> userIdToUsername = new HashMap<>();

    public HomeFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        if (getContext() == null) return view;

        authHelper = new FirebaseAuthHelper(getContext());
        firestoreHelper = new FirebaseFirestoreHelper(getContext());

        recyclerViewPosts = view.findViewById(R.id.recyclerViewPosts);

        posts = new ArrayList<>();
        postAdapter = new PostAdapter(posts, getContext(), authHelper, firestoreHelper);
        recyclerViewPosts.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewPosts.setAdapter(postAdapter);

        loadPosts();

        return view;
    }

    private void loadPosts() {
        if (firestoreHelper == null || getContext() == null) return;

        firestoreHelper.getAllPosts(new FirebaseFirestoreHelper.FirestoreListCallback<Post>() {
            @Override
            public void onSuccess(List<Post> postList) {

                posts.clear();
                posts.addAll(postList);
                postAdapter.notifyDataSetChanged();

                // Fetch usernames for all posts
                for (Post post : posts) {
                    String userId = post.getUserId();

                    // Check cache first
                    if (userIdToUsername.containsKey(userId)) {
                        post.setUsername(userIdToUsername.get(userId));
                        postAdapter.notifyDataSetChanged();
                        continue;
                    }

                    // Fetch username from Firestore directly
                    FirebaseFirestore.getInstance().collection("users").document(userId).get()
                            .addOnSuccessListener(document -> {
                                String username = "Unknown";
                                if (document.exists() && document.getString("username") != null) {
                                    username = document.getString("username");
                                }
                                userIdToUsername.put(userId, username);
                                post.setUsername(username);
                                postAdapter.notifyDataSetChanged();
                            })
                            .addOnFailureListener(e -> {
                                post.setUsername("Unknown");
                                postAdapter.notifyDataSetChanged();
                            });
                }
            }

            @Override
            public void onFailure(String error) {
                Log.e("HomeFragment", "Failed to load posts: " + error);
                Toast.makeText(getContext(), "Failed to load posts: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
