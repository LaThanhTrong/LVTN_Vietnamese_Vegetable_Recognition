package com.lathanhtrong.lvtn.Activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.lathanhtrong.lvtn.DBHandler;
import com.lathanhtrong.lvtn.Models.CultivateContent;
import com.lathanhtrong.lvtn.Models.Item;
import com.lathanhtrong.lvtn.R;
import com.lathanhtrong.lvtn.Values;
import com.lathanhtrong.lvtn.databinding.ActivityAddContentBinding;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.wasabeef.richeditor.RichEditor;

public class AddContentActivity extends AppCompatActivity {

    private ActivityAddContentBinding binding;
    private Uri imageChooseUri;
    private List<Uri> imagePickUriList;
    private String selectedCultivateId, selectedItemId;
    private String selectedCultivateTitle, selectedItemTitle;
    private List<String> cultivateTitleList, cultivateIdList;
    private List<String> itemTitleList, itemIdList;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CHOOSE_IMAGE_REQUEST = 2;
    private DBHandler dbHandler;
    private int culcon_id = -1;
    private String uploadedImageUrl = null;
    private List<String> uploadedHtmlImageUrls = new ArrayList<>();
    private int uploadCounter = 0;

    public int getCulcon_id() {
        return culcon_id;
    }

    public void setCulcon_id(int culcon_id) {
        this.culcon_id = culcon_id;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddContentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.editor.getSettings().setAllowFileAccess(true);

        Intent intent = getIntent();
        setCulcon_id(intent.getIntExtra("culcon_id", -1));

        dbHandler = new DBHandler(this);

        RichEditor editor = binding.editor;
        imagePickUriList = new ArrayList<>();

        loadCultivateList();
        loadItemsList();

        if (culcon_id != -1) {
            DatabaseReference ref = FirebaseDatabase.getInstance(Values.region).getReference("CultivateContent");
            ref.child(String.valueOf(culcon_id)).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    CultivateContent content = snapshot.getValue(CultivateContent.class);
                    if (content == null) {
                        finish();
                    }
                    else{
                        binding.title.setText(getString(R.string.modify_content));
                        binding.culconNameEn.getEditText().setText(content.getCulcon_name());
                        binding.culconNameVi.getEditText().setText(content.getCulcon_nameVi());
                        binding.culconDescEn.getEditText().setText(content.getCulcon_description());
                        binding.culconDescVi.getEditText().setText(content.getCulcon_descriptionVi());
                        selectedCultivateId = String.valueOf(content.getCul_id());
                        selectedItemId = String.valueOf(content.getItem_id());
                        for (int i = 0; i < cultivateIdList.size(); i++) {
                            if (cultivateIdList.get(i).equals(selectedCultivateId)) {
                                selectedCultivateTitle = cultivateTitleList.get(i);
                                binding.cbbCultivate.setText(selectedCultivateTitle);
                                break;
                            }
                        }
                        for (int i = 0; i < itemIdList.size(); i++) {
                            if (itemIdList.get(i).equals(selectedItemId)) {
                                selectedItemTitle = itemTitleList.get(i);
                                binding.cbbItem.setText(selectedItemTitle);
                                break;
                            }
                        }
                        String htmlContent = content.getCulcon_html();
                        extractImageUris(htmlContent);
                        setHtmlWithScaledImages(htmlContent);
                        imageChooseUri = Uri.parse(content.getCulcon_image());
                        binding.chooseImageText.setText(getResources().getString(R.string.image_picked));

                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        loadCustomFont();

        binding.boldButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.setBold();
            }
        });

        binding.italicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.setItalic();
            }
        });

        binding.imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagePicker();
            }
        });

        binding.undoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.undo();
            }
        });

        binding.redoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.redo();
            }
        });

        binding.saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveContent();
            }
        });

        binding.imageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageChooser();
            }
        });

        binding.cbbCultivate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String [] cultivateItems = new String[cultivateTitleList.size()];
                for (int i = 0; i < cultivateTitleList.size(); i++) {
                    cultivateItems[i] = cultivateTitleList.get(i);
                }

                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(AddContentActivity.this);
                builder.setTitle(getString(R.string.choose_cultivate));
                builder.setItems(cultivateItems, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedCultivateId = cultivateIdList.get(which);
                        selectedCultivateTitle = cultivateTitleList.get(which);
                        binding.cbbCultivate.setText(selectedCultivateTitle);
                    }
                }).show();
            }
        });

        binding.cbbItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String [] itemItems = new String[itemTitleList.size()];
                for (int i = 0; i < itemTitleList.size(); i++) {
                    itemItems[i] = itemTitleList.get(i);
                }

                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(AddContentActivity.this);
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

        binding.editor.setOnTextChangeListener(new RichEditor.OnTextChangeListener() {
            @Override
            public void onTextChange(String text) {
                removeImageFromEditor();
            }
        });

        binding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (!isFinishing()) {
            finish();
        }
    }

    private void extractImageUris(String htmlContent) {

        String regex = "<img\\s+[^>]*src=[\"']([^\"']+)[\"'][^>]*>";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(htmlContent);

        while (matcher.find()) {
            String url = matcher.group(1);
            if (url != null) {
                imagePickUriList.add(Uri.parse(url));
            }
        }
    }

    public interface OnImageDownloadedListener {
        void onImageDownloaded(Bitmap bitmap);
    }

    private void downloadImageFromUrl(String imageUrl, OnImageDownloadedListener listener) {
        Glide.with(this)
                .asBitmap()
                .load(imageUrl)
                .placeholder(R.drawable.notfound)
                .error(R.drawable.notfound)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        listener.onImageDownloaded(resource);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        listener.onImageDownloaded(null);
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        listener.onImageDownloaded(null);
                    }
                });
    }

    private void processNextImage(final Matcher matcher, final StringBuffer modifiedHtml) {
        if (matcher.find()) {
            String imageUrl = matcher.group(1);

            downloadImageFromUrl(imageUrl, new OnImageDownloadedListener() {
                @Override
                public void onImageDownloaded(Bitmap bitmap) {
                    if (isFinishing()) {
                        return;
                    }

                    if (bitmap != null) {
                        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                        int screenWidth = (int) (displayMetrics.widthPixels / 3.4);
                        int imageWidth = bitmap.getWidth();
                        int imageHeight = bitmap.getHeight();
                        int scaledHeight = (screenWidth * imageHeight) / imageWidth;

                        try {
                            String replacement = String.format("src=\"%s\" width=\"%d\" height=\"%d\"", imageUrl, screenWidth, scaledHeight);
                            matcher.appendReplacement(modifiedHtml, replacement);
                        } catch (IllegalStateException e) {
                            e.printStackTrace();
                            matcher.appendReplacement(modifiedHtml, matcher.group(0));
                        }
                    } else {
                        matcher.appendReplacement(modifiedHtml, matcher.group(0));
                    }
                    processNextImage(matcher, modifiedHtml);
                }
            });
        } else {
            try {
                matcher.appendTail(modifiedHtml);
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            binding.editor.setHtml(modifiedHtml.toString());
        }
    }

    private void setHtmlWithScaledImages(String htmlContent) {
        Pattern pattern = Pattern.compile("src=\"(.*?)\"");
        Matcher matcher = pattern.matcher(htmlContent);
        StringBuffer modifiedHtml = new StringBuffer();
        processNextImage(matcher, modifiedHtml);
    }

    private void removeImageFromEditor() {
        String htmlContent = binding.editor.getHtml();
        List<Uri> uriRemoveList = new ArrayList<>();

        if (htmlContent != null && !htmlContent.isEmpty()) {
            htmlContent = Html.fromHtml(htmlContent).toString();

            for (Uri uri : imagePickUriList) {
                String uriString = uri.toString();
                if (!htmlContent.contains(uriString)) {
                    uriRemoveList.add(uri);
                }
            }
            imagePickUriList.removeAll(uriRemoveList);

            imagePickUriList = new ArrayList<>(new HashSet<>(imagePickUriList));
        }
    }

    private void loadCultivateList() {
        cultivateIdList = new ArrayList<>();
        cultivateTitleList = new ArrayList<>();

        for (int i = 1; i <= 6; i++) {
            cultivateIdList.add(String.valueOf(i));
        }
        cultivateTitleList.add(getResources().getString(R.string.cultivar));
        cultivateTitleList.add(getResources().getString(R.string.landscape));
        cultivateTitleList.add(getResources().getString(R.string.plant_propagate));
        cultivateTitleList.add(getResources().getString(R.string.planting));
        cultivateTitleList.add(getResources().getString(R.string.plant_caring));
        cultivateTitleList.add(getResources().getString(R.string.harvest));
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


    public String modifyHtmlString(String html, List<String> imageUrls) {
        if (html != null && !html.isEmpty()) {
            // Remove width and height attributes
            html = html.replaceAll("\\s*width=\"\\d+\"\\s*", "");
            html = html.replaceAll("\\s*height=\"\\d+\"\\s*", "");

            int imgIndex = 0;
            Pattern pattern = Pattern.compile("src=\"[^\"]+\"");
            Matcher matcher = pattern.matcher(html);
            StringBuffer modifiedHtml = new StringBuffer();

            while (matcher.find() && imgIndex < imageUrls.size()) {
                String replacement = "src=\"" + imageUrls.get(imgIndex) + "\"";
                matcher.appendReplacement(modifiedHtml, replacement);
                imgIndex++;
            }
            matcher.appendTail(modifiedHtml);
            return modifiedHtml.toString();
            }
        return html;
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void loadCustomFont() {
        String css = "@font-face {" +
                "font-family: 'BeVietnamProRegular';" +
                "src: url('file:///android_asset/fonts/bevietnampro_regular.ttf');" +
                "}" +
                "body {" +
                "font-family: 'BeVietnamProRegular', sans-serif;" +
                "text-align: justify;" + // Ensures text justification
                "}";

        binding.editor.getSettings().setJavaScriptEnabled(true);
        binding.editor.setEditorHeight(200);
        binding.editor.setPlaceholder(getString(R.string.insertContentHere));
        binding.editor.setEditorFontSize(20);
        binding.editor.setPadding(20, 20, 20, 20);

        binding.editor.loadUrl("javascript:(function() {" +
                "var style = document.createElement('style');" +
                "style.type = 'text/css';" +
                "style.innerHTML = '" + css + "';" +
                "document.head.appendChild(style);" +
                "})()");
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.selectPicture)), PICK_IMAGE_REQUEST);
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
                if (requestCode == PICK_IMAGE_REQUEST) {
                    insertImageFromGallery(imageUri);
                    imagePickUriList.add(imageUri);
                } else if (requestCode == CHOOSE_IMAGE_REQUEST) {
                    imageChooseUri = imageUri;
                    binding.chooseImageText.setText(getResources().getString(R.string.image_picked));
                }
            }
        }
    }

    private void insertImageFromGallery(Uri imageUri) {
        try {
            InputStream imageStream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(imageStream);

            if (bitmap != null) {
                DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                int screenWidth = (int) (displayMetrics.widthPixels / 3.4);
                int imageWidth = bitmap.getWidth();
                int imageHeight = bitmap.getHeight();
                int scaledHeight = (screenWidth * imageHeight) / imageWidth;

                binding.editor.insertImage(imageUri.toString(), "Image", screenWidth, scaledHeight);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isContentEmpty(String html) {
        if (html == null || html.trim().isEmpty()) {
            return true;
        }
        String strippedContent = html.replaceAll("(?i)<br\\s*/?>|<p>|</p>|&nbsp;", "").trim();
        return strippedContent.isEmpty();
    }

    private boolean validateInput() {
        if (selectedCultivateId == null || selectedItemId == null || selectedCultivateId.isEmpty() || selectedItemId.isEmpty()) {
            Toast.makeText(this, getString(R.string.errCbb), Toast.LENGTH_SHORT).show();
            return false;
        }
        if (binding.culconNameEn.getEditText().getText() == null || binding.culconNameEn.getEditText().getText().toString().isEmpty()) {
            binding.culconNameEn.setError(getString(R.string.errNameEn));
            return false;
        }
        if (binding.culconNameVi.getEditText().getText() == null || binding.culconNameVi.getEditText().getText().toString().isEmpty()) {
            binding.culconNameVi.setError(getString(R.string.errNameVi));
            return false;
        }
        if (binding.culconDescEn.getEditText().getText() == null || binding.culconDescEn.getEditText().getText().toString().isEmpty()) {
            binding.culconDescEn.setError(getString(R.string.errDescriptionEn));
            return false;
        }
        if (binding.culconDescVi.getEditText().getText() == null || binding.culconDescVi.getEditText().getText().toString().isEmpty()) {
            binding.culconDescVi.setError(getString(R.string.errDescriptionVi));
            return false;
        }
        if (imageChooseUri == null) {
            Toast.makeText(this, getString(R.string.errImage), Toast.LENGTH_SHORT).show();
            return false;
        }
        if (isContentEmpty(binding.editor.getHtml())) {
            Toast.makeText(this, getString(R.string.errHtml), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private String normalizeString(String input) {
        if (input == null) return null;
        String nfdNormalizedString = Normalizer.normalize(input, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(nfdNormalizedString).replaceAll("").replaceAll("đ", "d").replaceAll("Đ", "D").toLowerCase();
    }

    private Uri saveBitmapAsLocalFile(Bitmap bitmap) {
        try {
            File tempDir = new File(getFilesDir(), "temp");
            if (!tempDir.exists()) {
                tempDir.mkdir();
            }

            File file = new File(tempDir, "downloaded_image" + ".jpg");
            FileOutputStream fos = new FileOutputStream(file, false);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            return Uri.fromFile(file);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void uploadFileToFirebase(Uri fileUri, List<Uri> imagePickUriList, ProgressDialog progressDialog) {
        String imageFilePathAndName = "content_images/" + "image_" + System.currentTimeMillis();
        StorageReference storageReference = FirebaseStorage.getInstance().getReference(imageFilePathAndName);

        storageReference.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful());
                uploadedImageUrl = uriTask.getResult().toString();

                uploadHtmlImages(imagePickUriList, progressDialog);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(AddContentActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveToFireBase(Uri imageChooseUri, List<Uri> imagePickUriList, ProgressDialog progressDialog) {
        if (imageChooseUri.toString().startsWith("http")) {
            downloadImageFromUrl(imageChooseUri.toString(), new OnImageDownloadedListener() {
                @Override
                public void onImageDownloaded(Bitmap bitmap) {
                    if (bitmap != null) {
                        Uri localUri = saveBitmapAsLocalFile(bitmap);
                        if (localUri != null) {
                            uploadFileToFirebase(localUri, imagePickUriList, progressDialog);
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(AddContentActivity.this, "Failed to save image locally", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        progressDialog.dismiss();
                    }
                }
            });
        } else {
            uploadFileToFirebase(imageChooseUri, imagePickUriList, progressDialog);
        }
    }

    private int getLocalImageCount(List<Uri> imagePickUriList) {
        int count = 0;
        for (Uri uri : imagePickUriList) {
            if (uri.toString().startsWith("content://")) {
                count++;
            }
        }
        return count;
    }

    private void addContent(CultivateContent content, ProgressDialog progressDialog) {
        DatabaseReference ref = FirebaseDatabase.getInstance(Values.region).getReference("CultivateContent");
        Query nameQuery = ref.orderByChild("cul_id").equalTo(content.getCul_id());

        nameQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    boolean combinationExistsEn = false;
                    boolean combinationExistsVi = false;

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Integer existingItemId = snapshot.child("item_id").getValue(Integer.class);
                        String existingName = snapshot.child("culcon_name").getValue(String.class);
                        String existingNameVi = snapshot.child("culcon_nameVi").getValue(String.class);

                        if (existingItemId != null && existingItemId.equals(content.getItem_id()) &&
                                existingName != null && existingName.equals(content.getCulcon_name())) {
                            combinationExistsEn = true;
                        }

                        if (existingItemId != null && existingItemId.equals(content.getItem_id()) &&
                                existingNameVi != null && existingNameVi.equals(content.getCulcon_nameVi())) {
                            combinationExistsVi = true;
                        }
                    }

                    if (combinationExistsEn) {
                        Toast.makeText(AddContentActivity.this, getResources().getString(R.string.err_insert_contentNameExist), Toast.LENGTH_SHORT).show();
                    } else if (combinationExistsVi) {
                        Toast.makeText(AddContentActivity.this, getResources().getString(R.string.err_insert_contentNameViExist), Toast.LENGTH_SHORT).show();
                    } else {
                        long currentTimeMillis = System.currentTimeMillis();
                        int id = (int) (currentTimeMillis % Integer.MAX_VALUE);
                        content.setCulcon_id(id);
                        ref.child(String.valueOf(id)).setValue(content).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                progressDialog.dismiss();
                                Toast.makeText(AddContentActivity.this, getResources().getString(R.string.saveContentSuccess), Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(AddContentActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                } else {
                    long currentTimeMillis = System.currentTimeMillis();
                    int id = (int) (currentTimeMillis % Integer.MAX_VALUE);
                    content.setCulcon_id(id);
                    ref.child(String.valueOf(id)).setValue(content).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            progressDialog.dismiss();
                            Toast.makeText(AddContentActivity.this, getResources().getString(R.string.saveContentSuccess), Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(AddContentActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AddContentActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void modifyContent(CultivateContent content, ProgressDialog progressDialog) {
        DatabaseReference ref = FirebaseDatabase.getInstance(Values.region).getReference("CultivateContent");
        Query nameQuery = ref.orderByChild("cul_id").equalTo(content.getCul_id());

        nameQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    boolean combinationExistsEn = false;
                    boolean combinationExistsVi = false;

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String existingName = snapshot.child("culcon_name").getValue(String.class);
                        String existingNameVi = snapshot.child("culcon_nameVi").getValue(String.class);
                        Integer existingItemId = snapshot.child("item_id").getValue(Integer.class);
                        Integer existingId = snapshot.child("culcon_id").getValue(Integer.class);

                        if (existingItemId != null && existingItemId.equals(content.getItem_id()) &&
                                existingName != null && existingName.equals(content.getCulcon_name()) &&
                                existingId != null && !existingId.equals(content.getCulcon_id())) {
                            combinationExistsEn = true;
                        }

                        if (existingItemId != null && existingItemId.equals(content.getItem_id()) &&
                                existingNameVi != null && existingNameVi.equals(content.getCulcon_nameVi()) &&
                                existingId != null && !existingId.equals(content.getCulcon_id())) {
                            combinationExistsVi = true;
                        }
                    }
                    if (combinationExistsEn) {
                        Toast.makeText(AddContentActivity.this, getResources().getString(R.string.err_insert_contentNameExist), Toast.LENGTH_SHORT).show();
                    } else if (combinationExistsVi) {
                        Toast.makeText(AddContentActivity.this, getResources().getString(R.string.err_insert_contentNameViExist), Toast.LENGTH_SHORT).show();
                    } else {
                        ref.child(String.valueOf(content.getCulcon_id())).setValue(content).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                progressDialog.dismiss();
                                Toast.makeText(AddContentActivity.this, getResources().getString(R.string.saveContentSuccess), Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(AddContentActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else {
                    ref.child(String.valueOf(content.getCulcon_id())).setValue(content).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            progressDialog.dismiss();
                            Toast.makeText(AddContentActivity.this, getResources().getString(R.string.saveContentSuccess), Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(AddContentActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AddContentActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadHtmlImages(List<Uri> imagePickUriList, ProgressDialog progressDialog) {
        int localImageCount = getLocalImageCount(imagePickUriList);
        if (localImageCount == 0) {
            String htmlContent = binding.editor.getHtml();
            htmlContent = modifyHtmlString(htmlContent, uploadedHtmlImageUrls);
            int cul_id = Integer.parseInt(selectedCultivateId);
            int item_id = Integer.parseInt(selectedItemId);
            String culcon_name = binding.culconNameEn.getEditText().getText().toString();
            String culcon_nameVi = binding.culconNameVi.getEditText().getText().toString();
            String culcon_nameVi2 = normalizeString(culcon_nameVi);
            String culcon_description = binding.culconDescEn.getEditText().getText().toString();
            String culcon_descriptionVi = binding.culconDescVi.getEditText().getText().toString();
            String culcon_image = uploadedImageUrl;
            String culcon_html = htmlContent;

            if (getCulcon_id() == -1) {
                // Insert
                CultivateContent content = new CultivateContent(cul_id, item_id, culcon_name, culcon_nameVi, culcon_nameVi2, culcon_description, culcon_descriptionVi, culcon_image, culcon_html);
                addContent(content, progressDialog);
            }
            else {
                // Update
                CultivateContent content = new CultivateContent(getCulcon_id(), cul_id, item_id, culcon_name, culcon_nameVi, culcon_nameVi2, culcon_description, culcon_descriptionVi, culcon_image, culcon_html);
                modifyContent(content, progressDialog);
            }
            progressDialog.dismiss();
            return;
        }

        uploadedHtmlImageUrls = new ArrayList<>(Collections.nCopies(imagePickUriList.size(), ""));
        uploadCounter = 0;

        for (int i = 0; i < imagePickUriList.size(); i++) {
            Uri imageUri = imagePickUriList.get(i);

            if (imageUri.toString().startsWith("content://")) {
                int finalI = i;
                String htmlImagesFilePathAndName = "content_html_images/" + "image_" + System.currentTimeMillis();
                StorageReference listStorageReference = FirebaseStorage.getInstance().getReference(htmlImagesFilePathAndName);

                listStorageReference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String uploadedListImageUrl = uri.toString();
                                uploadedHtmlImageUrls.set(finalI, uploadedListImageUrl);
                                uploadCounter++;

                                if (uploadCounter == getLocalImageCount(imagePickUriList)) {
                                    String htmlContent = binding.editor.getHtml();
                                    htmlContent = modifyHtmlString(htmlContent, uploadedHtmlImageUrls);
                                    int cul_id = Integer.parseInt(selectedCultivateId);
                                    int item_id = Integer.parseInt(selectedItemId);
                                    String culcon_name = binding.culconNameEn.getEditText().getText().toString();
                                    String culcon_nameVi = binding.culconNameVi.getEditText().getText().toString();
                                    String culcon_nameVi2 = normalizeString(culcon_nameVi);
                                    String culcon_description = binding.culconDescEn.getEditText().getText().toString();
                                    String culcon_descriptionVi = binding.culconDescVi.getEditText().getText().toString();
                                    String culcon_image = uploadedImageUrl;
                                    String culcon_html = htmlContent;

                                    if (getCulcon_id() == -1) {
                                        // Insert
                                        CultivateContent content = new CultivateContent(cul_id, item_id, culcon_name, culcon_nameVi, culcon_nameVi2, culcon_description, culcon_descriptionVi, culcon_image, culcon_html);
                                        addContent(content, progressDialog);

                                    }
                                    else {
                                        // Update
                                        CultivateContent content = new CultivateContent(getCulcon_id(), cul_id, item_id, culcon_name, culcon_nameVi, culcon_nameVi2, culcon_description, culcon_descriptionVi, culcon_image, culcon_html);
                                        modifyContent(content, progressDialog);
                                    }
                                    progressDialog.dismiss();
                                }
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(AddContentActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
            else {
                String uploadedListImageUrl = imageUri.toString();
                uploadedHtmlImageUrls.set(i, uploadedListImageUrl);
            }
        }
    }

    private void saveContent() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.savingContent));
        progressDialog.setCancelable(false);
        progressDialog.show();

        removeImageFromEditor();

        if (!validateInput()) {
            progressDialog.dismiss();
            return;
        }

        extractImageUris(binding.editor.getHtml());
        saveToFireBase(imageChooseUri, imagePickUriList, progressDialog);
    }
}