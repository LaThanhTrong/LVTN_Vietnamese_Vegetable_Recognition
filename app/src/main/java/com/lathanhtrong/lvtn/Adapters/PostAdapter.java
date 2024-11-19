package com.lathanhtrong.lvtn.Adapters;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lathanhtrong.lvtn.Activities.CommentActivity;
import com.lathanhtrong.lvtn.Activities.OpenPictureActivity;
import com.lathanhtrong.lvtn.DBHandler;
import com.lathanhtrong.lvtn.Models.Item;
import com.lathanhtrong.lvtn.Models.Post;
import com.lathanhtrong.lvtn.Others.Utils;
import com.lathanhtrong.lvtn.R;
import com.lathanhtrong.lvtn.Values;
import com.lathanhtrong.lvtn.databinding.PostRvBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder>{

    private List<Post> posts = new ArrayList<>();
    private DBHandler dbHandler;
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    Boolean isLiked = false;

    public PostAdapter(List<Post> posts) {
        this.posts = posts;
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return ViewHolder.from(parent);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        String uid = posts.get(holder.getAdapterPosition()).getUser_id();
        DatabaseReference ref = FirebaseDatabase.getInstance(Values.region).getReference("Users");
        DatabaseReference likeRef = FirebaseDatabase.getInstance(Values.region).getReference("Likes");
        DatabaseReference postRef = FirebaseDatabase.getInstance(Values.region).getReference("Posts");

        if (auth.getCurrentUser() == null) {
            holder.binding.btnDelete.setVisibility(View.GONE);
        }
        else {
            if (auth.getCurrentUser().getUid().equals(uid)) {
                holder.binding.btnDelete.setVisibility(View.VISIBLE);
            }
            else {
                holder.binding.btnDelete.setVisibility(View.GONE);
            }
        }

        // Get user's name by uid
        ref.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue().toString();
                    holder.binding.tvName.setText(name);
                    String profile = snapshot.child("image").getValue().toString();
                    Glide.with(holder.itemView.getContext())
                            .load(profile)
                            .into(holder.binding.ivIcon);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("PostAdapter", "onCancelled: " + error.getMessage());
            }
        });


        dbHandler = new DBHandler(holder.itemView.getContext());
        Item item = dbHandler.getItembyId(posts.get(holder.getAdapterPosition()).getItem_id());
        String item_clip = item.getItem_clipart() + ".png";
        String item_clipPath = "file:///android_asset/images/" + item_clip;

        Glide.with(holder.itemView.getContext())
                .load(item_clipPath)
                .into(holder.binding.itemIcon);

        if (Utils.getLanguage(holder.itemView.getContext()).equals("vi")) {
            holder.binding.itemName.setText(item.getItem_nameVi());
        }
        else {
            holder.binding.itemName.setText(item.getItem_name());
        }

        Glide.with(holder.itemView.getContext())
                .load(posts.get(holder.getAdapterPosition()).getPost_image())
                .into(holder.binding.ivImage);

        String timestamp = posts.get(holder.getAdapterPosition()).getPost_date();
        long timestampLong = Long.parseLong(timestamp);
        Date date = new Date(timestampLong);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String dateStr = sdf.format(date);
        holder.binding.tvDate.setText(dateStr);
        holder.binding.tvDescription.setText(posts.get(holder.getAdapterPosition()).getPost_desc());

        int post_id = posts.get(holder.getAdapterPosition()).getPost_id();

        likeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (auth.getCurrentUser() == null) {
                    int likeCount = (int) snapshot.child(String.valueOf(post_id)).getChildrenCount();
                    holder.binding.likeCount.setText(String.valueOf(likeCount));
                    holder.binding.likeBtnIcon.setBackground(holder.binding.getRoot().getResources().getDrawable(R.drawable.like2));
                    holder.binding.likeBtnText.setTextColor(holder.binding.getRoot().getResources().getColor(R.color.black));
                }
                else {
                    if (snapshot.child(String.valueOf(post_id)).hasChild(auth.getCurrentUser().getUid())) {
                        int likeCount = (int) snapshot.child(String.valueOf(post_id)).getChildrenCount();
                        holder.binding.likeCount.setText(String.valueOf(likeCount));
                        holder.binding.likeBtnIcon.setBackground(holder.binding.getRoot().getResources().getDrawable(R.drawable.like));
                        holder.binding.likeBtnText.setTextColor(holder.binding.getRoot().getResources().getColor(R.color.blue));
                    }
                    else {
                        int likeCount = (int) snapshot.child(String.valueOf(post_id)).getChildrenCount();
                        holder.binding.likeCount.setText(String.valueOf(likeCount));
                        holder.binding.likeBtnIcon.setBackground(holder.binding.getRoot().getResources().getDrawable(R.drawable.like2));
                        holder.binding.likeBtnText.setTextColor(holder.binding.getRoot().getResources().getColor(R.color.black));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        postRef.child(String.valueOf(post_id)).child("comments").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int commentCount = (int) snapshot.getChildrenCount();
                String comment = holder.itemView.getResources().getString(R.string.comment);
                if (commentCount > 1 && Utils.getLanguage(holder.itemView.getContext()).equals("en")) {
                    comment = holder.itemView.getResources().getString(R.string.comment) + "s";
                }
                comment = comment.substring(0, 1).toLowerCase() + comment.substring(1);
                holder.binding.commentCount.setText(String.valueOf(commentCount) + " " + comment);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.binding.likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (auth.getCurrentUser() == null) {
                    Toast.makeText(holder.itemView.getContext(), holder.itemView.getResources().getString(R.string.login_required), Toast.LENGTH_SHORT).show();
                    return;
                }
                isLiked = true;
                likeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (isLiked) {
                            if (snapshot.child(String.valueOf(post_id)).hasChild(auth.getCurrentUser().getUid())) {
                                likeRef.child(String.valueOf(post_id)).child(auth.getCurrentUser().getUid()).removeValue();
                                isLiked = false;
                            }
                            else {
                                likeRef.child(String.valueOf(post_id)).child(auth.getCurrentUser().getUid()).setValue(true);
                                isLiked = false;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

        holder.binding.ivImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(holder.itemView.getContext(), OpenPictureActivity.class);
                intent.putExtra("image", posts.get(holder.getAdapterPosition()).getPost_image());
                holder.itemView.getContext().startActivity(intent);
            }
        });

        holder.binding.commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(holder.itemView.getContext(), CommentActivity.class);
                intent.putExtra("post_id", post_id);
                holder.itemView.getContext().startActivity(intent);
            }
        });

        holder.binding.commentCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(holder.itemView.getContext(), CommentActivity.class);
                intent.putExtra("post_id", post_id);
                holder.itemView.getContext().startActivity(intent);
            }
        });

        holder.binding.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (auth.getCurrentUser() == null) {
                    return;
                }
                if (auth.getCurrentUser().getUid().equals(uid)) {
                    ProgressDialog progressDialog = new ProgressDialog(holder.itemView.getContext());
                    progressDialog.setMessage(holder.itemView.getResources().getString(R.string.deleling_post));
                    progressDialog.setCancelable(false);
                    progressDialog.setCanceledOnTouchOutside(false);
                    AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                    builder.setTitle(holder.itemView.getResources().getString(R.string.delete_post));
                    builder.setMessage(holder.itemView.getResources().getString(R.string.delete_post_confirm));
                    builder.setNegativeButton(holder.itemView.getResources().getString(R.string.close), null);
                    builder.setPositiveButton(holder.itemView.getResources().getString(R.string.delete), (dialog, which) -> {
                        progressDialog.show();
                        postRef.child(String.valueOf(post_id)).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                likeRef.child(String.valueOf(post_id)).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        progressDialog.dismiss();
                                        Toast.makeText(holder.itemView.getContext(), holder.itemView.getResources().getString(R.string.post_deleted), Toast.LENGTH_SHORT).show();
                                        notifyDataSetChanged();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(holder.itemView.getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        progressDialog.dismiss();
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(holder.itemView.getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }
                        });
                    }).create().show();
                }
            }
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private PostRvBinding binding;

        private ViewHolder(PostRvBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public static ViewHolder from(ViewGroup parent) {
            PostRvBinding binding = PostRvBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new ViewHolder(binding);
        }
    }


}
