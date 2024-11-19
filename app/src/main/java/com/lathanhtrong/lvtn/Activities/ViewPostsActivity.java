package com.lathanhtrong.lvtn.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lathanhtrong.lvtn.Adapters.PostAdapter;
import com.lathanhtrong.lvtn.Models.Post;
import com.lathanhtrong.lvtn.R;
import com.lathanhtrong.lvtn.Values;
import com.lathanhtrong.lvtn.databinding.ActivityViewPostsBinding;

import java.util.ArrayList;
import java.util.List;

public class ViewPostsActivity extends AppCompatActivity {

    private ActivityViewPostsBinding binding;
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityViewPostsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (auth.getCurrentUser() != null) {
            FirebaseUser user = auth.getCurrentUser();
            Glide.with(this).load(user.getPhotoUrl()).into(binding.profileIv);
            binding.cbMyPost.setVisibility(View.VISIBLE);
        }
        else {
            binding.cbMyPost.setVisibility(View.GONE);
        }


        binding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        binding.addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser user = auth.getCurrentUser();
                if (user == null) {
                    Toast.makeText(ViewPostsActivity.this, getResources().getString(R.string.err_addpost_login), Toast.LENGTH_SHORT).show();
                }
                else {
                    Intent intent = new Intent(ViewPostsActivity.this, AddPostActivity.class);
                    startActivity(intent);
                }
            }
        });

        getPost();

        binding.cbMyPost.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                getPost();
            }
        });
    }

    public void getPost() {
        List<Post> posts = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance(Values.region).getReference("Posts");

        if (auth.getCurrentUser() != null) {
            if (binding.cbMyPost.isChecked()) {
                ref.orderByChild("user_id").equalTo(auth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        posts.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Post post = ds.getValue(Post.class);
                            posts.add(post);
                        }
                        RecyclerView recyclerView = binding.postRv;
                        PostAdapter adapter = new PostAdapter(posts);
                        recyclerView.setAdapter(adapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
            else {
                ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        posts.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Post post = ds.getValue(Post.class);
                            posts.add(post);
                        }
                        RecyclerView recyclerView = binding.postRv;
                        PostAdapter adapter = new PostAdapter(posts);
                        recyclerView.setAdapter(adapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        }
        else {
            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    posts.clear();
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Post post = ds.getValue(Post.class);
                        posts.add(post);
                    }
                    RecyclerView recyclerView = binding.postRv;
                    PostAdapter adapter = new PostAdapter(posts);
                    recyclerView.setAdapter(adapter);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }
}