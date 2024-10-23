package com.lathanhtrong.lvtn.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lathanhtrong.lvtn.DBHandler;
import com.lathanhtrong.lvtn.Models.CultivateContent;
import com.lathanhtrong.lvtn.Others.Utils;
import com.lathanhtrong.lvtn.R;
import com.lathanhtrong.lvtn.Values;
import com.lathanhtrong.lvtn.databinding.ActivityCultivateContentBinding;

public class CultivateContentActivity extends AppCompatActivity {

    private ActivityCultivateContentBinding binding;
    private int culcon_id;
    private int cul_id;
    private CultivateContent culcon;
    private DBHandler dbHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHandler = new DBHandler(this);

        Intent intent = getIntent();
        culcon_id = intent.getIntExtra("culcon_id", -1);
        cul_id = intent.getIntExtra("cul_id", -1);

        binding = ActivityCultivateContentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loadContent();

        binding.wvContent.getSettings().setJavaScriptEnabled(true);
        binding.wvContent.getSettings().setDomStorageEnabled(true);
        binding.wvContent.getSettings().setAllowFileAccess(true);

        binding.back.setOnClickListener(v -> {
            onBackPressed();
        });
    }

    private void loadContent() {
        DatabaseReference ref = FirebaseDatabase.getInstance(Values.region).getReference("CultivateContent");
        ref.child(String.valueOf(culcon_id)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                culcon = snapshot.getValue(CultivateContent.class);

                if (culcon != null) {
                    String html = culcon.getCulcon_html();
                    String cul_name = "";

                    switch (cul_id) {
                        case 1:
                            cul_name = getResources().getString(R.string.cultivar);
                            break;
                        case 2:
                            cul_name = getResources().getString(R.string.landscape);
                            break;
                        case 3:
                            cul_name = getResources().getString(R.string.plant_propagate);
                            break;
                        case 4:
                            cul_name = getResources().getString(R.string.planting);
                            break;
                        case 5:
                            cul_name = getResources().getString(R.string.plant_caring);
                            break;
                        case 6:
                            cul_name = getResources().getString(R.string.harvest);
                            break;
                        default:
                            break;
                    }
                    binding.title.setText(cul_name);

                    if (Utils.getLanguage(CultivateContentActivity.this).equals("vi")) {
                        binding.tvName.setText(culcon.getCulcon_nameVi());
                    } else {
                        binding.tvName.setText(culcon.getCulcon_name());
                    }

                    binding.wvContent.loadDataWithBaseURL(null, modifyHtml(html), "text/html", "UTF-8", null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private String modifyHtml(String html) {

        String style = "<style>" +
                "@font-face {" +
                "font-family: 'BeVietnam';" +
                "src: url(\"file:///android_asset/fonts/bevietnampro_regular.ttf\");" +
                "}" +
                "body { font-family: 'BeVietnam'; font-size: 18px; text-align: justify;}" +
                "img { width: 100%; height: auto; border-radius: 8px; }" +
                "</style>";

        return style + html;
    }
}
