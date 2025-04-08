package com.ramascript.allenconnect.features.bot;

import android.net.Uri;

import com.ramascript.allenconnect.R;

public class BotFileItem {
    private Uri fileUri;
    private String fileName;
    private String fileType;
    private String description;
    private int fileIconResId;

    public BotFileItem(Uri fileUri, String fileName, String fileType) {
        this.fileUri = fileUri;
        this.fileName = fileName;
        this.fileType = fileType;
        this.description = "";
        this.fileIconResId = getIconForFileType(fileType);
    }

    public Uri getFileUri() {
        return fileUri;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getFileIconResId() {
        return fileIconResId;
    }

    private int getIconForFileType(String fileType) {
        if (fileType == null)
            return R.drawable.ic_file_default;

        switch (fileType.toLowerCase()) {
            case "pdf":
                return R.drawable.ic_pdf;
            case "doc":
            case "docx":
                return R.drawable.ic_word;
            case "xls":
            case "xlsx":
                return R.drawable.ic_excel;
            case "txt":
                return R.drawable.ic_text;
            default:
                return R.drawable.ic_file_default;
        }
    }
}