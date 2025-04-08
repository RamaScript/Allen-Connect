package com.ramascript.allenconnect.features.bot;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ramascript.allenconnect.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SelectedFilesAdapter extends RecyclerView.Adapter<SelectedFilesAdapter.FileViewHolder> {
    private List<File> files = new ArrayList<>();
    private OnFileRemoveListener listener;

    public interface OnFileRemoveListener {
        void onFileRemove(File file);
    }

    public SelectedFilesAdapter(OnFileRemoveListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_selected_file, parent, false);
        return new FileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        File file = files.get(position);
        holder.tvFileName.setText(file.getName());
        holder.tvFileSize.setText(formatFileSize(file.length()));

        // Set file type icon based on extension
        String extension = getFileExtension(file.getName()).toLowerCase();
        int iconResId = R.drawable.ic_file_default;

        if (extension.equals("pdf")) {
            iconResId = R.drawable.ic_pdf;
        } else if (extension.equals("doc") || extension.equals("docx")) {
            iconResId = R.drawable.ic_word;
        } else if (extension.equals("xls") || extension.equals("xlsx")) {
            iconResId = R.drawable.ic_excel;
        } else if (extension.equals("txt")) {
            iconResId = R.drawable.ic_text;
        }

        holder.ivFileType.setImageResource(iconResId);

        holder.btnRemove.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFileRemove(file);
            }
        });
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    public void setFiles(List<File> files) {
        this.files = files;
        notifyDataSetChanged();
    }

    public void addFile(File file) {
        files.add(file);
        notifyItemInserted(files.size() - 1);
    }

    public void removeFile(File file) {
        int position = files.indexOf(file);
        if (position != -1) {
            files.remove(position);
            notifyItemRemoved(position);
        }
    }

    private String formatFileSize(long size) {
        if (size <= 0)
            return "0 B";
        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return String.format("%.1f %s", size / Math.pow(1024, digitGroups), units[digitGroups]);
    }

    private String getFileExtension(String fileName) {
        if (fileName == null)
            return "";
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex == -1)
            return "";
        return fileName.substring(lastDotIndex + 1);
    }

    static class FileViewHolder extends RecyclerView.ViewHolder {
        ImageView ivFileType;
        TextView tvFileName;
        TextView tvFileSize;
        ImageView btnRemove;

        FileViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFileType = itemView.findViewById(R.id.fileTypeIcon);
            tvFileName = itemView.findViewById(R.id.fileNameText);
            tvFileSize = itemView.findViewById(R.id.fileSizeText);
            btnRemove = itemView.findViewById(R.id.removeButton);
        }
    }
}