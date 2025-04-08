package com.ramascript.allenconnect.features.bot;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xssf.extractor.XSSFExcelExtractor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FileTextExtractor {
    private static final String TAG = "FileTextExtractor";

    public interface TextExtractionCallback {
        void onTextExtracted(String text);

        void onExtractionFailed(Exception e);
    }

    public static void extractTextFromFile(Context context, Uri fileUri, String fileType,
            TextExtractionCallback callback) {
        new AsyncTask<Void, Void, String>() {
            private Exception exception = null;

            @Override
            protected String doInBackground(Void... voids) {
                try {
                    InputStream inputStream = context.getContentResolver().openInputStream(fileUri);
                    if (inputStream == null) {
                        throw new Exception("Cannot open file input stream");
                    }

                    String extractedText;
                    switch (fileType.toLowerCase()) {
                        case "pdf":
                            extractedText = extractFromPdf(inputStream);
                            break;
                        case "doc":
                        case "docx":
                            extractedText = extractFromDocx(inputStream);
                            break;
                        case "xls":
                        case "xlsx":
                            extractedText = extractFromExcel(inputStream);
                            break;
                        case "txt":
                            extractedText = extractFromTxt(inputStream);
                            break;
                        default:
                            throw new Exception("Unsupported file type: " + fileType);
                    }

                    inputStream.close();
                    return extractedText;
                } catch (Exception e) {
                    Log.e(TAG, "Text extraction failed", e);
                    exception = e;
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String extractedText) {
                if (extractedText != null) {
                    callback.onTextExtracted(extractedText);
                } else {
                    callback.onExtractionFailed(exception != null ? exception : new Exception("Unknown error"));
                }
            }
        }.execute();
    }

    public static void extractAndSaveToFirebase(Context context, Uri fileUri, String fileType, String fileId,
            String userId, TextExtractionCallback callback) {
        extractTextFromFile(context, fileUri, fileType, new TextExtractionCallback() {
            @Override
            public void onTextExtracted(String text) {
                // Save the extracted text to Firebase
                DatabaseReference database = FirebaseDatabase.getInstance().getReference();
                database.child("data_for_chatbot").child(fileId).child("extractedText").setValue(text)
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "Text extraction saved successfully for file: " + fileId);
                            if (callback != null) {
                                callback.onTextExtracted(text);
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error saving extracted text", e);
                            if (callback != null) {
                                callback.onExtractionFailed(
                                        new Exception("Failed to save text to Firebase: " + e.getMessage()));
                            }
                        });
            }

            @Override
            public void onExtractionFailed(Exception e) {
                if (callback != null) {
                    callback.onExtractionFailed(e);
                }
            }
        });
    }

    private static String extractFromPdf(InputStream inputStream) throws Exception {
        PDDocument document = PDDocument.load(inputStream);
        PDFTextStripper stripper = new PDFTextStripper();
        String text = stripper.getText(document);
        document.close();
        return text;
    }

    private static String extractFromDocx(InputStream inputStream) throws Exception {
        XWPFDocument document = new XWPFDocument(inputStream);
        XWPFWordExtractor extractor = new XWPFWordExtractor(document);
        String text = extractor.getText();
        extractor.close();
        return text;
    }

    private static String extractFromExcel(InputStream inputStream) throws Exception {
        XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
        XSSFExcelExtractor extractor = new XSSFExcelExtractor(workbook);
        extractor.setFormulasNotResults(false);
        extractor.setIncludeSheetNames(true);
        String text = extractor.getText();
        extractor.close();
        return text;
    }

    private static String extractFromTxt(InputStream inputStream) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line).append("\n");
        }
        reader.close();
        return stringBuilder.toString();
    }
}