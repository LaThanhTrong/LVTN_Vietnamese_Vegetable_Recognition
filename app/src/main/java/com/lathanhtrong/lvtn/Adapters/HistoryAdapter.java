package com.lathanhtrong.lvtn.Adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.lathanhtrong.lvtn.DBHandler;
import com.lathanhtrong.lvtn.Models.History;
import com.lathanhtrong.lvtn.Models.Item;
import com.lathanhtrong.lvtn.Others.Utils;
import com.lathanhtrong.lvtn.R;
import com.lathanhtrong.lvtn.databinding.HistoryRvBinding;

import java.io.File;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private List<History> histories;
    private Context context;

    public HistoryAdapter(List<History> histories, Context context) {
        this.histories = histories;
        this.context = context;
    }

    @Override
    public HistoryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return HistoryAdapter.ViewHolder.from(parent);
    }

    @Override
    public int getItemCount() {
        return histories.size();
    }

    @Override
    public void onBindViewHolder(HistoryAdapter.ViewHolder holder, int position) {
        String language = Utils.getLanguage(holder.itemView.getContext());

        DBHandler dbHandler = new DBHandler(holder.itemView.getContext());
        int item_id = histories.get(holder.getAdapterPosition()).getItem_id();
        Item item = dbHandler.getItembyId(item_id);

        if (item != null) {
            if (language.equals("vi")) {
                holder.binding.tvName.setText(item.getItem_nameVi());
            } else {
                holder.binding.tvName.setText(item.getItem_name());
            }
        }

        History currentHistory = histories.get(holder.getAdapterPosition());
        holder.binding.tvDatetime.setText(currentHistory.getHistory_datetime());
        holder.binding.tvModel.setText(context.getResources().getString(R.string.model)+": "+currentHistory.getModel());
        holder.binding.tvConf.setText(context.getResources().getString(R.string.confidence)+": "+currentHistory.getConfidence());

        String imageName = currentHistory.getHistory_image();
        String directoryPath = holder.itemView.getContext().getFilesDir() + "/historyImages";
        File imageFile = new File(directoryPath, imageName + ".png");

        if (imageFile.exists()) {
            Glide.with(holder.itemView.getContext())
                    .load(imageFile)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(holder.binding.ivImage);
        } else {
            Toast.makeText(context, "Not loaded", Toast.LENGTH_SHORT).show();
        }

        holder.binding.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder delete = new AlertDialog.Builder(holder.itemView.getContext());
                ProgressDialog progressDialog = new ProgressDialog(holder.itemView.getContext());
                delete.setTitle(v.getResources().getString(R.string.del_history));
                delete.setMessage(v.getResources().getString(R.string.warnDelete)+" "+ v.getResources().getString(R.string.this_image) +" "+v.getResources().getString(R.string.from)+" "+ v.getResources().getString(R.string.historyClassification) +"?");
                delete.setPositiveButton(v.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        progressDialog.setMessage(v.getResources().getString(R.string.del_history));
                        progressDialog.show();
                        progressDialog.setCanceledOnTouchOutside(false);
                        progressDialog.setCancelable(false);

                        dbHandler.deleteHistory(histories.get(holder.getAdapterPosition()).getHistory_id());

                        String imageName = histories.get(holder.getAdapterPosition()).getHistory_image();
                        String directoryPath = context.getFilesDir() + "/historyImages";
                        File imageFile = new File(directoryPath, imageName + ".png");

                        if (imageFile.exists()) {
                            imageFile.delete();
                        }

                        histories.remove(holder.getAdapterPosition());
                        notifyItemRemoved(holder.getAdapterPosition());
                        notifyItemRangeChanged(holder.getAdapterPosition(), histories.size());
                        progressDialog.dismiss();
                        Toast.makeText(context, v.getResources().getString(R.string.delHistorySuccess), Toast.LENGTH_SHORT).show();
                    }
                }).setNegativeButton(v.getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();
            }
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private HistoryRvBinding binding;

        public ViewHolder(HistoryRvBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public static ViewHolder from(ViewGroup parent) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            HistoryRvBinding binding = HistoryRvBinding.inflate(layoutInflater, parent, false);
            return new ViewHolder(binding);
        }
    }
}
