package com.ramascript.allenconnect.features.bot;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.ramascript.allenconnect.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UploadedFilesAdapter extends RecyclerView.Adapter<UploadedFilesAdapter.FileViewHolder> {
    private List<UploadedFile> files;
    private OnFileDeleteListener onFileDeleteListener;
    private OnFileItemClickListener onFileItemClickListener;
    private OnFileLongClickListener onFileLongClickListener;
    private boolean multiSelectMode = false;
    private Set<Integer> selectedPositions = new HashSet<>();

    public interface OnFileDeleteListener {
        void onFileDelete(String fileId);
    }

    public interface OnFileItemClickListener {
        void onFileItemClick(int position);
    }

    public interface OnFileLongClickListener {
        boolean onFileLongClick(int position);
    }

    public UploadedFilesAdapter(List<UploadedFile> files) {
        this.files = files;
    }

    public void setFiles(List<UploadedFile> files) {
        this.files = files;
        notifyDataSetChanged();
    }

    public List<UploadedFile> getFiles() {
        return files;
    }

    public void setOnFileDeleteListener(OnFileDeleteListener listener) {
        this.onFileDeleteListener = listener;
    }

    public void setOnFileItemClickListener(OnFileItemClickListener listener) {
        this.onFileItemClickListener = listener;
    }

    public void setOnFileLongClickListener(OnFileLongClickListener listener) {
        this.onFileLongClickListener = listener;
    }

    public void setMultiSelectMode(boolean multiSelectMode) {
        this.multiSelectMode = multiSelectMode;
        if (!multiSelectMode) {
            selectedPositions.clear();
        }
        notifyDataSetChanged();
    }

    public boolean isMultiSelectMode() {
        return multiSelectMode;
    }

    public void toggleSelection(int position) {
        if (selectedPositions.contains(position)) {
            selectedPositions.remove(position);
        } else {
            selectedPositions.add(position);
        }
        notifyItemChanged(position);
    }

    public void selectAll() {
        selectedPositions.clear();
        for (int i = 0; i < files.size(); i++) {
            selectedPositions.add(i);
        }
        notifyDataSetChanged();
    }

    public void clearSelections() {
        selectedPositions.clear();
        notifyDataSetChanged();
    }

    public List<UploadedFile> getSelectedFiles() {
        List<UploadedFile> selectedFiles = new ArrayList<>();
        for (Integer position : selectedPositions) {
            if (position < files.size()) {
                selectedFiles.add(files.get(position));
            }
        }
        return selectedFiles;
    }

    public int getSelectedItemCount() {
        return selectedPositions.size();
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_uploaded_file, parent, false);
        return new FileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        UploadedFile file = files.get(position);

        holder.tvFileName.setText(file.getFileName());
        holder.tvFileType.setText(file.getFileType().toUpperCase());
        holder.tvUploadTime.setText(file.getUploadTime());

        if (file.getDescription() != null && !file.getDescription().isEmpty()) {
            holder.tvDescription.setVisibility(View.VISIBLE);
            holder.tvDescription.setText(file.getDescription());
        } else {
            holder.tvDescription.setVisibility(View.GONE);
        }

        holder.ivFileIcon.setImageResource(file.getFileIconResId());

        // Handle multi-select UI
        if (multiSelectMode) {
            if (selectedPositions.contains(position)) {
                holder.cardView.setCardBackgroundColor(
                        ContextCompat.getColor(holder.itemView.getContext(), R.color.selection_highlight));
            } else {
                holder.cardView.setCardBackgroundColor(
                        ContextCompat.getColor(holder.itemView.getContext(), R.color.white));
            }
            holder.btnDelete.setVisibility(View.GONE);
        } else {
            holder.cardView.setCardBackgroundColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.white));
            holder.btnDelete.setVisibility(View.VISIBLE);
        }

        // Set click listeners
        holder.cardView.setOnClickListener(v -> {
            if (multiSelectMode) {
                toggleSelection(position);
                if (onFileItemClickListener != null) {
                    onFileItemClickListener.onFileItemClick(position);
                }
            } else if (onFileItemClickListener != null) {
                onFileItemClickListener.onFileItemClick(position);
            }
        });

        holder.cardView.setOnLongClickListener(v -> {
            if (onFileLongClickListener != null) {
                return onFileLongClickListener.onFileLongClick(position);
            }
            return false;
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (onFileDeleteListener != null && !multiSelectMode) {
                onFileDeleteListener.onFileDelete(file.getFileId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    static class FileViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView ivFileIcon;
        TextView tvFileName;
        TextView tvFileType;
        TextView tvUploadTime;
        TextView tvDescription;
        ImageButton btnDelete;

        FileViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            ivFileIcon = itemView.findViewById(R.id.ivFileIcon);
            tvFileName = itemView.findViewById(R.id.tvFileName);
            tvFileType = itemView.findViewById(R.id.tvFileType);
            tvUploadTime = itemView.findViewById(R.id.tvUploadTime);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}