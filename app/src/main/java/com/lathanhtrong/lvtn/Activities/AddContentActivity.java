package com.lathanhtrong.lvtn.Activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.lathanhtrong.lvtn.DBHandler;
import com.lathanhtrong.lvtn.Models.CultivateContent;
import com.lathanhtrong.lvtn.Models.Item;
import com.lathanhtrong.lvtn.R;
import com.lathanhtrong.lvtn.databinding.ActivityAddContentBinding;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.Normalizer;
import java.util.ArrayList;
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
            CultivateContent content = dbHandler.getCultivateContentbyId(culcon_id);
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
                setHtmlWithScaledImages(htmlContent);
                File imageFile = new File(getFilesDir(), "content_images/" + content.getCulcon_image() + ".png");
                if (imageFile.exists()) {
                    imageChooseUri = Uri.fromFile(imageFile);
                    binding.chooseImageText.setText(getResources().getString(R.string.image_picked));
                }
            }
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
                finish();
            }
        });
    }

    private void setHtmlWithScaledImages(String htmlContent) {
        String basePath = getFilesDir() + "/content_html_images/";
        Pattern pattern = Pattern.compile("src=\"(.*?)\"");
        Matcher matcher = pattern.matcher(htmlContent);
        StringBuffer modifiedHtml = new StringBuffer();

        while (matcher.find()) {
            String fileName = matcher.group(1);
            File imageFile = new File(basePath + fileName);

            if (imageFile.exists()) {
                try {
                    Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                    if (bitmap != null) {
                        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                        int screenWidth = (int) (displayMetrics.widthPixels / 3.4);
                        int imageWidth = bitmap.getWidth();
                        int imageHeight = bitmap.getHeight();
                        int scaledHeight = (screenWidth * imageHeight) / imageWidth;

                        // Replace the `src` with updated `src`, `width`, and `height` attributes
                        String replacement = String.format("src=\"file:///%s\" width=\"%d\" height=\"%d\"",
                                basePath + fileName, screenWidth, scaledHeight);
                        matcher.appendReplacement(modifiedHtml, replacement);
                        Uri imageUri = Uri.fromFile(imageFile);
                        imagePickUriList.add(imageUri);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                // If image file doesn't exist, keep the original `src`
                String replacement = String.format("src=\"file:///%s\"", basePath + fileName);
                matcher.appendReplacement(modifiedHtml, replacement);
            }
        }
        matcher.appendTail(modifiedHtml);
        binding.editor.setHtml(modifiedHtml.toString());
    }

    private void removeImageFromEditor() {
        String htmlContent = binding.editor.getHtml();
        Log.d("htmlContent", htmlContent);
        List<Uri> uriRemoveList = new ArrayList<>();
        if (htmlContent != null && !htmlContent.isEmpty()) {
            for (Uri uri : imagePickUriList) {
                String normalizedUri = uri.toString().replace("file:///", "file:////");
                if (!htmlContent.contains(normalizedUri)) {
                    uriRemoveList.add(uri);
                }
            }
            Log.d("uriRemoveList", uriRemoveList.toString());
            imagePickUriList.removeAll(uriRemoveList);
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


    public String modifyHtmlString(String html, List<String> savedFileNames) {
        if (html != null && !html.isEmpty()) {
            // Remove width and height attributes
            html = html.replaceAll("\\s*width=\"\\d+\"\\s*", "");
            html = html.replaceAll("\\s*height=\"\\d+\"\\s*", "");

            int imgIndex = 0;
            Pattern pattern = Pattern.compile("src=\"[^\"]+\""); // Match any src
            Matcher matcher = pattern.matcher(html);
            StringBuffer modifiedHtml = new StringBuffer();

            while (matcher.find() && imgIndex < savedFileNames.size()) {
                String replacement = "src=\"" + savedFileNames.get(imgIndex) + ".png\"";
                matcher.appendReplacement(modifiedHtml, replacement);
                imgIndex++;
            }
            matcher.appendTail(modifiedHtml);
            return modifiedHtml.toString();
            }
        return html; // Return original HTML if no modifications were made
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

    private String getImageFileNameFromUri(Uri uri) {
        String fileName = null;
        if (uri != null) {
            String uriPath = uri.getPath();
            if (uriPath != null) {
                // Extract the filename from the URI path
                fileName = uriPath.substring(uriPath.lastIndexOf('/') + 1);
                // Remove the extension
                int dotIndex = fileName.lastIndexOf('.');
                if (dotIndex != -1) {
                    fileName = fileName.substring(0, dotIndex); // Get the filename without the extension
                }
            }
        }
        return fileName;
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

    private String saveImageToInternalStorage(Uri imageUri, String directoryName, String baseName) {
        try {
            File directory = new File(getFilesDir(), directoryName);
            if (!directory.exists()) {
                directory.mkdir();
            }

            String fileName = generateUniqueImageName(directory, baseName);
            File imageFile = new File(directory, fileName);

            InputStream imageStream = getContentResolver().openInputStream(imageUri);
            if (imageStream != null) {
                Bitmap bitmap = BitmapFactory.decodeStream(imageStream);
                FileOutputStream fos = new FileOutputStream(imageFile);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.flush();
                fos.close();

                return fileName.substring(0, fileName.lastIndexOf('.'));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private List<String> saveImageListToInternalStorage(List<Uri> imageUriList, String directoryName) {
        List<String> savedFileNames = new ArrayList<>();
        Set<String> usedFileNames = new HashSet<>();

        for (Uri imageUri : imageUriList) {
            String baseName = getImageFileNameFromUri(imageUri).replaceAll("\\.png$", "");

            if (usedFileNames.contains(baseName)) {
                baseName = baseName + "_" + (System.currentTimeMillis() % 100);
            }

            String savedFileName = saveImageToInternalStorage(imageUri, directoryName, baseName);
            if (savedFileName != null) {
                savedFileNames.add(savedFileName);
                usedFileNames.add(savedFileName);
            }
        }
        return savedFileNames;
    }

    private String generateUniqueImageName(File directory, String baseName) {
        String extension = ".png";
        int counter = 0;
        String imageName;

        do {
            imageName = baseName + (counter == 0 ? "" : "_" + counter) + extension;
            counter++;
        } while (new File(directory, imageName).exists());

        return imageName;
    }

    private String normalizeString(String input) {
        if (input == null) return null;
        String nfdNormalizedString = Normalizer.normalize(input, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(nfdNormalizedString).replaceAll("").replaceAll("đ", "d").replaceAll("Đ", "D").toLowerCase();
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
        String htmlContent = binding.editor.getHtml();
        String fileName = getImageFileNameFromUri(imageChooseUri);

        String imageFileName = saveImageToInternalStorage(imageChooseUri, "content_images", fileName);
        if (imageFileName == null) {
            progressDialog.dismiss();
            return;
        }

        ArrayList<String> imageFileNames = new ArrayList<>();
        if (!imagePickUriList.isEmpty()) {
            imageFileNames = (ArrayList<String>) saveImageListToInternalStorage(imagePickUriList, "content_html_images");
        }

        htmlContent = modifyHtmlString(htmlContent, imageFileNames);
        Log.d("imagePickUriList", imagePickUriList.toString());
        Log.d("imageFileNames", imageFileNames.toString());
        Log.d("saveContent", "saveContent: " + htmlContent);
        int cul_id = Integer.parseInt(selectedCultivateId);
        int item_id = Integer.parseInt(selectedItemId);
        String culcon_name = binding.culconNameEn.getEditText().getText().toString();
        String culcon_nameVi = binding.culconNameVi.getEditText().getText().toString();
        String culcon_nameVi2 = normalizeString(culcon_nameVi);
        String culcon_description = binding.culconDescEn.getEditText().getText().toString();
        String culcon_descriptionVi = binding.culconDescVi.getEditText().getText().toString();
        String culcon_html = htmlContent;

        long res;
        if (getCulcon_id() == -1) {
            CultivateContent content = new CultivateContent(cul_id, item_id, culcon_name, culcon_nameVi, culcon_nameVi2, culcon_description, culcon_descriptionVi, imageFileName, culcon_html);
            res = dbHandler.addCultivateContent(content);
        }
        else {
            CultivateContent content = new CultivateContent(getCulcon_id(), cul_id, item_id, culcon_name, culcon_nameVi, culcon_nameVi2, culcon_description, culcon_descriptionVi, imageFileName, culcon_html);
            res = dbHandler.modifyCultivateContent(content);
        }
        progressDialog.dismiss();
        if (res != -1) {
            Toast.makeText(this, getString(R.string.saveContentSuccess), Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(this, getString(R.string.saveContentFail), Toast.LENGTH_SHORT).show();
        }
    }
}