package com.ramascript.allenconnect.features.bot;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ramascript.allenconnect.R;

import java.io.File;
import java.util.List;

public class SelectedFilesAdapter extends RecyclerView.Adapter<SelectedFilesAdapter.FileViewHolder> {

    private final List<FileItem> fileItems;
    private final Context context;
    private final OnFileRemoveListener removeListener;

    public interface OnFileRemoveListener {
        void onRemoveFile(int position);
    }

    public static class FileItem {
        private final Uri uri;
        private final String displayName;
        private final String fileType;

        public FileItem(Uri uri, String displayName, String fileType) {
            this.uri = uri;
            this.displayName = displayName;
            this.fileType = fileType;
        }

        public Uri getUri() {
            return uri;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getFileType() {
            return fileType;
        }
    }

    public SelectedFilesAdapter(Context context, List<FileItem> fileItems, OnFileRemoveListener removeListener) {
        this.context = context;
        this.fileItems = fileItems;
        this.removeListener = removeListener;
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_selected_file, parent, false);
        return new FileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        FileItem fileItem = fileItems.get(position);
        holder.fileName.setText(fileItem.getDisplayName());

        // Set file type icon based on file extension
        String fileType = fileItem.getFileType().toLowerCase();
        if (fileType.equals("pdf")) {
            holder.fileIcon.setImageResource(R.drawable.ic_pdf_file);
        } else if (fileType.equals("doc") || fileType.equals("docx")) {
            holder.fileIcon.setImageResource(R.drawable.ic_doc_file);
        } else if (fileType.equals("xls") || fileType.equals("xlsx")) {
            holder.fileIcon.setImageResource(R.drawable.ic_xls_file);
        } else if (fileType.equals("txt")) {
            holder.fileIcon.setImageResource(R.drawable.ic_txt_file);
        } else {
            holder.fileIcon.setImageResource(R.drawable.ic_file_generic);
        }

        holder.removeButton.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                removeListener.onRemoveFile(adapterPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        return fileItems.size();
    }

    public void addFile(FileItem fileItem) {
        fileItems.add(fileItem);
        notifyItemInserted(fileItems.size() - 1);
    }

    public void removeFile(int position) {
        if (position >= 0 && position < fileItems.size()) {
            fileItems.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, fileItems.size());
        }
    }

    public List<FileItem> getFileItems() {
        return fileItems;
    }

    static class FileViewHolder extends RecyclerView.ViewHolder {
        ImageView fileIcon;
        TextView fileName;
        ImageButton removeButton;

        FileViewHolder(@NonNull View itemView) {
            super(itemView);
            fileIcon = itemView.findViewById(R.id.file_icon);
            fileName = itemView.findViewById(R.id.file_name);
            removeButton = itemView.findViewById(R.id.remove_file_button);
        }
    }
}