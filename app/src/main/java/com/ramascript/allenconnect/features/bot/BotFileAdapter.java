package com.ramascript.allenconnect.features.bot;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.ramascript.allenconnect.R;

import java.util.ArrayList;
import java.util.List;

public class BotFileAdapter extends RecyclerView.Adapter<BotFileAdapter.FileViewHolder> {
    private List<BotFileItem> fileItems = new ArrayList<>();
    private OnFileRemovedListener onFileRemovedListener;

    public interface OnFileRemovedListener {
        void onFileRemoved(int position);
    }

    public void setOnFileRemovedListener(OnFileRemovedListener listener) {
        this.onFileRemovedListener = listener;
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_selected_file, parent, false);
        return new FileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        BotFileItem item = fileItems.get(position);
        holder.tvFileName.setText(item.getFileName());
        holder.ivFileIcon.setImageResource(item.getFileIconResId());

        // Set description if exists
        if (item.getDescription() != null && !item.getDescription().isEmpty()) {
            holder.etFileDescription.setText(item.getDescription());
        } else {
            holder.etFileDescription.setText("");
        }

        // Set up description change listener
        holder.etFileDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                item.setDescription(s.toString());
            }
        });

        // Set up remove button
        holder.btnRemoveFile.setOnClickListener(v -> {
            if (onFileRemovedListener != null) {
                onFileRemovedListener.onFileRemoved(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return fileItems.size();
    }

    public void addFile(BotFileItem fileItem) {
        fileItems.add(fileItem);
        notifyItemInserted(fileItems.size() - 1);
    }

    public void removeFile(int position) {
        if (position >= 0 && position < fileItems.size()) {
            fileItems.remove(position);
            notifyItemRemoved(position);
        }
    }

    public List<BotFileItem> getFileItems() {
        return fileItems;
    }

    static class FileViewHolder extends RecyclerView.ViewHolder {
        ImageView ivFileIcon;
        TextView tvFileName;
        TextInputEditText etFileDescription;
        ImageView btnRemoveFile;

        FileViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFileIcon = itemView.findViewById(R.id.fileTypeIcon);
            tvFileName = itemView.findViewById(R.id.fileNameText);
            etFileDescription = itemView.findViewById(R.id.etFileDescription);
            btnRemoveFile = itemView.findViewById(R.id.removeButton);
        }
    }
}