package backend.app.api;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class DocumentTextExtractor {

    private static final int CHUNK_SIZE = 1000;
    private static final int OVERLAP = 100;

    public static String extractText(Path filePath) throws IOException {
        String fileName = filePath.getFileName().toString().toLowerCase();

        if (fileName.endsWith(".pdf")) {
            return extractPdf(filePath);
        }
        if (fileName.endsWith(".docx")) {
            return extractDocx(filePath);
        }

        throw new IllegalArgumentException("Supported formats: PDF, DOCX");
    }

    public static List<String> splitIntoChunks(String text) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return chunks;
        }

        int step = CHUNK_SIZE - OVERLAP;
        for (int start = 0; start < text.length(); start += step) {
            int end = Math.min(start + CHUNK_SIZE, text.length());
            chunks.add(text.substring(start, end));
            if (end == text.length()) {
                break;
            }
        }

        return chunks;
    }

    private static String extractPdf(Path filePath) throws IOException {
        try (PDDocument document = Loader.loadPDF(filePath.toFile())) {
            return new PDFTextStripper().getText(document);
        }
    }

    private static String extractDocx(Path filePath) throws IOException {
        try (InputStream inputStream = Files.newInputStream(filePath);
             XWPFDocument document = new XWPFDocument(inputStream)) {
            StringBuilder text = new StringBuilder();
            document.getParagraphs().forEach(paragraph -> text.append(paragraph.getText()).append('\n'));
            return text.toString();
        }
    }
}
