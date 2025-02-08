package org.example.pdftool.controller;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PDFController {
    private PDDocument document;
    private int currentPage = 0;

    public static List<PDFSearchResult> currentSearchResults;
    private int currentSearchIndex;

    public PDFController() {
        currentSearchResults = new ArrayList<>();
        currentSearchIndex = -1;
    }

    private static class PositionalTextStripper extends PDFTextStripper {
        private final String searchTerm;
        private final List<PDFSearchResult> results;
        private final int pageNum;

        public PositionalTextStripper(String searchTerm, List<PDFSearchResult> results, int pageNum) {
            this.searchTerm = searchTerm.toLowerCase();
            this.results = results;
            this.pageNum = pageNum;
            setSortByPosition(true);
        }

        @Override
        protected void writeString(String text, List<TextPosition> textPositions) {
            String lowerCaseText = text.toLowerCase();
            int startIndex = lowerCaseText.indexOf(searchTerm);

            if (startIndex != -1) {
                System.out.println("Found a match for: " + searchTerm);

                int endIndex = startIndex + searchTerm.length();

                // Find text position of matching word
                TextPosition startPosition = null;
                TextPosition endPosition = null;
                int currentIndex = 0;

                for (TextPosition pos : textPositions) {
                    if (currentIndex == startIndex) {
                        startPosition = pos;
                    }
                    if (currentIndex == endIndex - 1) {
                        endPosition = pos;
                        break;
                    }
                    currentIndex += pos.getUnicode().length();
                }

                // If we have good positions, store the position in a box
                if (startPosition != null && endPosition != null) {
                    PDRectangle position = new PDRectangle(
                            startPosition.getXDirAdj(),
                            startPosition.getYDirAdj(),
                            endPosition.getXDirAdj() + endPosition.getWidth() - startPosition.getXDirAdj(),
                            startPosition.getHeight()
                    );
                    results.add(new PDFSearchResult(pageNum, text.substring(startIndex, endIndex), position));
                    System.out.println("Result size: " + results.size());
                }
            }
        }
    }


    public record PDFSearchResult(int pageNumber, String text, PDRectangle position) {
    }

    public List<PDFSearchResult> searchText(String searchTerm) throws IOException {
        System.out.println("Searching for " + searchTerm);
        if (document == null || searchTerm.isEmpty()) {
            System.out.println("Document null or empty search term");
            return Collections.emptyList();
        }

        clearSearchResults();
        currentSearchIndex = -1;

        for (int pageNum = 0; pageNum < document.getNumberOfPages(); pageNum++) {
            PositionalTextStripper stripper = new PositionalTextStripper(
                    searchTerm,
                    currentSearchResults,
                    pageNum
            );
            stripper.setStartPage(pageNum + 1);
            stripper.setEndPage(pageNum + 1);
            stripper.getText(document);
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

    public void clearSearchResults() {
        currentSearchResults.clear();
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
        if (document != null && currentPage < document.getNumberOfPages() && currentPage >= 0) {
            this.currentPage = currentPage;
        }
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
