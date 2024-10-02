package com.lathanhtrong.lvtn;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;
import com.lathanhtrong.lvtn.Models.CultivateContent;
import com.lathanhtrong.lvtn.Models.History;
import com.lathanhtrong.lvtn.Models.Item;
import com.lathanhtrong.lvtn.Others.Utils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DBHandler extends SQLiteOpenHelper {

    private static final String DB_NAME = "lvtn";
    private static final int DB_VERSION = 1;
    private Context context;

    private static final String TABLE_ITEMS = "items";
    private static final String ITEM_ID = "item_id";
    private static final String ITEM_NAME = "item_name";
    private static final String ITEM_NAME_VI = "item_nameVi";
    private static final String ITEM_NAME_VI2 = "item_nameVi2";
    private static final String ITEM_SNAME = "item_sname";
    private static final String ITEM_DESCRIPTION = "item_description";
    private static final String ITEM_DESCRIPTION_VI = "item_descriptionVi";
    private static final String ITEM_IMAGE = "item_image";
    private static final String ITEM_CLIPART = "item_clipart";

    private static final String TABLE_CULTIVATE_CONTENT = "cultivate_content";
    private static final String CULTIVATE_CONTENT_ID = "culcon_id";
    private static final String CULTIVATE_CONTENT_CULTIVATE_ID = "cultivate_id";
    private static final String CULTIVATE_CONTENT_ITEM_ID = "item_id";
    private static final String CULTIVATE_CONTENT_NAME = "culcon_name";
    private static final String CULTIVATE_CONTENT_NAME_VI = "culcon_nameVi";
    private static final String CULTIVATE_CONTENT_NAME_VI2 = "culcon_nameVi2";
    private static final String CULTIVATE_CONTENT_DESCRIPTION = "culcon_description";
    private static final String CULTIVATE_CONTENT_DESCRIPTION_VI = "culcon_descriptionVi";
    private static final String CULTIVATE_CONTENT_IMAGE = "culcon_image";
    private static final String CULTIVATE_CONTENT_HTML = "culcon_html";

    private static final String TABLE_HISTORY = "history";
    private static final String HISTORY_ID = "history_id";
    private static final String HISTORY_ITEM_ID = "item_id";
    private static final String HISTORY_DATETIME = "history_datetime";
    private static final String HISTORY_IMAGE = "history_image";
    private static final String HISTORY_MODEL = "history_model";
    private static final String HISTORY_CONFIDENCE = "history_confidence";

    public DBHandler(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_ITEMS + " ("
                + ITEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ITEM_NAME + " TEXT UNIQUE,"
                + ITEM_NAME_VI + " TEXT UNIQUE,"
                + ITEM_NAME_VI2 + " TEXT,"
                + ITEM_SNAME + " TEXT,"
                + ITEM_DESCRIPTION + " TEXT,"
                + ITEM_DESCRIPTION_VI + " TEXT,"
                + ITEM_IMAGE + " TEXT,"
                + ITEM_CLIPART + " TEXT)";
        db.execSQL(query);
        loadItemsFromJson(db);

        String query3 = "CREATE TABLE " + TABLE_CULTIVATE_CONTENT + " ("
                + CULTIVATE_CONTENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + CULTIVATE_CONTENT_CULTIVATE_ID + " INTEGER,"
                + CULTIVATE_CONTENT_ITEM_ID + " INTEGER,"
                + CULTIVATE_CONTENT_NAME + " TEXT,"
                + CULTIVATE_CONTENT_NAME_VI + " TEXT,"
                + CULTIVATE_CONTENT_NAME_VI2 + " TEXT,"
                + CULTIVATE_CONTENT_DESCRIPTION + " TEXT,"
                + CULTIVATE_CONTENT_DESCRIPTION_VI + " TEXT,"
                + CULTIVATE_CONTENT_IMAGE + " TEXT,"
                + CULTIVATE_CONTENT_HTML + " TEXT,"
                + "FOREIGN KEY (" + CULTIVATE_CONTENT_ITEM_ID + ") REFERENCES " + TABLE_ITEMS + "(" + ITEM_ID + "), "
                + "UNIQUE(" + CULTIVATE_CONTENT_CULTIVATE_ID + ", " + CULTIVATE_CONTENT_NAME + "), "
                + "UNIQUE(" + CULTIVATE_CONTENT_CULTIVATE_ID + ", " + CULTIVATE_CONTENT_NAME_VI + ")"
                + ")";
        db.execSQL(query3);

        String query2 = "CREATE TABLE " + TABLE_HISTORY + " ("
                + HISTORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + HISTORY_ITEM_ID + " INTEGER,"
                + HISTORY_DATETIME + " TEXT,"
                + HISTORY_IMAGE + " TEXT,"
                + HISTORY_MODEL + " TEXT,"
                + HISTORY_CONFIDENCE + " TEXT,"
                + "FOREIGN KEY (" + HISTORY_ITEM_ID + ") REFERENCES " + TABLE_ITEMS + "(" + ITEM_ID + ")"
                + ")";

        db.execSQL(query2);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String query = "DROP TABLE IF EXISTS " + TABLE_ITEMS;
        String query2 = "DROP TABLE IF EXISTS " + TABLE_HISTORY;
        String query3 = "DROP TABLE IF EXISTS " + TABLE_CULTIVATE_CONTENT;
        db.execSQL(query);
        db.execSQL(query2);
        db.execSQL(query3);
        onCreate(db);
    }

    private void loadItemsFromJson(SQLiteDatabase db) {
        try {
            InputStream is = context.getAssets().open("data/items.json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            StringBuilder jsonString = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                jsonString.append(line);
            }

            reader.close();
            is.close();

            JSONArray jsonArray = new JSONArray(jsonString.toString());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject item = jsonArray.getJSONObject(i);
                int id = item.getInt("item_id");
                String name = item.getString("item_name");
                String nameVi = item.getString("item_nameVi");
                String nameVi2 = item.getString("item_nameVi2");
                String sname = item.getString("item_sname");
                String description = item.getString("item_description");
                String descriptionVi = item.getString("item_descriptionVi");
                String image = item.getString("item_image");
                String clipart = item.getString("item_clipart");

                ContentValues values = new ContentValues();
                values.put(ITEM_ID, id);
                values.put(ITEM_NAME, name);
                values.put(ITEM_NAME_VI, nameVi);
                values.put(ITEM_NAME_VI2, nameVi2);
                values.put(ITEM_SNAME, sname);
                values.put(ITEM_DESCRIPTION, description);
                values.put(ITEM_DESCRIPTION_VI, descriptionVi);
                values.put(ITEM_IMAGE, image);
                values.put(ITEM_CLIPART, clipart);
                db.insert(TABLE_ITEMS, null, values);
            }

        } catch (IOException | JSONException e) {
            e.printStackTrace();  // Print exception stack trace for debugging
        }
    }

    public ArrayList<Item> getItems() {
        ArrayList<Item> items = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_ITEMS;
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                String nameVi = cursor.getString(2);
                String nameVi2 = cursor.getString(3);
                String sname = cursor.getString(4);
                String description = cursor.getString(5);
                String descriptionVi = cursor.getString(6);
                String image = cursor.getString(7);
                String clipart = cursor.getString(8);

                Item item = new Item(id, name, nameVi, nameVi2, sname, description, descriptionVi, image, clipart);
                items.add(item);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return items;
    }

    public Item getItembyId(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_ITEMS + " WHERE " + ITEM_ID + " = " + id;
        Cursor cursor = db.rawQuery(query, null);
        Item item = null;

        if (cursor.moveToFirst()) {
            int itemId = cursor.getInt(0);
            String name = cursor.getString(1);
            String nameVi = cursor.getString(2);
            String nameVi2 = cursor.getString(3);
            String sname = cursor.getString(4);
            String description = cursor.getString(5);
            String descriptionVi = cursor.getString(6);
            String image = cursor.getString(7);
            String clipart = cursor.getString(8);
            item = new Item(itemId, name, nameVi, nameVi2, sname, description, descriptionVi, image, clipart);
        }
        cursor.close();
        db.close();
        return item;
    }

    public Item getItembyName(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        String language = Utils.getLanguage(context);
        String query;
        if (language.equals("vi")) {
            query = "SELECT * FROM " + TABLE_ITEMS + " WHERE " + ITEM_NAME_VI + " = '" + name + "'";
        } else {
            query = "SELECT * FROM " + TABLE_ITEMS + " WHERE " + ITEM_NAME + " = '" + name + "'";
        }
        Cursor cursor = db.rawQuery(query, null);
        Item item = null;

        if (cursor.moveToFirst()) {
            int itemId = cursor.getInt(0);
            String nameVi = cursor.getString(2);
            String nameVi2 = cursor.getString(3);
            String sname = cursor.getString(4);
            String description = cursor.getString(5);
            String descriptionVi = cursor.getString(6);
            String image = cursor.getString(7);
            String clipart = cursor.getString(8);
            item = new Item(itemId, name, nameVi, nameVi2, sname, description, descriptionVi, image, clipart);
        }
        cursor.close();
        db.close();
        return item;
    }

    public ArrayList<Item> searchItemsbyName(String search_name) {
        SQLiteDatabase db = this.getReadableDatabase();
        String language = Utils.getLanguage(context);
        String query;

        if (language.equals("vi")) {
            query = "SELECT * FROM " + TABLE_ITEMS + " WHERE " +
                    "lower(" + ITEM_NAME_VI2 + ") LIKE '%' || lower(?) || '%' ";
            Log.d("query", query);
        } else {
            query = "SELECT * FROM " + TABLE_ITEMS + " WHERE " +
                    "lower(" + ITEM_NAME + ") LIKE '%' || lower(?) || '%' ";
            Log.d("query", query);
        }

        String normalizedSearchName = normalizeString(search_name);
        Cursor cursor = db.rawQuery(query, new String[]{normalizedSearchName});
        ArrayList<Item> items = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                int itemId = cursor.getInt(0);
                String name = cursor.getString(1);
                String nameVi = cursor.getString(2);
                String nameVi2 = cursor.getString(3);
                String sname = cursor.getString(4);
                String description = cursor.getString(5);
                String descriptionVi = cursor.getString(6);
                String image = cursor.getString(7);
                String clipart = cursor.getString(8);
                Item item = new Item(itemId, name, nameVi, nameVi2, sname, description, descriptionVi, image, clipart);
                items.add(item);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return items;
    }

    public long addCultivateContent(CultivateContent culcon) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(CULTIVATE_CONTENT_CULTIVATE_ID, culcon.getCul_id());
        values.put(CULTIVATE_CONTENT_ITEM_ID, culcon.getItem_id());
        values.put(CULTIVATE_CONTENT_NAME, culcon.getCulcon_name().trim());
        values.put(CULTIVATE_CONTENT_NAME_VI, culcon.getCulcon_nameVi().trim());
        values.put(CULTIVATE_CONTENT_NAME_VI2, culcon.getCulcon_nameVi2().trim());
        values.put(CULTIVATE_CONTENT_DESCRIPTION, culcon.getCulcon_description());
        values.put(CULTIVATE_CONTENT_DESCRIPTION_VI, culcon.getCulcon_descriptionVi());
        values.put(CULTIVATE_CONTENT_IMAGE, culcon.getCulcon_image());
        values.put(CULTIVATE_CONTENT_HTML, culcon.getCulcon_html());

        long result = -1;
        try {
            result = db.insertOrThrow(TABLE_CULTIVATE_CONTENT, null, values);

        } catch (SQLiteConstraintException e) {
            String errorMessage = e.getMessage();
            Log.e("SQLiteConstraintException", errorMessage);

            if (errorMessage.contains("UNIQUE")) {
                // Use regex to extract the specific column that caused the UNIQUE constraint failure
                Pattern pattern = Pattern.compile("UNIQUE constraint failed: (\\S+)");
                Matcher matcher = pattern.matcher(errorMessage);

                if (matcher.find()) {
                    String failedConstraint = matcher.group(1);

                    switch (failedConstraint) {
                        case "cultivate_content.culcon_name":
                            Toast.makeText(context, R.string.err_insert_contentNameExist, Toast.LENGTH_LONG).show();
                            break;
                        case "cultivate_content.culcon_nameVi":
                            Toast.makeText(context, R.string.err_insert_contentNameViExist, Toast.LENGTH_LONG).show();
                            break;
                        default:
                            Toast.makeText(context, R.string.duplicate_entryContent, Toast.LENGTH_LONG).show();
                            break;
                    }
                }
            } else if (errorMessage.contains("FOREIGN KEY")) {
                // Foreign key constraint failure (invalid item_id or cul_id)
                Toast.makeText(context, R.string.invalid_referenceContent, Toast.LENGTH_LONG).show();
            } else {
                // General constraint failure
                Toast.makeText(context, R.string.constraint_violation, Toast.LENGTH_LONG).show();
            }

            e.printStackTrace();
        } catch (Exception e) {
            // Handle other general errors
            Toast.makeText(context, "Insert failed: Unexpected error occurred.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } finally {
            db.close();  // Ensure database is always closed
        }
        return result;
    }

    public long modifyCultivateContent (CultivateContent culcon) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(CULTIVATE_CONTENT_CULTIVATE_ID, culcon.getCul_id());
        values.put(CULTIVATE_CONTENT_ITEM_ID, culcon.getItem_id());
        values.put(CULTIVATE_CONTENT_NAME, culcon.getCulcon_name().trim());
        values.put(CULTIVATE_CONTENT_NAME_VI, culcon.getCulcon_nameVi().trim());
        values.put(CULTIVATE_CONTENT_NAME_VI2, culcon.getCulcon_nameVi2().trim());
        values.put(CULTIVATE_CONTENT_DESCRIPTION, culcon.getCulcon_description());
        values.put(CULTIVATE_CONTENT_DESCRIPTION_VI, culcon.getCulcon_descriptionVi());
        values.put(CULTIVATE_CONTENT_IMAGE, culcon.getCulcon_image());
        values.put(CULTIVATE_CONTENT_HTML, culcon.getCulcon_html());

        long result = -1;
        try {
            result = db.update(TABLE_CULTIVATE_CONTENT, values, CULTIVATE_CONTENT_ID + " = " + culcon.getCulcon_id(), null);
        } catch (SQLiteConstraintException e) {
            String errorMessage = e.getMessage();
            Log.e("SQLiteConstraintException", errorMessage);

            if (errorMessage.contains("UNIQUE")) {
                // Use regex to extract the specific column that caused the UNIQUE constraint failure
                Pattern pattern = Pattern.compile("UNIQUE constraint failed: (\\S+)");
                Matcher matcher = pattern.matcher(errorMessage);

                if (matcher.find()) {
                    String failedConstraint = matcher.group(1);

                    switch (failedConstraint) {
                        case "cultivate_content.culcon_name":
                            Toast.makeText(context, R.string.err_insert_contentNameExist, Toast.LENGTH_LONG).show();
                            break;
                        case "cultivate_content.culcon_nameVi":
                            Toast.makeText(context, R.string.err_insert_contentNameViExist, Toast.LENGTH_LONG).show();
                            break;
                        default:
                            Toast.makeText(context, R.string.duplicate_entryContent, Toast.LENGTH_LONG).show();
                            break;
                    }
                }
            } else if (errorMessage.contains("FOREIGN KEY")) {
                // Foreign key constraint failure (invalid item_id or cul_id)
                Toast.makeText(context, R.string.invalid_referenceContent, Toast.LENGTH_LONG).show();
            } else {
                // General constraint failure
                Toast.makeText(context, R.string.constraint_violation, Toast.LENGTH_LONG).show();
            }
        }
        db.close();
        return result;
    }

    public ArrayList<CultivateContent> getCultivateContentsByItem(int c_id, int i_id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_CULTIVATE_CONTENT + " WHERE " + CULTIVATE_CONTENT_CULTIVATE_ID + " = " + c_id + " AND " + CULTIVATE_CONTENT_ITEM_ID + " = " + i_id;
        Cursor cursor = db.rawQuery(query, null);
        ArrayList<CultivateContent> culcons = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                int culcon_id = cursor.getInt(0);
                int cul_id = cursor.getInt(1);
                int item_id = cursor.getInt(2);
                String culcon_name = cursor.getString(3);
                String culcon_nameVi = cursor.getString(4);
                String culcon_nameVi2 = cursor.getString(5);
                String culcon_description = cursor.getString(6);
                String culcon_descriptionVi = cursor.getString(7);
                String culcon_image = cursor.getString(8);
                String culcon_html = cursor.getString(9);
                CultivateContent culcon = new CultivateContent(culcon_id, cul_id, item_id, culcon_name, culcon_nameVi, culcon_nameVi2, culcon_description, culcon_descriptionVi, culcon_image, culcon_html);
                culcons.add(culcon);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return culcons;
    }

    public CultivateContent getCultivateContentbyId(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_CULTIVATE_CONTENT + " WHERE " + CULTIVATE_CONTENT_ID + " = " + id;
        Cursor cursor = db.rawQuery(query, null);
        CultivateContent culcon = null;

        if (cursor.moveToFirst()) {
            int culcon_id = cursor.getInt(0);
            int cul_id = cursor.getInt(1);
            int item_id = cursor.getInt(2);
            String culcon_name = cursor.getString(3);
            String culcon_nameVi = cursor.getString(4);
            String culcon_nameVi2 = cursor.getString(5);
            String culcon_description = cursor.getString(6);
            String culcon_descriptionVi = cursor.getString(7);
            String culcon_image = cursor.getString(8);
            String culcon_html = cursor.getString(9);
            culcon = new CultivateContent(culcon_id, cul_id, item_id, culcon_name, culcon_nameVi, culcon_nameVi2, culcon_description, culcon_descriptionVi, culcon_image, culcon_html);
        }
        cursor.close();
        db.close();
        return culcon;
    }

    public void deleteCultivateContent(int culcon_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "DELETE FROM " + TABLE_CULTIVATE_CONTENT + " WHERE " + CULTIVATE_CONTENT_ID + " = " + culcon_id;
        db.execSQL(query);
        db.close();
    }

    public void addHistory(History h) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(HISTORY_ITEM_ID, h.getItem_id());
        values.put(HISTORY_DATETIME, h.getHistory_datetime());
        values.put(HISTORY_IMAGE, h.getHistory_image());
        values.put(HISTORY_MODEL, h.getModel());
        values.put(HISTORY_CONFIDENCE, h.getConfidence());
        db.insert(TABLE_HISTORY, null, values);
        db.close();
    }

    public void deleteHistory(int history_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "DELETE FROM " + TABLE_HISTORY + " WHERE " + HISTORY_ID + " = " + history_id;
        db.execSQL(query);
        db.close();
    }

    public ArrayList<History> getHistories() {
        ArrayList<History> histories = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_HISTORY + " ORDER BY " + HISTORY_DATETIME + " DESC";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                int history_id = cursor.getInt(0);
                int item_id = cursor.getInt(1);
                String history_datetime = cursor.getString(2);
                String history_image = cursor.getString(3);
                String history_model = cursor.getString(4);
                String history_confidence = cursor.getString(5);
                History history = new History(history_id, item_id, history_datetime, history_image, history_model, history_confidence);
                histories.add(history);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return histories;
    }

    private String normalizeString(String input) {
        if (input == null) return null;
        String nfdNormalizedString = Normalizer.normalize(input, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(nfdNormalizedString).replaceAll("").replaceAll("đ", "d").replaceAll("Đ", "D").toLowerCase();
    }
}
