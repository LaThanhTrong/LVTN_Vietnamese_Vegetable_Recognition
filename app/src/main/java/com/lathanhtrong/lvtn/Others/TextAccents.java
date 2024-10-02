package com.lathanhtrong.lvtn.Others;
import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.Locale;

public class TextAccents extends ArrayAdapter<String> {
    private ArrayList<String> originalItems;

    public TextAccents(Context context, int resource, int textViewResourceId, ArrayList<String> items) {
        super(context, resource, textViewResourceId, items);
        this.originalItems = new ArrayList<>(items);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                if (constraint == null || constraint.length() == 0) {
                    results.values = new ArrayList<>(originalItems);
                    results.count = originalItems.size();
                } else {
                    String filterString = removeAccents(constraint.toString()).toLowerCase(Locale.ROOT);
                    ArrayList<String> filteredList = new ArrayList<>();
                    for (String item : originalItems) {
                        if (removeAccents(item).toLowerCase(Locale.ROOT).contains(filterString)) {
                            filteredList.add(item);
                        }
                    }
                    results.values = filteredList;
                    results.count = filteredList.size();
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                clear();
                if (results.values != null) {
                    addAll((ArrayList<String>) results.values);
                }
                notifyDataSetChanged();
            }

            private String removeAccents(String string) {
                String normalizedString = Normalizer.normalize(string, Form.NFD);
                return normalizedString.replaceAll("\\p{M}", "");
            }
        };
    }
}
