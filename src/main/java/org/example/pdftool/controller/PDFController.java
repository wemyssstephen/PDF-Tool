package org.example.pdftool.controller;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PDFController {
    private PDDocument document;
    private int currentPage = 0;

    private List<PDFSearchResult> currentSearchResults;
    private int currentSearchIndex;

    public PDFController() {
        currentSearchResults = new ArrayList<>();
        currentSearchIndex = -1;
    }

    public class PDFSearchResult {
        private final int pageNumber;
        private final String text;
        private final PDRectangle position;

        public PDFSearchResult(int pageNumber, String text, PDRectangle position) {
            this.pageNumber = pageNumber;
            this.text = text;
            this.position = position;
        }
        public int getPageNumber() {return pageNumber;}
        public String getText() {return text;}
        public PDRectangle getPosition() {return position;}
    }

    public List<PDFSearchResult> searchText(String searchTerm) throws IOException {
        System.out.println("Searching for " + searchTerm);
        if (document == null || searchTerm.isEmpty()) {
            System.out.println("Document null or empty search term");
            return Collections.emptyList();
        }

        currentSearchResults.clear();
        currentSearchIndex = -1;

        PDFTextStripper stripper = new PDFTextStripper();

        for (int pageNum = 0; pageNum < document.getNumberOfPages(); pageNum++) {
            stripper.setStartPage(pageNum);
            stripper.setEndPage(pageNum + 1);
            String pageText = stripper.getText(document);

            if (pageText.toLowerCase().contains(searchTerm.toLowerCase())) {
                currentSearchResults.add(new PDFSearchResult(
                        pageNum,
                        searchTerm,
                        new PDRectangle(0,0,10,20)
                ));
            }
        }
        return currentSearchResults;
    }

    public PDFSearchResult getNextSearchResult() {
        if (currentSearchResults.isEmpty()) return null;
        currentSearchIndex = (currentSearchIndex + 1) % currentSearchResults.size();
        return currentSearchResults.get(currentSearchIndex);
    }

    public PDFSearchResult getPreviousSearchResult() {
        if (currentSearchResults.isEmpty()) return null;
        currentSearchIndex = (currentSearchIndex - 1) % currentSearchResults.size();
        return currentSearchResults.get(currentSearchIndex);
    }

    public List<PDFSearchResult> getCurrentSearchResults() {
        return currentSearchResults;
    }

    public int getCurrentSearchIndex() {
        return currentSearchIndex;
    }

    public PDFRenderer getRenderer() {
        if (document == null) {throw new IllegalStateException("No document has been loaded");}
        return new PDFRenderer(document);
    }

    public void loadPDFDocument(File file) throws IOException {
        if (document != null) {document.close();}

        java.util.logging.Logger.getLogger("org.apache.fontbox.cff.Type1CharString").setLevel(java.util.logging.Level.SEVERE);
        document = Loader.loadPDF(file);
    }

    public void savePDFDocument(File file) throws IOException {
        if (document == null) {throw new IllegalStateException("No document has been loaded");}
        document.save(file);
    }

    public PDDocument getDocument() {
        if (document == null) {
            throw new IllegalStateException("No document has been loaded");
        }
        return document;
    }

    public int getCurrentPage() {
        if (document == null) {
            throw new IllegalStateException("No document has been loaded");
        }
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        if (document != null && currentPage < document.getNumberOfPages() && currentPage >= 0) {}
        this.currentPage = currentPage;
    }

    public int nextPage() {
        if (document == null) {
            throw new IllegalStateException("No document has been loaded");
        }
        if (currentPage <= document.getNumberOfPages() - 1) {
            currentPage++;
        }
        return currentPage;
    }

    public int previousPage() {
        if (document == null) {
            throw new IllegalStateException("No document has been loaded");
        }
        if (currentPage > 0) {
            currentPage--;
        }
        return currentPage;
    }

    public int getPageCount() {
        return document != null ? document.getNumberOfPages() : 0;
    }

    public void closeDocument() throws IOException {
        if (document != null) {
            document.close();
        }
    }
}
