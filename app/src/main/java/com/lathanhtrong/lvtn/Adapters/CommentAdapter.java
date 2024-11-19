package com.lathanhtrong.lvtn.Adapters;

import android.app.Activity;
import android.app.Application;
import android.app.ProgressDialog;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lathanhtrong.lvtn.R;
import com.lathanhtrong.lvtn.Values;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CommentAdapter extends RecyclerView.ViewHolder {

    TextView txtTitle, txtTime, txtContent;
    ShapeableImageView imgIcon;
    ImageButton btnDelete;
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    public CommentAdapter(@NonNull View itemView) {
        super(itemView);
    }

    public void Comment(Activity application, String comment_id, int post_id, String user_id, String comment, String timestamp) {
        txtTime = itemView.findViewById(R.id.txtTime);
        txtContent = itemView.findViewById(R.id.txtContent);
        btnDelete = itemView.findViewById(R.id.btnDelete);
        txtTitle = itemView.findViewById(R.id.txtTitle);
        imgIcon = itemView.findViewById(R.id.imgIcon);

        if (auth.getCurrentUser() == null) {
            btnDelete.setVisibility(View.GONE);
        }
        else {
            if (auth.getCurrentUser().getUid().equals(user_id)) {
                btnDelete.setVisibility(View.VISIBLE);
            }
            else {
                btnDelete.setVisibility(View.GONE);
            }
        }
        DatabaseReference ref = FirebaseDatabase.getInstance(Values.region).getReference("Users");
        ref.child(user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue().toString();
                    txtTitle.setText(name);
                    String profile = snapshot.child("image").getValue().toString();
                    Glide.with(application).load(profile).into(imgIcon);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (auth.getCurrentUser() == null) {
                    return;
                }
                if (auth.getCurrentUser().getUid().equals(user_id)) {
                    ProgressDialog progressDialog = new ProgressDialog(application);
                    progressDialog.setMessage(application.getResources().getString(R.string.deleting_comment));
                    progressDialog.setCancelable(false);
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();

                    DatabaseReference ref = FirebaseDatabase.getInstance(Values.region).getReference("Posts").child(String.valueOf(post_id)).child("comments");
                    ref.child(comment_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(application, application.getResources().getString(R.string.del_comment_success), Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(application, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    });
                }
            }
        });

        txtTime.setText(timestamp);
        txtContent.setText(comment);
    }
}
