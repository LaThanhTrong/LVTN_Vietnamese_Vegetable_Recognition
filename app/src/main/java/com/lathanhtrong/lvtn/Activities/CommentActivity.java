package com.lathanhtrong.lvtn.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lathanhtrong.lvtn.Adapters.CommentAdapter;
import com.lathanhtrong.lvtn.Models.Comment;
import com.lathanhtrong.lvtn.R;
import com.lathanhtrong.lvtn.Values;
import com.lathanhtrong.lvtn.databinding.ActivityCommentBinding;

import java.text.SimpleDateFormat;

public class CommentActivity extends AppCompatActivity {

    private ActivityCommentBinding binding;
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private DatabaseReference postRef, userRef;
    private FirebaseRecyclerAdapter<Comment, CommentAdapter> firebaseRecyclerAdapter;

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = getIntent();
        int post_id = intent.getIntExtra("post_id", -1);
        if (post_id == -1) {
            finish();
        }
        else{
            if (firebaseRecyclerAdapter != null) {
                firebaseRecyclerAdapter.startListening();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCommentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (auth.getCurrentUser() != null) {
            FirebaseUser user = auth.getCurrentUser();
            Glide.with(this).load(user.getPhotoUrl()).into(binding.profileIv);
        }

        Intent intent = getIntent();
        int post_id = intent.getIntExtra("post_id", -1);

        userRef = FirebaseDatabase.getInstance(Values.region).getReference().child("Users");
        postRef = FirebaseDatabase.getInstance(Values.region).getReference().child("Posts").child(String.valueOf(post_id)).child("comments");

        binding.commentRv.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        binding.commentRv.setLayoutManager(layoutManager);

        FirebaseRecyclerOptions<Comment> options = new FirebaseRecyclerOptions.Builder<Comment>()
                .setQuery(postRef.orderByChild("timestamp"), Comment.class)
                .build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Comment, CommentAdapter>(options) {
            @Override
            protected void onBindViewHolder(@NonNull CommentAdapter holder, int position, @NonNull Comment model) {
                holder.Comment(CommentActivity.this, model.getComment_id(), model.getPost_id(), model.getUser_id(), model.getComment(), model.getTimestamp());
            }

            @NonNull
            @Override
            public CommentAdapter onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_rv, parent, false);
                return new CommentAdapter(view);
            }
        };

        binding.commentRv.setAdapter(firebaseRecyclerAdapter);

        binding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        binding.send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (auth.getCurrentUser() == null) {
                    Toast.makeText(CommentActivity.this, getResources().getString(R.string.login_required), Toast.LENGTH_SHORT).show();
                    return;
                }
                String comment = binding.writeComment.getText().toString();
                if (comment.isEmpty()) {
                    Toast.makeText(CommentActivity.this, getResources().getString(R.string.err_comment), Toast.LENGTH_SHORT).show();
                    return;
                }
                userRef.child(auth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String userid = snapshot.child("id").getValue().toString();
                            Commentfeature(userid);
                            binding.writeComment.setText("");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            private void Commentfeature(String userid) {
                String comment_post = binding.writeComment.getText().toString();
                if (comment_post.isEmpty()) {
                    Toast.makeText(CommentActivity.this, getResources().getString(R.string.err_comment), Toast.LENGTH_SHORT).show();
                }
                else {
                    ProgressDialog progressDialog = new ProgressDialog(CommentActivity.this);
                    progressDialog.setMessage(getResources().getString(R.string.adding_comment));
                    progressDialog.setCancelable(false);
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();

                    String timestamp = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date());
                    final String comment_id = auth.getCurrentUser().getUid() + System.currentTimeMillis();
                    Comment comment = new Comment(comment_id, post_id, userid, comment_post, timestamp);

                    postRef.child(comment_id).setValue(comment).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(CommentActivity.this, getResources().getString(R.string.add_comment_success), Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }
                            else {
                                Toast.makeText(CommentActivity.this, getResources().getString(R.string.add_comment_fail), Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(CommentActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    });
                }
            }
        });
    }
}