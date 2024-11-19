package com.lathanhtrong.lvtn.Activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.lathanhtrong.lvtn.DBHandler;
import com.lathanhtrong.lvtn.Models.Item;
import com.lathanhtrong.lvtn.Models.Post;
import com.lathanhtrong.lvtn.R;
import com.lathanhtrong.lvtn.Values;
import com.lathanhtrong.lvtn.databinding.ActivityAddPostBinding;

import java.util.ArrayList;
import java.util.List;

public class AddPostActivity extends AppCompatActivity {

    private ActivityAddPostBinding binding;
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private DBHandler dbHandler;
    private Uri imageChooseUri;
    private String uploadedImageUrl = null;
    private String selectedItemId;
    private String selectedItemTitle;
    private List<String> itemTitleList, itemIdList;
    private static final int CHOOSE_IMAGE_REQUEST = 2;

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddPostBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FirebaseUser user = auth.getCurrentUser();
        binding.usernameTv.setText(user.getDisplayName());
        binding.emailTv.setText(user.getEmail());
        Glide.with(this).load(user.getPhotoUrl()).into(binding.profileIv);
        Glide.with(this).load(user.getPhotoUrl()).into(binding.profileIv2);

        dbHandler = new DBHandler(this);
        loadItemsList();

        binding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        binding.cbbItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String [] itemItems = new String[itemTitleList.size()];
                for (int i = 0; i < itemTitleList.size(); i++) {
                    itemItems[i] = itemTitleList.get(i);
                }

                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(AddPostActivity.this);
                builder.setTitle(getString(R.string.choose_plant));
                builder.setItems(itemItems, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedItemId = itemIdList.get(which);
                        selectedItemTitle = itemTitleList.get(which);
                        binding.cbbItem.setText(selectedItemTitle);
                    }
                }).show();
            }
        });

        binding.imageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageChooser();
            }
        });

        binding.saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePost();
            }
        });
    }

    private void loadItemsList() {
        itemIdList = new ArrayList<>();
        itemTitleList = new ArrayList<>();
        SharedPreferences preferences = this.getSharedPreferences("LANGUAGE_SETTINGS", this.MODE_PRIVATE);
        String language = preferences.getString("language", "en");
        ArrayList<Item> items = new ArrayList<>();
        items = dbHandler.getItems();
        ArrayList<Item> displayItems = new ArrayList<>();
        for (Item item : items) {
            if (language.equals("vi")) {
                Item displayItem = new Item(item.getItem_id(), item.getItem_nameVi(), item.getItem_nameVi(), item.getItem_nameVi2(),
                        item.getItem_sname(), item.getItem_descriptionVi(), item.getItem_descriptionVi(),
                        item.getItem_image(), item.getItem_clipart());
                displayItems.add(displayItem);
            } else {
                displayItems.add(item);
            }
        }
        for (Item item : displayItems) {
            itemIdList.add(String.valueOf(item.getItem_id()));
            itemTitleList.add(item.getItem_name());
        }
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.selectPicture)), CHOOSE_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                if (requestCode == CHOOSE_IMAGE_REQUEST) {
                    imageChooseUri = imageUri;
                    binding.chooseImageText.setText(getResources().getString(R.string.image_picked));
                }
            }
        }
    }

    private boolean validateInput() {
        if (selectedItemId == null || selectedItemId.isEmpty()) {
            binding.cbbItem.setError(getString(R.string.choose_plant));
            return false;
        }

        if (binding.descEt.getText().toString().isEmpty()) {
            binding.descEt.setError(getString(R.string.errDescription));
            return false;
        }

        if (imageChooseUri == null) {
            binding.chooseImageText.setError(getString(R.string.choose_image));
            return false;
        }

        return true;
    }

    private void saveToFireBase(Uri imageUri, ProgressDialog progressDialog) {
        String uid = auth.getCurrentUser().getUid();
        long currentTimeMillis = System.currentTimeMillis();
        String imageFilePathAndName = "post_images/" + uid + "/" + "image_" + System.currentTimeMillis();
        StorageReference storageReference = FirebaseStorage.getInstance().getReference(imageFilePathAndName);
        storageReference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful());
                uploadedImageUrl = uriTask.getResult().toString();

                int post_id = (int) (currentTimeMillis % Integer.MAX_VALUE);
                String user_id = uid;
                int item_id = Integer.parseInt(selectedItemId);
                String post_date = String.valueOf(System.currentTimeMillis());
                String post_desc = binding.descEt.getText().toString();
                String post_image = uploadedImageUrl;
                Post post = new Post(post_id, user_id, item_id, post_date, post_desc, post_image);

                DatabaseReference ref = FirebaseDatabase.getInstance(Values.region).getReference("Posts");
                ref.child(String.valueOf(post_id)).setValue(post).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressDialog.dismiss();
                        Toast.makeText(AddPostActivity.this, getString(R.string.post_saved), Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(AddPostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
                progressDialog.dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(AddPostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void savePost() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.savingPost));
        progressDialog.setCancelable(false);
        progressDialog.show();

        if (!validateInput()) {
            progressDialog.dismiss();
            return;
        }

        saveToFireBase(imageChooseUri, progressDialog);
    }
}