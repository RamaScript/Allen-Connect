package com.ramascript.allenconnect.features.bot;

public class UploadedFile {
    private String fileId;
    private String fileName;
    private String fileType;
    private String downloadUrl;
    private String description;
    private String uploadTime;

    public UploadedFile(String fileId, String fileName, String fileType, String downloadUrl,
            String description, String uploadTime) {
        this.fileId = fileId;
        this.fileName = fileName;
        this.fileType = fileType;
        this.downloadUrl = downloadUrl;
        this.description = description;
        this.uploadTime = uploadTime;
    }

    public String getFileId() {
        return fileId;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public String getDescription() {
        return description;
    }

    public String getUploadTime() {
        return uploadTime;
    }

    public int getFileIconResId() {
        if (fileType == null) {
            return com.ramascript.allenconnect.R.drawable.ic_file_default;
        }

        switch (fileType.toLowerCase()) {
            case "pdf":
                return com.ramascript.allenconnect.R.drawable.ic_pdf;
            case "doc":
            case "docx":
                return com.ramascript.allenconnect.R.drawable.ic_word;
            case "xls":
            case "xlsx":
                return com.ramascript.allenconnect.R.drawable.ic_excel;
            case "txt":
                return com.ramascript.allenconnect.R.drawable.ic_text;
            default:
                return com.ramascript.allenconnect.R.drawable.ic_file_default;
        }
    }
}